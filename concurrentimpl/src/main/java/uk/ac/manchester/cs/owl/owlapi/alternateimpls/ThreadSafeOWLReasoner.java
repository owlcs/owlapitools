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

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author ignazio a threadsafe wrapper for OWLReasoners */
public class ThreadSafeOWLReasoner implements OWLReasoner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadSafeOWLReasoner.class);
    private final OWLReasoner delegate;

    /**
     * @param reasoner
     *        the reasoner to wrap
     */
    public ThreadSafeOWLReasoner(OWLReasoner reasoner) {
        checkNotNull(reasoner, "The input reasoner cannot be null");
        delegate = reasoner;
    }

    private static void log(String s, Object object, boolean b) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(Thread.currentThread().getName() + " reasoner." + s + object + ", " + b + " ... ");
        }
    }

    private static void log(String s, Object object1, Object object2) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(Thread.currentThread().getName() + " reasoner." + s + object1 + ", " + object2 + " ... ");
        }
    }

    private static void log(String s, Object object) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(Thread.currentThread().getName() + " reasoner." + s + object + " ... ");
        }
    }

    private static void log(String s) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(Thread.currentThread().getName() + " reasoner." + s + " ... ");
        }
    }

    private static <T> T logresult(T t) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(t.toString());
        }
        return t;
    }

    private static boolean logresult(boolean t) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(Boolean.toString(t));
        }
        return t;
    }

    @Override
    public synchronized String getReasonerName() {
        log("getReasonerName()");
        return logresult(delegate.getReasonerName());
    }

    @Override
    public synchronized Version getReasonerVersion() {
        log("getReasonerVersion()");
        return logresult(delegate.getReasonerVersion());
    }

    @Override
    public synchronized BufferingMode getBufferingMode() {
        log("getBufferingMode()");
        return logresult(delegate.getBufferingMode());
    }

    @Override
    public synchronized void flush() {
        log("flush()");
        delegate.flush();
    }

    @Override
    public synchronized List<OWLOntologyChange> getPendingChanges() {
        log("getPendingChanges()");
        return logresult(delegate.getPendingChanges());
    }

    @Override
    public synchronized Set<OWLAxiom> getPendingAxiomAdditions() {
        log("getPendingAxiomAdditions()");
        return logresult(delegate.getPendingAxiomAdditions());
    }

    @Override
    public synchronized Set<OWLAxiom> getPendingAxiomRemovals() {
        log("getPendingAxiomRemovals()");
        return logresult(delegate.getPendingAxiomRemovals());
    }

    @Override
    public synchronized OWLOntology getRootOntology() {
        log("getRootOntology()");
        return logresult(delegate.getRootOntology());
    }

    @Override
    public synchronized void interrupt() {
        log("interrupt()");
        delegate.interrupt();
    }

    @Override
    public synchronized void precomputeInferences(InferenceType... inferenceTypes) {
        log("precomputeInferences()", inferenceTypes);
        delegate.precomputeInferences(inferenceTypes);
    }

    @Override
    public synchronized boolean isPrecomputed(InferenceType inferenceType) {
        log("isPrecomputed() " + inferenceType);
        return logresult(delegate.isPrecomputed(inferenceType));
    }

    @Override
    public synchronized Set<InferenceType> getPrecomputableInferenceTypes() {
        log("getPrecomputableInferenceTypes()");
        return logresult(delegate.getPrecomputableInferenceTypes());
    }

    @Override
    public synchronized boolean isConsistent() {
        log("isConsistent()");
        return logresult(delegate.isConsistent());
    }

    @Override
    public synchronized boolean isSatisfiable(OWLClassExpression classExpression) {
        log("isSatisfiable()", classExpression);
        return logresult(delegate.isSatisfiable(classExpression));
    }

    @Override
    public synchronized Node<OWLClass> getUnsatisfiableClasses() {
        log("getUnsatisfiableClasses()");
        return logresult(delegate.getUnsatisfiableClasses());
    }

    @Override
    public synchronized boolean isEntailed(OWLAxiom axiom) {
        log("isEntailed()", axiom);
        return logresult(delegate.isEntailed(axiom));
    }

    @Override
    public synchronized boolean isEntailed(Set<? extends OWLAxiom> axioms) {
        log("isEntailed()", axioms);
        return logresult(delegate.isEntailed(axioms));
    }

    @Override
    public synchronized boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        log("isEntailmentCheckingSupported()", axiomType);
        return logresult(delegate.isEntailmentCheckingSupported(axiomType));
    }

    @Override
    public synchronized Node<OWLClass> getTopClassNode() {
        log("getTopClassNode()");
        return logresult(delegate.getTopClassNode());
    }

    @Override
    public synchronized Node<OWLClass> getBottomClassNode() {
        log("getBottomClassNode()");
        return logresult(delegate.getBottomClassNode());
    }

    @Override
    public synchronized NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct) {
        log("getSubClasses()", ce, direct);
        return logresult(delegate.getSubClasses(ce, direct));
    }

    @Override
    public synchronized NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce, boolean direct) {
        log("getSuperClasses()", ce, direct);
        return logresult(delegate.getSuperClasses(ce, direct));
    }

    @Override
    public synchronized Node<OWLClass> getEquivalentClasses(OWLClassExpression ce) {
        log("getEquivalentClasses()", ce);
        return logresult(delegate.getEquivalentClasses(ce));
    }

    @Override
    public synchronized NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
        log("getDisjointClasses()", ce);
        return logresult(delegate.getDisjointClasses(ce));
    }

    @Override
    public synchronized Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        log("getTopObjectPropertyNode()");
        return logresult(delegate.getTopObjectPropertyNode());
    }

    @Override
    public synchronized Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        log("getBottomObjectPropertyNode()");
        return logresult(delegate.getBottomObjectPropertyNode());
    }

    @Override
    public synchronized NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression pe,
        boolean direct) {
        log("getSubObjectProperties()", pe, direct);
        return logresult(delegate.getSubObjectProperties(pe, direct));
    }

    @Override
    public synchronized NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe,
        boolean direct) {
        log("getSuperObjectProperties()", pe, direct);
        return logresult(delegate.getSuperObjectProperties(pe, direct));
    }

    @Override
    public synchronized Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
        OWLObjectPropertyExpression pe) {
        log("getEquivalentObjectProperties()", pe);
        return logresult(delegate.getEquivalentObjectProperties(pe));
    }

    @Override
    public synchronized NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
        OWLObjectPropertyExpression pe) {
        log("getDisjointObjectProperties()", pe);
        return logresult(delegate.getDisjointObjectProperties(pe));
    }

    @Override
    public synchronized Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression pe) {
        log("getInverseObjectProperties()", pe);
        return logresult(delegate.getInverseObjectProperties(pe));
    }

    @Override
    public synchronized NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct) {
        log("getObjectPropertyDomains()", pe, direct);
        return logresult(delegate.getObjectPropertyDomains(pe, direct));
    }

    @Override
    public synchronized NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression pe, boolean direct) {
        log("getObjectPropertyRanges() ", pe, direct);
        return logresult(delegate.getObjectPropertyRanges(pe, direct));
    }

    @Override
    public synchronized Node<OWLDataProperty> getTopDataPropertyNode() {
        log("getTopDataPropertyNode()");
        return logresult(delegate.getTopDataPropertyNode());
    }

    @Override
    public synchronized Node<OWLDataProperty> getBottomDataPropertyNode() {
        log("getBottomDataPropertyNode()");
        return logresult(delegate.getBottomDataPropertyNode());
    }

    @Override
    public synchronized NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe, boolean direct) {
        log("getSubDataProperties()", pe, direct);
        return logresult(delegate.getSubDataProperties(pe, direct));
    }

    @Override
    public synchronized NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe, boolean direct) {
        log("getSuperDataProperties()", pe, direct);
        return logresult(delegate.getSuperDataProperties(pe, direct));
    }

    @Override
    public synchronized Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe) {
        log("getEquivalentDataProperties()", pe);
        return logresult(delegate.getEquivalentDataProperties(pe));
    }

    @Override
    public synchronized NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression pe) {
        log("getDisjointDataProperties()", pe);
        return logresult(delegate.getDisjointDataProperties(pe));
    }

    @Override
    public synchronized NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe, boolean direct) {
        log("getDataPropertyDomains()", pe, direct);
        return logresult(delegate.getDataPropertyDomains(pe, direct));
    }

    @Override
    public synchronized NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) {
        log("getTypes()", ind, direct);
        return logresult(delegate.getTypes(ind, direct));
    }

    @Override
    public synchronized NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct) {
        log("getInstances()", ce, direct);
        return logresult(delegate.getInstances(ce, direct));
    }

    @Override
    public synchronized NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual ind,
        OWLObjectPropertyExpression pe) {
        log("getObjectPropertyValues()", ind, pe);
        return logresult(delegate.getObjectPropertyValues(ind, pe));
    }

    @Override
    public synchronized Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind, OWLDataProperty pe) {
        log("getDataPropertyValues()", ind, pe);
        return logresult(delegate.getDataPropertyValues(ind, pe));
    }

    @Override
    public synchronized Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind) {
        log("getSameIndividuals()", ind);
        return logresult(delegate.getSameIndividuals(ind));
    }

    @Override
    public synchronized NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual ind) {
        log("getDifferentIndividuals()", ind);
        return logresult(delegate.getDifferentIndividuals(ind));
    }

    @Override
    public synchronized long getTimeOut() {
        log("getTimeOut()");
        return logresult(Long.valueOf(delegate.getTimeOut())).longValue();
    }

    @Override
    public synchronized FreshEntityPolicy getFreshEntityPolicy() {
        log("getFreshEntityPolicy()");
        return logresult(delegate.getFreshEntityPolicy());
    }

    @Override
    public synchronized IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        log("getIndividualNodeSetPolicy()");
        return logresult(delegate.getIndividualNodeSetPolicy());
    }

    @Override
    public synchronized void dispose() {
        log("dispose()");
        delegate.dispose();
    }
}
