FROM python:3.13-alpine

RUN adduser -D -u 1000 vss \
  && mkdir -p /opt/observability \
  && chown -R 1000:1000 /opt/observability

COPY server.py /opt/observability/server.py
RUN chmod 755 /opt/observability/server.py

USER 1000
WORKDIR /opt/observability

ENV PORT=8099 \
    BIND_HOST=0.0.0.0 \
    VSS_BASE_URL=http://127.0.0.1:8000 \
    VSS_TLS_VERIFY=true \
    GRAPH_CACHE_SECONDS=60 \
    STATUS_CACHE_SECONDS=3

EXPOSE 8099

HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1:${PORT}/healthz || exit 1

ENTRYPOINT ["python3", "/opt/observability/server.py"]
