/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.impl;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

import org.coode.suggestor.api.FillerSuggestor;
import org.coode.suggestor.api.PropertySuggestor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/** Implementation binding. */
public class SuggestorFactory {

    private final OWLReasoner r;

    /**
     * @param r
     *        reasoner to use
     */
    public SuggestorFactory(OWLReasoner r) {
        checkNotNull(r, "Reasoner cannot be null");
        this.r = r;
    }

    /** @return new property suggestor */
    public final PropertySuggestor getPropertySuggestor() {
        return new PropertySuggestorImpl(r);
    }

    /** @return new filler suggestor */
    public final FillerSuggestor getFillerSuggestor() {
        return new FillerSuggestorImpl(r);
    }
}
