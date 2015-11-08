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

import org.coode.suggestor.api.PropertySanctionRule;
import org.coode.suggestor.api.PropertySuggestor;
import org.coode.suggestor.util.RestrictionAccumulator;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Looks at the direct subclasses to determine which properties are restricted.
 * <br>
 * Sanction is met if for all d where DirectStrictSubClassOf(c, d) if
 * SubClassOf(d, p only x) is asserted (where x is any class expression). <br>
 * NNF is used when evaluating candidate restrictions.
 */
public class CheckSubsStructureSanctionRule implements PropertySanctionRule {

    private OWLReasoner r;

    @Override
    public void setSuggestor(PropertySuggestor ps) {
        r = ps.getReasoner();
    }

    @Override
    public <T extends OWLPropertyExpression> boolean meetsSanction(OWLClassExpression c, T p) {
        Class<? extends OWLRestriction> class1 = p.isOWLDataProperty() ? OWLDataAllValuesFrom.class
            : OWLObjectAllValuesFrom.class;
        return r.getSubClasses(c, true).nodes().anyMatch(sub -> match(c, p, class1, sub));
    }

    protected <T extends OWLPropertyExpression> boolean match(OWLClassExpression c, T p,
        Class<? extends OWLRestriction> class1, Node<OWLClass> sub) {
        RestrictionAccumulator acc = new RestrictionAccumulator(r);
        return sub.entities().flatMap(s -> acc.accummulateRestrictions(c, p, class1)).anyMatch(restr -> class1
            .isInstance(restr.getNNF()));
    }
}
