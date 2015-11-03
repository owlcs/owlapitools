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

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

import org.coode.suggestor.api.PropertySanctionRule;
import org.coode.suggestor.api.PropertySuggestor;
import org.coode.suggestor.util.RestrictionAccumulator;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * <p>
 * Looks at the direct subclasses to determine which properties are restricted.
 * </p>
 * <p>
 * Sanction is met if for all d where DirectStrictSubClassOf(c, d) if
 * SubClassOf(d, p only x) is asserted (where x is any class expression).
 * </p>
 * <p>
 * NNF is used when evaluating candidate restrictions.
 * </p>
 */
public class CheckSubsStructureSanctionRule implements PropertySanctionRule {

    private OWLReasoner r;

    @Override
    public void setSuggestor(PropertySuggestor ps) {
        r = ps.getReasoner();
    }

    @Override
    public boolean meetsSanction(OWLClassExpression c, OWLObjectPropertyExpression p) {
        for (Node<OWLClass> sub : r.getSubClasses(c, true)) {
            RestrictionAccumulator acc = new RestrictionAccumulator(r);
            for (OWLClass s : asList(sub.entities())) {
                for (OWLClassExpression restr : acc.getRestrictions(s, p)) {
                    restr = restr.getNNF();
                    if (restr instanceof OWLObjectAllValuesFrom) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean meetsSanction(OWLClassExpression c, OWLDataProperty p) {
        for (Node<OWLClass> sub : r.getSubClasses(c, true)) {
            RestrictionAccumulator acc = new RestrictionAccumulator(r);
            for (OWLClass s : asList(sub.entities())) {
                for (OWLClassExpression restr : acc.getRestrictions(s, p)) {
                    restr = restr.getNNF();
                    if (restr instanceof OWLDataAllValuesFrom) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
