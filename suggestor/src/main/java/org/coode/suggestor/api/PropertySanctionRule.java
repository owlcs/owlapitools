/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.api;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

/**
 * A pluggable way of determining if a property is "interesting" for a given
 * class.
 */
public interface PropertySanctionRule {

    /**
     * Called by the suggestor when the rule has been registered (in case the
     * rule requires suggestor methods)
     * 
     * @param ps
     *        The PropertySuggestor
     */
    void setSuggestor(PropertySuggestor ps);

    /**
     * @param c
     *        a class expression
     * @param p
     *        a property
     * @param <T>
     *        object or data property
     * @return true if p is an "interesting" property to use in the axiom
     *         SubClassOf(c, p some x)
     */
    <T extends OWLPropertyExpression> boolean meetsSanction(OWLClassExpression c, T p);
}
