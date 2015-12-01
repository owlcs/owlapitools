/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.manchester.cs.owl.owlapi.alternateimpls;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.Version;

/** @author ignazio a threadsafe wrapper for OWLReasoners */
public class ThreadSafeOWLReasoner implements OWLReasoner {

    private final OWLReasoner delegate;
    private boolean log = true;

    /**
     * @param reasoner
     *        the reasoner to wrap
     * @param log
     *        true if logging is required
     */
    public ThreadSafeOWLReasoner(OWLReasoner reasoner, boolean log) {
        this(reasoner);
        this.log = log;
    }

    /**
     * @param reasoner
     *        the reasoner to wrap
     */
    public ThreadSafeOWLReasoner(OWLReasoner reasoner) {
        checkNotNull(reasoner, "The input reasoner cannot be null");
        delegate = reasoner;
    }

    private void log(String s, Object... objects) {
        if (log) {
            System.out.print(Thread.currentThread().getName()
                + " reasoner." + s + (objects.length == 0 ? "" : Arrays.toString(objects)) + " ... ");
        }
    }

    private <T> T logresult(T t) {
        if (log) {
            System.out.println(t);
            System.out.flush();
        }
        return t;
    }

    @Override
    public String getReasonerName() {
        synchronized (delegate) {
            log("getReasonerName()");
            return logresult(delegate.getReasonerName());
        }
    }

    @Override
    public Version getReasonerVersion() {
        synchronized (delegate) {
            log("getReasonerVersion()");
            return logresult(delegate.getReasonerVersion());
        }
    }

    @Override
    public BufferingMode getBufferingMode() {
        synchronized (delegate) {
            log("getBufferingMode()");
            return logresult(delegate.getBufferingMode());
        }
    }

    @Override
    public void flush() {
        synchronized (delegate) {
            log("flush()");
            delegate.flush();
        }
    }

    @Override
    public List<OWLOntologyChange> getPendingChanges() {
        synchronized (delegate) {
            log("getPendingChanges()");
            return logresult(delegate.getPendingChanges());
        }
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        synchronized (delegate) {
            log("getPendingAxiomAdditions()");
            return logresult(delegate.getPendingAxiomAdditions());
        }
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        synchronized (delegate) {
            log("getPendingAxiomRemovals()");
            return logresult(delegate.getPendingAxiomRemovals());
        }
    }

    @Override
    public OWLOntology getRootOntology() {
        synchronized (delegate) {
            log("getRootOntology()");
            return logresult(delegate.getRootOntology());
        }
    }

    @Override
    public void interrupt() {
        synchronized (delegate) {
            log("interrupt()");
            delegate.interrupt();
        }
    }

    @Override
    public void precomputeInferences(InferenceType... inferenceTypes)
        throws ReasonerInterruptedException, TimeOutException,
        InconsistentOntologyException {
        synchronized (delegate) {
            log("precomputeInferences()", inferenceTypes);
            delegate.precomputeInferences(inferenceTypes);
        }
    }

    @Override
    public boolean isPrecomputed(InferenceType inferenceType) {
        synchronized (delegate) {
            log("isPrecomputed() " + inferenceType);
            return logresult(delegate.isPrecomputed(inferenceType));
        }
    }

    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        synchronized (delegate) {
            log("getPrecomputableInferenceTypes()");
            return logresult(delegate.getPrecomputableInferenceTypes());
        }
    }

    @Override
    public boolean isConsistent() throws ReasonerInterruptedException,
        TimeOutException {
        synchronized (delegate) {
            log("isConsistent()");
            return logresult(delegate.isConsistent());
        }
    }

    @Override
    public boolean isSatisfiable(OWLClassExpression classExpression)
        throws ReasonerInterruptedException, TimeOutException,
        ClassExpressionNotInProfileException, FreshEntitiesException,
        InconsistentOntologyException {
        synchronized (delegate) {
            log("isSatisfiable()", classExpression);
            return logresult(delegate.isSatisfiable(classExpression));
        }
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses()
        throws ReasonerInterruptedException, TimeOutException,
        InconsistentOntologyException {
        synchronized (delegate) {
            log("getUnsatisfiableClasses()");
            return logresult(delegate.getUnsatisfiableClasses());
        }
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom)
        throws ReasonerInterruptedException,
        UnsupportedEntailmentTypeException, TimeOutException,
        AxiomNotInProfileException, FreshEntitiesException,
        InconsistentOntologyException {
        synchronized (delegate) {
            log("isEntailed()", axiom);
            return logresult(delegate.isEntailed(axiom));
        }
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> axioms)
        throws ReasonerInterruptedException,
        UnsupportedEntailmentTypeException, TimeOutException,
        AxiomNotInProfileException, FreshEntitiesException,
        InconsistentOntologyException {
        synchronized (delegate) {
            log("isEntailed()", axioms);
            return logresult(delegate.isEntailed(axioms));
        }
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        synchronized (delegate) {
            log("isEntailmentCheckingSupported()", axiomType);
            return logresult(delegate.isEntailmentCheckingSupported(axiomType));
        }
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
        synchronized (delegate) {
            log("getTopClassNode()");
            return logresult(delegate.getTopClassNode());
        }
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        synchronized (delegate) {
            log("getBottomClassNode()");
            return logresult(delegate.getBottomClassNode());
        }
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct)
        throws ReasonerInterruptedException, TimeOutException,
        FreshEntitiesException, InconsistentOntologyException,
        ClassExpressionNotInProfileException {
        synchronized (delegate) {
            log("getSubClasses()", ce, direct);
            return logresult(delegate.getSubClasses(ce, direct));
        }
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce,
        boolean direct) throws InconsistentOntologyException,
            ClassExpressionNotInProfileException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getSuperClasses()", ce, direct);
            return logresult(delegate.getSuperClasses(ce, direct));
        }
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce)
        throws InconsistentOntologyException,
        ClassExpressionNotInProfileException, FreshEntitiesException,
        ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getEquivalentClasses()", ce);
            return logresult(delegate.getEquivalentClasses(ce));
        }
    }

    @Override
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce)
        throws ReasonerInterruptedException, TimeOutException,
        FreshEntitiesException, InconsistentOntologyException {
        synchronized (delegate) {
            log("getDisjointClasses()", ce);
            return logresult(delegate.getDisjointClasses(ce));
        }
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        synchronized (delegate) {
            log("getTopObjectPropertyNode()");
            return logresult(delegate.getTopObjectPropertyNode());
        }
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        synchronized (delegate) {
            log("getBottomObjectPropertyNode()");
            return logresult(delegate.getBottomObjectPropertyNode());
        }
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
        OWLObjectPropertyExpression pe, boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getSubObjectProperties()", pe, direct);
            return logresult(delegate.getSubObjectProperties(pe, direct));
        }
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
        OWLObjectPropertyExpression pe, boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getSuperObjectProperties()", pe, direct);
            return logresult(delegate.getSuperObjectProperties(pe, direct));
        }
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
        OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getEquivalentObjectProperties()", pe);
            return logresult(delegate.getEquivalentObjectProperties(pe));
        }
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
        OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getDisjointObjectProperties()", pe);
            return logresult(delegate.getDisjointObjectProperties(pe));
        }
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
        OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getInverseObjectProperties()", pe);
            return logresult(delegate.getInverseObjectProperties(pe));
        }
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(
        OWLObjectPropertyExpression pe, boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getObjectPropertyDomains()", pe, direct);
            return logresult(delegate.getObjectPropertyDomains(pe, direct));
        }
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(
        OWLObjectPropertyExpression pe, boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getObjectPropertyRanges() ", pe, direct);
            return logresult(delegate.getObjectPropertyRanges(pe, direct));
        }
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        synchronized (delegate) {
            log("getTopDataPropertyNode()");
            return logresult(delegate.getTopDataPropertyNode());
        }
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        synchronized (delegate) {
            log("getBottomDataPropertyNode()");
            return logresult(delegate.getBottomDataPropertyNode());
        }
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe,
        boolean direct) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        synchronized (delegate) {
            log("getSubDataProperties()", pe, direct);
            return logresult(delegate.getSubDataProperties(pe, direct));
        }
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe,
        boolean direct) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        synchronized (delegate) {
            log("getSuperDataProperties()", pe, direct);
            return logresult(delegate.getSuperDataProperties(pe, direct));
        }
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe)
        throws InconsistentOntologyException,
        FreshEntitiesException, ReasonerInterruptedException,
        TimeOutException {
        synchronized (delegate) {
            log("getEquivalentDataProperties()", pe);
            return logresult(delegate.getEquivalentDataProperties(pe));
        }
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(
        OWLDataPropertyExpression pe) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        synchronized (delegate) {
            log("getDisjointDataProperties()", pe);
            return logresult(delegate.getDisjointDataProperties(pe));
        }
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe,
        boolean direct) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        synchronized (delegate) {
            log("getDataPropertyDomains()", pe, direct);
            return logresult(delegate.getDataPropertyDomains(pe, direct));
        }
    }

    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct)
        throws InconsistentOntologyException, FreshEntitiesException,
        ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getTypes()", ind, direct);
            return logresult(delegate.getTypes(ind, direct));
        }
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce,
        boolean direct) throws InconsistentOntologyException,
            ClassExpressionNotInProfileException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getInstances()", ce, direct);
            return logresult(delegate.getInstances(ce, direct));
        }
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
        OWLNamedIndividual ind, OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getObjectPropertyValues()", ind, pe);
            return logresult(delegate.getObjectPropertyValues(ind, pe));
        }
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind,
        OWLDataProperty pe) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        synchronized (delegate) {
            log("getDataPropertyValues()", ind, pe);
            return logresult(delegate.getDataPropertyValues(ind, pe));
        }
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind)
        throws InconsistentOntologyException, FreshEntitiesException,
        ReasonerInterruptedException, TimeOutException {
        synchronized (delegate) {
            log("getSameIndividuals()", ind);
            return logresult(delegate.getSameIndividuals(ind));
        }
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
        OWLNamedIndividual ind) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        synchronized (delegate) {
            log("getDifferentIndividuals()", ind);
            return logresult(delegate.getDifferentIndividuals(ind));
        }
    }

    @Override
    public long getTimeOut() {
        synchronized (delegate) {
            log("getTimeOut()");
            return logresult(delegate.getTimeOut());
        }
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        synchronized (delegate) {
            log("getFreshEntityPolicy()");
            return logresult(delegate.getFreshEntityPolicy());
        }
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        synchronized (delegate) {
            log("getIndividualNodeSetPolicy()");
            return logresult(delegate.getIndividualNodeSetPolicy());
        }
    }

    @Override
    public void dispose() {
        synchronized (delegate) {
            log("dispose()");
            delegate.dispose();
        }
    }
}
