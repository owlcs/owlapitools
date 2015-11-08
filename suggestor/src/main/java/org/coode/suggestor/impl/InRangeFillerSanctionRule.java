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

import org.coode.suggestor.api.FillerSanctionRule;
import org.coode.suggestor.api.FillerSuggestor;
import org.coode.suggestor.util.ReasonerHelper;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;

/** Checks if the filler is in the asserted range. */
public class InRangeFillerSanctionRule implements FillerSanctionRule {

    private ReasonerHelper reasonerHelper;

    @Override
    public void setSuggestor(FillerSuggestor fs) {
        reasonerHelper = new ReasonerHelper(fs.getReasoner());
    }

    @Override
    public <T extends OWLPropertyExpression> boolean meetsSanction(OWLClassExpression c, T p, OWLPropertyRange f) {
        return reasonerHelper.isInAssertedRange(p, f);
    }
}
