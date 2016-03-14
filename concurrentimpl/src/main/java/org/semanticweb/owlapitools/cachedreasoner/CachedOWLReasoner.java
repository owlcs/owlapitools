/*
 * Date: Jan 13, 2012
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2012, Ignazio Palmisano, University of Manchester
 *
 */
package org.semanticweb.owlapitools.cachedreasoner;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;
import static org.semanticweb.owlapitools.cachedreasoner.CacheKeys.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.Version;

/** @author ignazio */
@SuppressWarnings("unchecked")
public class CachedOWLReasoner implements OWLReasoner, OWLOntologyChangeListener {

    protected final OWLReasoner delegate;

    private static final class BoolKey {

        final Object o;
        final boolean b;

        public BoolKey(Object o, boolean b) {
            this.o = o;
            this.b = b;
        }

        @Override
        public int hashCode() {
            return o.hashCode() * (b ? 1 : -1);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return obj instanceof BoolKey && b == ((BoolKey) obj).b && o.equals(((BoolKey) obj).o);
        }
    }

    private static final class RegKey {

        final Object o1;
        final Object o2;

        public RegKey(Object o1, Object o2) {
            this.o1 = o1;
            this.o2 = o2;
        }

        @Override
        public int hashCode() {
            return o1.hashCode() * o2.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return obj instanceof RegKey && o1.equals(((RegKey) obj).o1) && o2.equals(((RegKey) obj).o2);
        }
    }

    static final Object key(Object o1, boolean o2) {
        return new BoolKey(o1, o2);
    }

    static final Object key(Object o1, Object o2) {
        return new RegKey(o1, o2);
    }

    private final static class CachedReasoner {

        public CachedReasoner() {}

        EnumMap<CacheKeys, ConcurrentHashMap<Object, Object>> mainCache = new EnumMap<>(CacheKeys.class);

        public void clear() {
            mainCache.clear();
        }

        public <T> T get(CacheKeys cachekey, Object key, Function<Object, T> c) {
            ConcurrentHashMap<Object, Object> map = mainCache.computeIfAbsent(cachekey, k -> new ConcurrentHashMap<>());
            return (T) map.computeIfAbsent(key, c);
        }
    }

    private final CachedReasoner cache = new CachedReasoner();
    private final OWLOntology rootOntology;

    /**
     * @param reasoner
     *        reasoner
     * @param manager
     *        manager
     */
    public CachedOWLReasoner(OWLReasoner reasoner, OWLOntologyManager manager) {
        checkNotNull(reasoner, "The input reasoner cannot be null");
        delegate = reasoner;
        manager.addOntologyChangeListener(this);
        rootOntology = delegate.getRootOntology();
    }

    @Override
    public String getReasonerName() {
        return delegate.getReasonerName();
    }

    @Override
    public Version getReasonerVersion() {
        return delegate.getReasonerVersion();
    }

    @Override
    public BufferingMode getBufferingMode() {
        return delegate.getBufferingMode();
    }

    @Override
    public void flush() {
        cache.clear();
        delegate.flush();
    }

    @Override
    public List<OWLOntologyChange> getPendingChanges() {
        return delegate.getPendingChanges();
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        return delegate.getPendingAxiomAdditions();
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        return delegate.getPendingAxiomRemovals();
    }

    @Override
    public OWLOntology getRootOntology() {
        return rootOntology;
    }

    @Override
    public void interrupt() {
        cache.clear();
        delegate.interrupt();
    }

    @Override
    public void precomputeInferences(InferenceType... inferenceTypes) {
        cache.clear();
        delegate.precomputeInferences(inferenceTypes);
    }

    @Override
    public boolean isPrecomputed(InferenceType inferenceType) {
        return delegate.isPrecomputed(inferenceType);
    }

    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        return delegate.getPrecomputableInferenceTypes();
    }

    @Override
    public boolean isConsistent() {
        return delegate.isConsistent();
    }

    @Override
    public boolean isSatisfiable(OWLClassExpression ce) {
        return cache.get(issatisfiable, ce, c -> Boolean.valueOf(delegate.isSatisfiable(ce))).booleanValue();
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        return delegate.getUnsatisfiableClasses();
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) {
        return cache.get(isEntailed, axiom, ax -> Boolean.valueOf(delegate.isEntailed(axiom))).booleanValue();
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> axioms) {
        return axioms.stream().allMatch(this::isEntailed);
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return delegate.isEntailmentCheckingSupported(axiomType);
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
        return delegate.getTopClassNode();
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        return delegate.getBottomClassNode();
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct) {
        return cache.get(direct ? subclassesDirect : subclasses, key(ce, direct), c -> delegate.getSubClasses(ce,
            direct));
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce, boolean direct) {
        return cache.get(direct ? superclassesDirect : superclasses, key(ce, direct), c -> delegate.getSuperClasses(ce,
            direct));
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce) {
        return cache.get(equivclasses, ce, c -> delegate.getEquivalentClasses(ce));
    }

    @Override
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
        return cache.get(disjointclasses, ce, c -> delegate.getDisjointClasses(ce));
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        return delegate.getTopObjectPropertyNode();
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        return delegate.getBottomObjectPropertyNode();
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression pe, boolean direct) {
        return cache.get(direct ? subobjectpropertiesDirect : subobjectproperties, key(pe, direct), p -> delegate
            .getSubObjectProperties(pe, direct));
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe,
        boolean direct) {
        return cache.get(direct ? superobjectpropertiesDirect : superobjectproperties, key(pe, direct), p -> delegate
            .getSuperObjectProperties(pe, direct));
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression pe) {
        return cache.get(equivobjectproperties, pe, p -> delegate.getEquivalentObjectProperties(pe));
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression pe) {
        return cache.get(disjointobjectproperties, pe, p -> delegate.getDisjointObjectProperties(pe));
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression pe) {
        return cache.get(inverseobjectproperties, pe, p -> delegate.getInverseObjectProperties(pe));
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct) {
        return cache.get(direct ? objectpropertiesdomainsDirect : objectpropertiesdomains, key(pe, direct),
            p -> delegate.getObjectPropertyDomains(pe, direct));
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression pe, boolean direct) {
        return cache.get(direct ? objectpropertiesrangesDirect : objectpropertiesranges, key(pe, direct), p -> delegate
            .getObjectPropertyRanges(pe, direct));
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        return delegate.getTopDataPropertyNode();
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        return delegate.getBottomDataPropertyNode();
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe, boolean direct) {
        return cache.get(direct ? subdatapropertiesDirect : subdataproperties, key(pe, direct), p -> delegate
            .getSubDataProperties(pe, direct));
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe, boolean direct) {
        return cache.get(direct ? superdatapropertiesDirect : superdataproperties, key(pe, direct), p -> delegate
            .getSuperDataProperties(pe, direct));
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe) {
        return cache.get(equivdataproperties, pe, p -> delegate.getEquivalentDataProperties(pe));
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression pe) {
        return cache.get(disjointdataproperties, pe, p -> delegate.getDisjointDataProperties(pe));
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe, boolean direct) {
        return cache.get(direct ? datapropertiesdomainsDirect : datapropertiesdomains, key(pe, direct), p -> delegate
            .getDataPropertyDomains(pe, direct));
    }

    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) {
        return cache.get(direct ? typesDirect : types, key(ind, direct), i -> delegate.getTypes(ind, direct));
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct) {
        return cache.get(direct ? instancesDirect : instances, key(ce, direct), c -> delegate.getInstances(ce, direct));
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual ind, OWLObjectPropertyExpression pe) {
        return cache.get(objectpropertiesvalues, key(ind, pe), i -> delegate.getObjectPropertyValues(ind, pe));
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind, OWLDataProperty pe) {
        return cache.get(datapropertiesvalues, key(ind, pe), i -> delegate.getDataPropertyValues(ind, pe));
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind) {
        return cache.get(sameindividual, ind, i -> delegate.getSameIndividuals(ind));
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual ind) {
        return cache.get(diffindividual, ind, i -> delegate.getDifferentIndividuals(ind));
    }

    @Override
    public long getTimeOut() {
        return delegate.getTimeOut();
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        return delegate.getFreshEntityPolicy();
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        return delegate.getIndividualNodeSetPolicy();
    }

    @Override
    public void dispose() {
        cache.clear();
        delegate.dispose();
    }

    @Override
    public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
        // only invalidate the caches, the changes are supposed to go to the
        // reasoner via its own listener
        cache.clear();
    }
}
