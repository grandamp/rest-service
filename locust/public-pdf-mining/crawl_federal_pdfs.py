#!/usr/bin/env python3
"""
Zero-API-key crawler that extracts direct .pdf links from public federal
index pages and XML sitemaps, deduplicating against existing URL lists.

Usage:
  python3 locust/public-pdf-mining/crawl_federal_pdfs.py --out locust/urls_crawled.txt --depth 1
  python3 locust/public-pdf-mining/crawl_federal_pdfs.py --seed https://www.acq.osd.mil/dpap/dars/class_deviations.html --depth 1 --out locust/mil_crawled.txt
  python3 locust/public-pdf-mining/crawl_federal_pdfs.py --sitemap https://nasa.gov --match "/saa/" --out locust/nasa_saa.txt
"""

import argparse
import http.client
import re
import ssl
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
from collections import deque
from html.parser import HTMLParser
from pathlib import Path
from typing import Optional

DEFAULT_SEEDS = [
    # --- Government-Wide Acquisition ---
    "https://www.acquisition.gov/far-overhaul/far-part-deviation-guide",
    "https://acquisition.gov",

    # --- 15 Cabinet-Level Executive Departments (CFO Act) ---
    "https://usda.gov",
    "https://commerce.gov",
    "https://www.acq.osd.mil/dpap/dars/dfars_far_overhaul_class_deviations.html",
    "https://osd.mil",
    "https://ed.gov",
    "https://energy.gov",
    "https://hhs.gov",
    "https://dhs.gov",
    "https://hud.gov",
    "https://doi.gov",
    "https://justice.gov",
    "https://dol.gov",
    "https://state.gov",
    "https://transportation.gov",
    "https://treasury.gov",
    "https://va.gov",

    # --- 9 Independent CFO Act Agencies ---
    "https://epa.gov",
    "https://gsa.gov",
    "https://nasa.gov",
    "https://nsf.gov",
    "https://nrc.gov",
    "https://opm.gov",
    "https://sba.gov",
    "https://ssa.gov",
    "https://usaid.gov",
]


DEFAULT_USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/131.0.0.0 Safari/537.36"
)


class LinkExtractor(HTMLParser):
    def __init__(self) -> None:
        super().__init__()
        self.hrefs: list[str] = []

    def handle_starttag(self, tag: str, attrs: list[tuple[str, Optional[str]]]) -> None:
        if tag.lower() == "a":
            for k, v in attrs:
                if k.lower() == "href" and v:
                    self.hrefs.append(v.strip())


def build_ssl_context(ca_bundle: Optional[str], insecure: bool) -> ssl.SSLContext:
    ctx = ssl.create_default_context()
    if insecure:
        ctx.check_hostname = False
        ctx.verify_mode = ssl.CERT_NONE
        return ctx
    if ca_bundle:
        ca_path = Path(ca_bundle)
        if not ca_path.exists():
            raise FileNotFoundError(f"CA bundle path not found: {ca_bundle}")
        if ca_path.is_dir():
            ctx.load_verify_locations(capath=str(ca_path))
            for pattern in ("*.pem", "*.crt", "*.cer"):
                for cert_file in sorted(ca_path.glob(pattern)):
                    if cert_file.is_file():
                        ctx.load_verify_locations(cafile=str(cert_file))
        else:
            ctx.load_verify_locations(cafile=str(ca_path))
    return ctx


def fetch_text(
    url: str,
    ssl_context: ssl.SSLContext,
    timeout: int = 20,
) -> Optional[str]:
    req = urllib.request.Request(
        url,
        headers={
            "User-Agent": DEFAULT_USER_AGENT,
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Language": "en-US,en;q=0.9",
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout, context=ssl_context) as resp:
            ctype = resp.headers.get("Content-Type", "").lower()
            if "pdf" in ctype or "octet-stream" in ctype:
                return None
            raw = resp.read()
            return raw.decode("utf-8", errors="replace")
    except (
        urllib.error.HTTPError,
        urllib.error.URLError,
        http.client.HTTPException,
        TimeoutError,
        ssl.SSLError,
        OSError,
    ) as e:
        print(f"  [WARN] Failed to fetch {url}: {e}", file=sys.stderr)
        return None


def is_pdf_url(url: str) -> bool:
    path = urllib.parse.urlsplit(url).path.lower()
    return path.endswith(".pdf")


def normalize_url(base_url: str, href: str) -> Optional[str]:
    if not href or href.startswith(("#", "mailto:", "javascript:", "tel:")):
        return None
    full = urllib.parse.urljoin(base_url, href)
    parts = urllib.parse.urlsplit(full)
    if parts.scheme not in ("http", "https"):
        return None
    # Strip URL fragment while preserving query params (e.g., ?ver=...)
    return urllib.parse.urlunsplit((parts.scheme, parts.netloc, parts.path, parts.query, ""))


def load_existing_urls(paths: list[Path]) -> set[str]:
    existing: set[str] = set()
    for p in paths:
        if p.is_file():
            for line in p.read_text(encoding="utf-8").splitlines():
                cleaned = line.strip()
                if cleaned and not cleaned.startswith("#"):
                    existing.add(cleaned)
    return existing


def crawl_html_seeds(
    seeds: list[str],
    max_depth: int,
    max_pages: int,
    url_filter: Optional[re.Pattern[str]],
    seen_pdfs: set[str],
    ssl_context: ssl.SSLContext,
    delay: float,
    timeout: int,
) -> list[str]:
    discovered: list[str] = []
    visited_pages: set[str] = set()
    allowed_hosts = {urllib.parse.urlsplit(s).netloc.lower() for s in seeds}

    queue: deque[tuple[str, int]] = deque((s, 0) for s in seeds)

    while queue and len(visited_pages) < max_pages:
        page_url, depth = queue.popleft()
        if page_url in visited_pages:
            continue
        visited_pages.add(page_url)

        print(f"[PAGE {len(visited_pages)}/{max_pages} | depth={depth}] {page_url}")
        html = fetch_text(page_url, ssl_context=ssl_context, timeout=timeout)
        if not html:
            continue

        parser = LinkExtractor()
        parser.feed(html)

        added_here = 0
        for href in parser.hrefs:
            resolved = normalize_url(page_url, href)
            if not resolved:
                continue

            if is_pdf_url(resolved):
                if url_filter and not url_filter.search(resolved):
                    continue
                if resolved not in seen_pdfs:
                    seen_pdfs.add(resolved)
                    discovered.append(resolved)
                    added_here += 1
            elif depth < max_depth:
                host = urllib.parse.urlsplit(resolved).netloc.lower()
                if host in allowed_hosts and resolved not in visited_pages:
                    queue.append((resolved, depth + 1))

        print(f"  Found {added_here} new PDF link(s)")
        if delay > 0:
            time.sleep(delay)

    return discovered


def crawl_sitemaps(
    sitemap_urls: list[str],
    url_filter: Optional[re.Pattern[str]],
    seen_pdfs: set[str],
    ssl_context: ssl.SSLContext,
    delay: float,
    timeout: int,
    max_sitemaps: int = 25,
) -> list[str]:
    discovered: list[str] = []
    visited: set[str] = set()
    queue: deque[str] = deque(sitemap_urls)

    while queue and len(visited) < max_sitemaps:
        sm_url = queue.popleft()
        if sm_url in visited:
            continue
        visited.add(sm_url)

        print(f"[SITEMAP {len(visited)}] {sm_url}")
        xml_text = fetch_text(sm_url, ssl_context=ssl_context, timeout=timeout)
        if not xml_text:
            continue

        try:
            root = ET.fromstring(xml_text)
        except ET.ParseError as e:
            print(f"  [WARN] Invalid XML in {sm_url}: {e}", file=sys.stderr)
            continue

        added_here = 0
        for elem in root.iter():
            tag_name = elem.tag.split("}", 1)[-1].lower()
            if tag_name == "loc" and elem.text:
                loc = elem.text.strip()
                if loc.endswith(".xml") or "sitemap" in loc.lower():
                    if loc not in visited:
                        queue.append(loc)
                elif is_pdf_url(loc):
                    if url_filter and not url_filter.search(loc):
                        continue
                    if loc not in seen_pdfs:
                        seen_pdfs.add(loc)
                        discovered.append(loc)
                        added_here += 1

        print(f"  Found {added_here} new PDF link(s)")
        if delay > 0:
            time.sleep(delay)

    return discovered


def main():
    ap = argparse.ArgumentParser(
        description="Zero-API-key crawler for discovering federal signed PDF URLs."
    )
    ap.add_argument(
        "-o",
        "--out",
        type=Path,
        default=Path("locust/urls_crawled.txt"),
        help="Output plaintext file for discovered URLs (default: locust/urls_crawled.txt)",
    )
    ap.add_argument(
        "--seed",
        action="append",
        dest="seeds",
        help="Index HTML URL to crawl (repeatable; defaults to built-in federal deviation/CMA indexes)",
    )
    ap.add_argument(
        "--sitemap",
        action="append",
        dest="sitemaps",
        default=[],
        help="XML sitemap URL to parse for .pdf entries (repeatable)",
    )
    ap.add_argument(
        "--depth",
        type=int,
        default=1,
        help="Link-following depth on same-host HTML pages (default: 1)",
    )
    ap.add_argument(
        "--max-pages",
        type=int,
        default=60,
        help="Maximum HTML pages to crawl per run (default: 60)",
    )
    ap.add_argument(
        "--match",
        default=None,
        help="Optional regex filter that discovered PDF URLs must match (e.g., 'Deviation|CMA|SAA')",
    )
    ap.add_argument(
        "--exclude-from",
        type=Path,
        action="append",
        default=[],
        help="Existing URL list file(s) to deduplicate against (repeatable)",
    )
    ap.add_argument(
        "--ca-bundle",
        default=None,
        help="Optional PEM bundle or directory of extra trusted CAs",
    )
    ap.add_argument(
        "-k",
        "--insecure",
        action="store_true",
        help="Disable TLS verification if needed for legacy hosts",
    )
    ap.add_argument(
        "--delay",
        type=float,
        default=0.5,
        help="Seconds to pause between page requests (default: 0.5)",
    )
    ap.add_argument(
        "--timeout",
        type=int,
        default=20,
        help="HTTP request timeout in seconds (default: 20)",
    )
    args = ap.parse_args()

    try:
        ssl_ctx = build_ssl_context(args.ca_bundle, args.insecure)
    except FileNotFoundError as e:
        ap.error(str(e))
    url_filter = re.compile(args.match, re.IGNORECASE) if args.match else None
    seen_pdfs = load_existing_urls([*args.exclude_from, args.out])

    new_urls: list[str] = []

    if args.sitemaps:
        sitemap_urls = [
            f"{s.rstrip('/')}/sitemap.xml" if urllib.parse.urlsplit(s).path in ("", "/") else s
            for s in args.sitemaps
        ]
        new_urls.extend(
            crawl_sitemaps(
                sitemap_urls,
                url_filter=url_filter,
                seen_pdfs=seen_pdfs,
                ssl_context=ssl_ctx,
                delay=args.delay,
                timeout=args.timeout,
            )
        )

    seeds = args.seeds if args.seeds else ([] if args.sitemaps else DEFAULT_SEEDS)
    if seeds:
        new_urls.extend(
            crawl_html_seeds(
                seeds,
                max_depth=args.depth,
                max_pages=args.max_pages,
                url_filter=url_filter,
                seen_pdfs=seen_pdfs,
                ssl_context=ssl_ctx,
                delay=args.delay,
                timeout=args.timeout,
            )
        )

    args.out.parent.mkdir(parents=True, exist_ok=True)
    if new_urls:
        prefix = ""
        if args.out.is_file() and args.out.stat().st_size > 0:
            existing_bytes = args.out.read_bytes()
            if not existing_bytes.endswith(b"\n"):
                prefix = "\n"
        with args.out.open("a", encoding="utf-8") as f:
            f.write(prefix + "\n".join(new_urls) + "\n")
    elif not args.out.exists():
        args.out.touch()

    print(f"\nDone: discovered {len(new_urls)} new PDF URL(s) -> {args.out}")


if __name__ == "__main__":
    main()

