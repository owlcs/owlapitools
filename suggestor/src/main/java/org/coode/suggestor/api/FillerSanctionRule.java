/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *@author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.api;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;

/**
 * A pluggable way of determining if a certain filler is "interesting" for a
 * given (class, property) pair
 */
public interface FillerSanctionRule {

    /**
     * Called by the suggestor when the rule has been registered (in case the
     * rule requires suggestor methods)
     * 
     * @param fs
     *        the FillerSuggestor
     */
    void setSuggestor(FillerSuggestor fs);

    /**
     * @param c
     *        a class expression
     * @param p
     *        a property
     * @param f
     *        a filler class expression
     * @param <T>
     *        object or data property
     * @return true if f is an "interesting" filler to use in the axiom
     *         SubClassOf(c, p some f)
     */
    <T extends OWLPropertyExpression> boolean meetsSanction(OWLClassExpression c, T p,
        OWLPropertyRange f);
}
