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

import java.util.Set;
import java.util.stream.Stream;

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
        return asSet(accummulateRestrictions(cls, prop, null));
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
    public <T extends OWLRestriction> Set<T> getRestrictions(
        OWLClassExpression cls, OWLPropertyExpression prop, Class<T> type) {
        return asSet(accummulateRestrictions(cls, prop, type));
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
    public <T extends OWLRestriction> Stream<T> accummulateRestrictions(
        OWLClassExpression cls, @Nullable OWLPropertyExpression prop,
        @Nullable Class<T> type) {
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
        return (Stream<T>) v.restrs.stream();
    }

    protected RestrictionVisitor getVisitor(@Nullable OWLPropertyExpression prop,
        @Nullable Class<? extends OWLRestriction> type) {
        return new RestrictionVisitor(r, prop, type);
    }
}
