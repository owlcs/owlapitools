/*
 * Date: Jan 13, 2012
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2012, Ignazio Palmisano, University of Manchester
 *
 */
package org.semanticweb.owlapitools.threadedreasoner;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.Version;

/**
 * @author ignazio Reasoner wrapper that will spin each call to the underlying
 *         reasoner on an Executor service and try hard to respect timeouts
 */
@SuppressWarnings("boxing")
public class ThreadedReasoner implements OWLReasoner {

    protected final OWLReasoner delegate;
    private final ExecutorService exec = Executors.newFixedThreadPool(1);

    /**
     * @param r
     *        reasoner to wrap
     */
    public ThreadedReasoner(OWLReasoner r) {
        delegate = r;
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
        return delegate.getRootOntology();
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
    public Node<OWLClass> getTopClassNode() {
        return delegate.getTopClassNode();
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        return delegate.getBottomClassNode();
    }

    @Override
    public void interrupt() {
        delegate.interrupt();
        exec.shutdownNow();
    }

    @Override
    public void precomputeInferences(InferenceType... inferenceTypes) {
        threadedRun(() -> delegate.precomputeInferences(inferenceTypes));
    }

    private <T> T threadedRun(Callable<T> r) {
        Future<T> toReturn = exec.submit(r);
        try {
            if (delegate.getTimeOut() > 0) {
                return toReturn.get(delegate.getTimeOut(), TimeUnit.MILLISECONDS);
            } else {
                return toReturn.get();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            interrupt();
            exec.shutdownNow();
            throw new OWLRuntimeException(e);
        } finally {
            toReturn.cancel(true);
        }
    }

    private void threadedRun(Runnable r) {
        Future<?> submit = exec.submit(r);
        try {
            if (delegate.getTimeOut() > 0) {
                submit.get(delegate.getTimeOut(), TimeUnit.MILLISECONDS);
            } else {
                submit.get();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            interrupt();
            exec.shutdownNow();
            throw new OWLRuntimeException(e);
        } finally {
            submit.cancel(true);
        }
    }

    @Override
    public boolean isConsistent() {
        return threadedRun(() -> delegate.isConsistent());
    }

    @Override
    public boolean isSatisfiable(OWLClassExpression classExpression) {
        return threadedRun(() -> delegate.isSatisfiable(classExpression));
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        return threadedRun(() -> delegate.getUnsatisfiableClasses());
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) {
        return threadedRun(() -> delegate.isEntailed(axiom));
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> axioms) {
        return threadedRun(() -> delegate.isEntailed(axioms));
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return threadedRun(() -> delegate.isEntailmentCheckingSupported(axiomType));
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct) {
        return threadedRun(() -> delegate.getSubClasses(ce, direct));
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce, boolean direct) {
        return threadedRun(() -> delegate.getSuperClasses(ce, direct));
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce) {
        return threadedRun(() -> delegate.getEquivalentClasses(ce));
    }

    @Override
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
        return threadedRun(() -> delegate.getDisjointClasses(ce));
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
        return threadedRun(() -> delegate.getSubObjectProperties(pe, direct));
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe,
        boolean direct) {
        return threadedRun(() -> delegate.getSuperObjectProperties(pe, direct));
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression pe) {
        return threadedRun(() -> delegate.getEquivalentObjectProperties(pe));
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression pe) {
        return threadedRun(() -> delegate.getDisjointObjectProperties(pe));
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression pe) {
        return threadedRun(() -> delegate.getInverseObjectProperties(pe));
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct) {
        return threadedRun(() -> delegate.getObjectPropertyDomains(pe, direct));
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression pe, boolean direct) {
        return threadedRun(() -> delegate.getObjectPropertyRanges(pe, direct));
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        return threadedRun(() -> delegate.getTopDataPropertyNode());
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        return threadedRun(() -> delegate.getBottomDataPropertyNode());
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe, boolean direct) {
        return threadedRun(() -> delegate.getSubDataProperties(pe, direct));
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe, boolean direct) {
        return threadedRun(() -> delegate.getSuperDataProperties(pe, direct));
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe) {
        return threadedRun(() -> delegate.getEquivalentDataProperties(pe));
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression pe) {
        return threadedRun(() -> delegate.getDisjointDataProperties(pe));
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe, boolean direct) {
        return threadedRun(() -> delegate.getDataPropertyDomains(pe, direct));
    }

    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) {
        return threadedRun(() -> delegate.getTypes(ind, direct));
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct) {
        return threadedRun(() -> delegate.getInstances(ce, direct));
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual ind, OWLObjectPropertyExpression pe) {
        return threadedRun(() -> delegate.getObjectPropertyValues(ind, pe));
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind, OWLDataProperty pe) {
        return threadedRun(() -> delegate.getDataPropertyValues(ind, pe));
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind) {
        return threadedRun(() -> delegate.getSameIndividuals(ind));
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual ind) {
        return threadedRun(() -> delegate.getDifferentIndividuals(ind));
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
        delegate.dispose();
        exec.shutdownNow();
    }
}
