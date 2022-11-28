package org.keysupport.api.pkix.cache;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Text;

/*<pre>
 * This implementation is specific to a markdown source that tracks CRLs we are interested in:
 *
 * I.e.,
 *
 * - https://raw.githubusercontent.com/GSA/ficam-playbooks/staging/_fpki/2b_pivcas.md
 * </pre>
 */
public class HttpCrlParsingVisitor extends AbstractVisitor {

    private List<URI> crls = null;

    @Override
    public void visit(Text text) {

    	/*
    	 * Get all of the HTTP CRLs from all text nodes
    	 */
    	String possibleUri = text.getLiteral();
    	if (possibleUri.startsWith("http://") && possibleUri.endsWith(".crl")) {
    		addCrlUri(possibleUri);
    	}

        // Descend into children (could be omitted in this case because Text nodes don't have children).
        visitChildren(text);
    }

    public void addCrlUri(String url) {
    	URI crl = URI.create(url);
    	if (null == crls) {
    		crls = new ArrayList<>();
    	}
    	crls.add(crl);
    }

    public List<URI> getCrlUris() {
    	return crls;
    }
}

