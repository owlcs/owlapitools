/*
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * Author: Nick Drummond
 * http://www.cs.man.ac.uk/~drummond/
 * Bio Health Informatics Group
 * The University Of Manchester
 */
package org.coode.suggestor.impl;

import org.coode.suggestor.api.FillerSanctionRule;
import org.coode.suggestor.api.FillerSuggestor;
import org.coode.suggestor.util.ReasonerHelper;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/** Checks if the filler is in the asserted range. */
public class InRangeFillerSanctionRule implements FillerSanctionRule {
    private ReasonerHelper reasonerHelper;

    @Override
    public void setSuggestor(FillerSuggestor fs) {
        reasonerHelper = new ReasonerHelper(fs.getReasoner());
    }

    @Override
    public boolean meetsSanction(OWLClassExpression c, OWLObjectPropertyExpression p,
            OWLClassExpression f) {
        return reasonerHelper.isInAssertedRange(p, f);
    }

    @Override
    public boolean meetsSanction(OWLClassExpression c, OWLDataProperty p, OWLDataRange f) {
        return reasonerHelper.isInAssertedRange(p, f);
    }
}
