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

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asSet;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.parameters.Imports;
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
        OWLClassExpression cls, @Nullable OWLPropertyExpression prop,
        @Nullable Class<? extends OWLRestriction> type) {
        Set<OWLClass> relevantClasses = asSet(r.getSuperClasses(cls, false).entities());
        RestrictionVisitor v = getVisitor(prop, type);
        if (!cls.isAnonymous()) {
            relevantClasses.add(cls.asOWLClass());
        } else {
            cls.accept(v);
        }
        Imports.INCLUDED.stream(r.getRootOntology()).forEach(o -> {
            for (OWLClass ancestor : relevantClasses) {
                Searcher.sup(o.subClassAxiomsForSubClass(ancestor), OWLClassExpression.class).forEach(restr -> restr
                    .accept(v));
                Searcher.equivalent(o.equivalentClassesAxioms(ancestor), OWLClassExpression.class).forEach(
                    restr -> restr.accept(v));
            }
        });
        return v.restrs;
    }

    protected RestrictionVisitor getVisitor(@Nullable OWLPropertyExpression prop,
        @Nullable Class<? extends OWLRestriction> type) {
        return new RestrictionVisitor(r, prop, type);
    }
}
