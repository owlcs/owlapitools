/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.Searcher;

/**
 * Accumulates all restrictions made on a class and its ancestors along the
 * given property (and its descendants).
 */
public class RestrictionAccumulator {

    protected final OWLReasoner r;

    /**
     * @param r
     *        reasoner to use
     */
    public RestrictionAccumulator(OWLReasoner r) {
        this.r = r;
    }

    /**
     * @param cls
     *        cls
     * @param prop
     *        prop
     * @return restrictions
     */
    public Set<OWLRestriction> getRestrictions(OWLClassExpression cls,
            OWLPropertyExpression prop) {
        return accummulateRestrictions(cls, prop, null);
    }

    /**
     * @param cls
     *        class
     * @param prop
     *        property
     * @param type
     *        type
     * @param <T>
     *        type
     * @return set of restrictions
     */
    @SuppressWarnings("unchecked")
    public <T extends OWLRestriction> Set<T> getRestrictions(
            OWLClassExpression cls, OWLPropertyExpression prop, Class<T> type) {
        Set<T> results = new HashSet<>();
        for (OWLRestriction restr : accummulateRestrictions(cls, prop, type)) {
            results.add((T) restr);
        }
        return results;
    }

    protected Set<OWLRestriction> accummulateRestrictions(
            OWLClassExpression cls, OWLPropertyExpression prop,
            Class<? extends OWLRestriction> type) {
        Set<OWLClass> relevantClasses = r.getSuperClasses(cls, false)
                .getFlattened();
        RestrictionVisitor v = getVisitor(prop, type);
        if (!cls.isAnonymous()) {
            relevantClasses.add(cls.asOWLClass());
        } else {
            cls.accept(v);
        }
        final OWLOntology rootOnt = r.getRootOntology();
        final Set<OWLOntology> onts = rootOnt.getImportsClosure();
        for (OWLClass ancestor : relevantClasses) {
            for (OWLOntology ont : onts) {
                Collection<OWLClassExpression> superclasses = Searcher.sup(ont
                        .getSubClassAxiomsForSubClass(ancestor));
                for (OWLClassExpression restr : superclasses) {
                    restr.accept(v);
                }
                Collection<OWLClassExpression> equivalent = Searcher
                        .equivalent(ont.getEquivalentClassesAxioms(ancestor));
                for (OWLClassExpression restr : equivalent) {
                    restr.accept(v);
                }
            }
        }
        return v.restrs;
    }

    protected RestrictionVisitor getVisitor(OWLPropertyExpression prop,
            Class<? extends OWLRestriction> type) {
        return new RestrictionVisitor(r, prop, type);
    }
}
