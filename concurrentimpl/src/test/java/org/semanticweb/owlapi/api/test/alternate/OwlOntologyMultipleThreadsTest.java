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
package org.semanticweb.owlapi.api.test.alternate;

import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.threadsafe.ThreadSafeOWLManager;

import uk.ac.manchester.cs.owl.owlapi.alternateimpls.test.MultiThreadChecker;
import uk.ac.manchester.cs.owl.owlapi.alternateimpls.test.TestMultithreadCallBack;

@SuppressWarnings("javadoc")
public class OwlOntologyMultipleThreadsTest {

    private static class TestCallback implements TestMultithreadCallBack {

        private final OWLOntology o1;
        private final OWLOntology o2;

        TestCallback(OWLOntology o1, OWLOntology o2) {
            this.o1 = o1;
            this.o2 = o2;
        }

        @Override
        public void execute() throws Exception {
            for (int index = 0; index < 100; index++) {
                o1.isEmpty();
                o1.getAnnotations();
                o1.getSignature(true);
                o1.getSignature(false);
                Set<OWLEntity> entities = o1.getSignature();
                o1.getOWLOntologyManager();
                o1.getOntologyID();
                o1.isAnonymous();
                o1.getDirectImportsDocuments();
                o1.getDirectImports();
                o1.getImports();
                o1.getImportsClosure();
                o1.getImportsDeclarations();
                o1.getAxioms();
                o1.getAxiomCount();
                Set<OWLClass> classes = o1.getClassesInSignature();
                o1.getClassesInSignature(true);
                o1.getClassesInSignature(false);
                Set<OWLObjectProperty> objectProperties = o1
                        .getObjectPropertiesInSignature(true);
                o1.getObjectPropertiesInSignature(false);
                o1.getObjectPropertiesInSignature();
                Set<OWLDataProperty> dataProperties = o1
                        .getDataPropertiesInSignature();
                o1.getDataPropertiesInSignature(true);
                o1.getDataPropertiesInSignature(false);
                Set<OWLNamedIndividual> individuals = o1
                        .getIndividualsInSignature();
                o1.getIndividualsInSignature(true);
                o1.getIndividualsInSignature(false);
                Set<OWLAnonymousIndividual> anonIndividuals = o1
                        .getReferencedAnonymousIndividuals(false);
                o1.getDatatypesInSignature();
                o1.getDatatypesInSignature(true);
                o1.getDatatypesInSignature(false);
                o1.getAnnotationPropertiesInSignature(false);
                for (OWLObjectProperty o : objectProperties) {
                    o1.getAxioms(o, false);
                    o1.containsObjectPropertyInSignature(o.getIRI(), false);
                    o1.containsObjectPropertyInSignature(o.getIRI(), true);
                    o1.containsObjectPropertyInSignature(o.getIRI(), false);
                    o1.getObjectSubPropertyAxiomsForSubProperty(o);
                    o1.getObjectSubPropertyAxiomsForSuperProperty(o);
                    o1.getObjectPropertyDomainAxioms(o);
                    o1.getObjectPropertyRangeAxioms(o);
                    o1.getInverseObjectPropertyAxioms(o);
                    o1.getEquivalentObjectPropertiesAxioms(o);
                    o1.getDisjointObjectPropertiesAxioms(o);
                    o1.getFunctionalObjectPropertyAxioms(o);
                    o1.getInverseFunctionalObjectPropertyAxioms(o);
                    o1.getSymmetricObjectPropertyAxioms(o);
                    o1.getAsymmetricObjectPropertyAxioms(o);
                    o1.getReflexiveObjectPropertyAxioms(o);
                    o1.getIrreflexiveObjectPropertyAxioms(o);
                    o1.getTransitiveObjectPropertyAxioms(o);
                }
                for (OWLClass c : classes) {
                    o1.getAxioms(c, false);
                    o1.containsClassInSignature(c.getIRI(), false);
                    o1.containsClassInSignature(c.getIRI(), true);
                    o1.containsClassInSignature(c.getIRI(), false);
                    o1.getSubClassAxiomsForSubClass(c);
                    o1.getSubClassAxiomsForSuperClass(c);
                    o1.getEquivalentClassesAxioms(c);
                    o1.getDisjointClassesAxioms(c);
                    o1.getDisjointUnionAxioms(c);
                    o1.getHasKeyAxioms(c);
                    o1.getClassAssertionAxioms(c);
                }
                for (OWLDataProperty p : dataProperties) {
                    o1.getAxioms(p, false);
                    o1.containsDataPropertyInSignature(p.getIRI(), false);
                    o1.containsDataPropertyInSignature(p.getIRI(), true);
                    o1.containsDataPropertyInSignature(p.getIRI(), false);
                    o1.getDataSubPropertyAxiomsForSubProperty(p);
                    o1.getDataSubPropertyAxiomsForSuperProperty(p);
                    o1.getDataPropertyDomainAxioms(p);
                    o1.getDataPropertyRangeAxioms(p);
                    o1.getEquivalentDataPropertiesAxioms(p);
                    o1.getDisjointDataPropertiesAxioms(p);
                    o1.getFunctionalDataPropertyAxioms(p);
                }
                for (OWLNamedIndividual i : individuals) {
                    o1.getAxioms(i, false);
                    o1.containsIndividualInSignature(i.getIRI(), false);
                    o1.containsIndividualInSignature(i.getIRI(), true);
                    o1.containsIndividualInSignature(i.getIRI(), false);
                    o1.getClassAssertionAxioms(i);
                    o1.getDataPropertyAssertionAxioms(i);
                    o1.getObjectPropertyAssertionAxioms(i);
                    o1.getNegativeObjectPropertyAssertionAxioms(i);
                    o1.getNegativeDataPropertyAssertionAxioms(i);
                    o1.getSameIndividualAxioms(i);
                    o1.getDifferentIndividualAxioms(i);
                }
                for (OWLAnonymousIndividual i : anonIndividuals) {
                    o1.getAxioms(i, false);
                }
                for (AxiomType<?> ax : AxiomType.AXIOM_TYPES) {
                    o1.getAxioms(ax);
                    o1.getAxioms(ax, true);
                    o1.getAxioms(ax, false);
                }
                for (OWLDatatype t : o1.getDatatypesInSignature()) {
                    o1.getAxioms(t, false);
                    o1.containsDatatypeInSignature(t.getIRI(), false);
                    o1.containsDatatypeInSignature(t.getIRI(), true);
                    o1.containsDatatypeInSignature(t.getIRI(), false);
                    o1.getDatatypeDefinitions(t);
                }
                for (OWLAnnotationProperty p : o1
                        .getAnnotationPropertiesInSignature(false)) {
                    o1.getAxioms(p, false);
                    o1.containsAnnotationPropertyInSignature(p.getIRI(), false);
                    o1.containsAnnotationPropertyInSignature(p.getIRI(), true);
                    o1.containsAnnotationPropertyInSignature(p.getIRI(), false);
                    o1.getSubAnnotationPropertyOfAxioms(p);
                    o1.getAnnotationPropertyDomainAxioms(p);
                    o1.getAnnotationPropertyRangeAxioms(p);
                }
                for (AxiomType<?> ax : AxiomType.AXIOM_TYPES) {
                    o1.getAxiomCount(ax);
                    o1.getAxiomCount(ax, true);
                    o1.getAxiomCount(ax, false);
                }
                o1.getLogicalAxioms();
                o1.getLogicalAxiomCount();
                for (OWLAxiom ax : o1.getLogicalAxioms()) {
                    o1.containsAxiom(ax);
                    o1.containsAxiom(ax, true, false);
                    o1.containsAxiom(ax, false, false);
                }
                for (OWLAxiom ax : o1.getLogicalAxioms()) {
                    o1.containsAxiom(ax, false, true);
                    o1.containsAxiom(ax, true, true);
                    o1.containsAxiom(ax, false, true);
                }
                for (OWLAxiom ax : o1.getLogicalAxioms()) {
                    o1.getAxiomsIgnoreAnnotations(ax, false);
                    o1.getAxiomsIgnoreAnnotations(ax, true);
                    o1.getAxiomsIgnoreAnnotations(ax, false);
                }
                o1.getGeneralClassAxioms();
                for (OWLAnonymousIndividual i : anonIndividuals) {
                    o1.getReferencingAxioms(i, false);
                }
                for (OWLEntity e : entities) {
                    o1.getReferencingAxioms(e, false);
                    o1.getReferencingAxioms(e, true);
                    o1.getReferencingAxioms(e, false);
                    o1.getDeclarationAxioms(e);
                    o1.containsEntityInSignature(e, true);
                    o1.containsEntityInSignature(e, false);
                    o1.containsEntityInSignature(e);
                    o1.containsEntityInSignature(e.getIRI(), false);
                    o1.containsEntityInSignature(e.getIRI(), true);
                    o1.getEntitiesInSignature(e.getIRI());
                    o1.getEntitiesInSignature(e.getIRI(), false);
                    o1.getEntitiesInSignature(e.getIRI(), true);
                    o1.isDeclared(e);
                    o1.isDeclared(e, true);
                    o1.isDeclared(e, false);
                    if (e instanceof OWLAnnotationSubject) {
                        o1.getAnnotationAssertionAxioms((OWLAnnotationSubject) e);
                    }
                }
                Set<OWLAxiom> axioms = o1.getAxioms();
                for (OWLAxiom ax : axioms) {
                    o1.getOWLOntologyManager().addAxiom(o2, ax);
                    o1.getOWLOntologyManager().removeAxiom(o2, ax);
                }
            }
        }

        @Override
        public String getId() {
            return "test for " + o1.getClass().getSimpleName();
        }
    }

    @Test
    public void testLockingOwlOntologyImpl() {
        String koala = "<?xml version=\"1.0\"?>\n"
                + "<rdf:RDF\n"
                + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
                + "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
                + "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
                + "    xmlns=\"http://protege.stanford.edu/plugins/owl/owl-library/koala.owl#\"\n"
                + "  xml:base=\"http://protege.stanford.edu/plugins/owl/owl-library/koala.owl\">\n"
                + "  <owl:Ontology rdf:about=\"\"/>\n"
                + "  <owl:Class rdf:ID=\"Female\">\n"
                + "    <owl:equivalentClass>\n"
                + "      <owl:Restriction>\n"
                + "        <owl:onProperty>\n"
                + "          <owl:FunctionalProperty rdf:about=\"#hasGender\"/>\n"
                + "        </owl:onProperty>\n"
                + "        <owl:hasValue>\n"
                + "          <Gender rdf:ID=\"female\"/>\n"
                + "        </owl:hasValue>\n"
                + "      </owl:Restriction>\n"
                + "    </owl:equivalentClass>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"Marsupials\">\n"
                + "    <owl:disjointWith>\n"
                + "      <owl:Class rdf:about=\"#Person\"/>\n"
                + "    </owl:disjointWith>\n"
                + "    <rdfs:subClassOf>\n"
                + "      <owl:Class rdf:about=\"#Animal\"/>\n"
                + "    </rdfs:subClassOf>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"Student\">\n"
                + "    <owl:equivalentClass>\n"
                + "      <owl:Class>\n"
                + "        <owl:intersectionOf rdf:parseType=\"Collection\">\n"
                + "          <owl:Class rdf:about=\"#Person\"/>\n"
                + "          <owl:Restriction>\n"
                + "            <owl:onProperty>\n"
                + "              <owl:FunctionalProperty rdf:about=\"#isHardWorking\"/>\n"
                + "            </owl:onProperty>\n"
                + "            <owl:hasValue rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\"\n"
                + "            >true</owl:hasValue>\n"
                + "          </owl:Restriction>\n"
                + "          <owl:Restriction>\n"
                + "            <owl:someValuesFrom>\n"
                + "              <owl:Class rdf:about=\"#University\"/>\n"
                + "            </owl:someValuesFrom>\n"
                + "            <owl:onProperty>\n"
                + "              <owl:ObjectProperty rdf:about=\"#hasHabitat\"/>\n"
                + "            </owl:onProperty>\n"
                + "          </owl:Restriction>\n"
                + "        </owl:intersectionOf>\n"
                + "      </owl:Class>\n"
                + "    </owl:equivalentClass>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"KoalaWithPhD\">\n"
                + "    <owl:versionInfo>1.2</owl:versionInfo>\n"
                + "    <owl:equivalentClass>\n"
                + "      <owl:Class>\n"
                + "        <owl:intersectionOf rdf:parseType=\"Collection\">\n"
                + "          <owl:Restriction>\n"
                + "            <owl:hasValue>\n"
                + "              <Degree rdf:ID=\"PhD\"/>\n"
                + "            </owl:hasValue>\n"
                + "            <owl:onProperty>\n"
                + "              <owl:ObjectProperty rdf:about=\"#hasDegree\"/>\n"
                + "            </owl:onProperty>\n"
                + "          </owl:Restriction>\n"
                + "          <owl:Class rdf:about=\"#Koala\"/>\n"
                + "        </owl:intersectionOf>\n"
                + "      </owl:Class>\n"
                + "    </owl:equivalentClass>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"University\">\n"
                + "    <rdfs:subClassOf>\n"
                + "      <owl:Class rdf:ID=\"Habitat\"/>\n"
                + "    </rdfs:subClassOf>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"Koala\">\n"
                + "    <rdfs:subClassOf>\n"
                + "      <owl:Restriction>\n"
                + "        <owl:hasValue rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\"\n"
                + "        >false</owl:hasValue>\n"
                + "        <owl:onProperty>\n"
                + "          <owl:FunctionalProperty rdf:about=\"#isHardWorking\"/>\n"
                + "        </owl:onProperty>\n"
                + "      </owl:Restriction>\n"
                + "    </rdfs:subClassOf>\n"
                + "    <rdfs:subClassOf>\n"
                + "      <owl:Restriction>\n"
                + "        <owl:someValuesFrom>\n"
                + "          <owl:Class rdf:about=\"#DryEucalyptForest\"/>\n"
                + "        </owl:someValuesFrom>\n"
                + "        <owl:onProperty>\n"
                + "          <owl:ObjectProperty rdf:about=\"#hasHabitat\"/>\n"
                + "        </owl:onProperty>\n"
                + "      </owl:Restriction>\n"
                + "    </rdfs:subClassOf>\n"
                + "    <rdfs:subClassOf rdf:resource=\"#Marsupials\"/>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"Animal\">\n"
                + "    <rdfs:seeAlso>Male</rdfs:seeAlso>\n"
                + "    <rdfs:subClassOf>\n"
                + "      <owl:Restriction>\n"
                + "        <owl:onProperty>\n"
                + "          <owl:ObjectProperty rdf:about=\"#hasHabitat\"/>\n"
                + "        </owl:onProperty>\n"
                + "        <owl:minCardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\"\n"
                + "        >1</owl:minCardinality>\n"
                + "      </owl:Restriction>\n"
                + "    </rdfs:subClassOf>\n"
                + "    <rdfs:subClassOf>\n"
                + "      <owl:Restriction>\n"
                + "        <owl:cardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\"\n"
                + "        >1</owl:cardinality>\n"
                + "        <owl:onProperty>\n"
                + "          <owl:FunctionalProperty rdf:about=\"#hasGender\"/>\n"
                + "        </owl:onProperty>\n"
                + "      </owl:Restriction>\n"
                + "    </rdfs:subClassOf>\n"
                + "    <owl:versionInfo>1.1</owl:versionInfo>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"Forest\">\n"
                + "    <rdfs:subClassOf rdf:resource=\"#Habitat\"/>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"Rainforest\">\n"
                + "    <rdfs:subClassOf rdf:resource=\"#Forest\"/>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"GraduateStudent\">\n"
                + "    <rdfs:subClassOf>\n"
                + "      <owl:Restriction>\n"
                + "        <owl:onProperty>\n"
                + "          <owl:ObjectProperty rdf:about=\"#hasDegree\"/>\n"
                + "        </owl:onProperty>\n"
                + "        <owl:someValuesFrom>\n"
                + "          <owl:Class>\n"
                + "            <owl:oneOf rdf:parseType=\"Collection\">\n"
                + "              <Degree rdf:ID=\"BA\"/>\n"
                + "              <Degree rdf:ID=\"BS\"/>\n"
                + "            </owl:oneOf>\n"
                + "          </owl:Class>\n"
                + "        </owl:someValuesFrom>\n"
                + "      </owl:Restriction>\n"
                + "    </rdfs:subClassOf>\n"
                + "    <rdfs:subClassOf rdf:resource=\"#Student\"/>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"Parent\">\n"
                + "    <owl:equivalentClass>\n"
                + "      <owl:Class>\n"
                + "        <owl:intersectionOf rdf:parseType=\"Collection\">\n"
                + "          <owl:Class rdf:about=\"#Animal\"/>\n"
                + "          <owl:Restriction>\n"
                + "            <owl:onProperty>\n"
                + "              <owl:ObjectProperty rdf:about=\"#hasChildren\"/>\n"
                + "            </owl:onProperty>\n"
                + "            <owl:minCardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\"\n"
                + "            >1</owl:minCardinality>\n"
                + "          </owl:Restriction>\n"
                + "        </owl:intersectionOf>\n"
                + "      </owl:Class>\n"
                + "    </owl:equivalentClass>\n"
                + "    <rdfs:subClassOf rdf:resource=\"#Animal\"/>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"DryEucalyptForest\">\n"
                + "    <rdfs:subClassOf rdf:resource=\"#Forest\"/>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"Quokka\">\n"
                + "    <rdfs:subClassOf>\n"
                + "      <owl:Restriction>\n"
                + "        <owl:hasValue rdf:datatype=\"http://www.w3.org/2001/XMLSchema#boolean\"\n"
                + "        >true</owl:hasValue>\n"
                + "        <owl:onProperty>\n"
                + "          <owl:FunctionalProperty rdf:about=\"#isHardWorking\"/>\n"
                + "        </owl:onProperty>\n"
                + "      </owl:Restriction>\n"
                + "    </rdfs:subClassOf>\n"
                + "    <rdfs:subClassOf rdf:resource=\"#Marsupials\"/>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"TasmanianDevil\">\n"
                + "    <rdfs:subClassOf rdf:resource=\"#Marsupials\"/>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"MaleStudentWith3Daughters\">\n"
                + "    <owl:equivalentClass>\n"
                + "      <owl:Class>\n"
                + "        <owl:intersectionOf rdf:parseType=\"Collection\">\n"
                + "          <owl:Class rdf:about=\"#Student\"/>\n"
                + "          <owl:Restriction>\n"
                + "            <owl:onProperty>\n"
                + "              <owl:FunctionalProperty rdf:about=\"#hasGender\"/>\n"
                + "            </owl:onProperty>\n"
                + "            <owl:hasValue>\n"
                + "              <Gender rdf:ID=\"male\"/>\n"
                + "            </owl:hasValue>\n"
                + "          </owl:Restriction>\n"
                + "          <owl:Restriction>\n"
                + "            <owl:onProperty>\n"
                + "              <owl:ObjectProperty rdf:about=\"#hasChildren\"/>\n"
                + "            </owl:onProperty>\n"
                + "            <owl:cardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\"\n"
                + "            >3</owl:cardinality>\n"
                + "          </owl:Restriction>\n"
                + "          <owl:Restriction>\n"
                + "            <owl:allValuesFrom rdf:resource=\"#Female\"/>\n"
                + "            <owl:onProperty>\n"
                + "              <owl:ObjectProperty rdf:about=\"#hasChildren\"/>\n"
                + "            </owl:onProperty>\n"
                + "          </owl:Restriction>\n"
                + "        </owl:intersectionOf>\n"
                + "      </owl:Class>\n"
                + "    </owl:equivalentClass>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"Degree\"/>\n"
                + "  <owl:Class rdf:ID=\"Male\">\n"
                + "    <owl:equivalentClass>\n"
                + "      <owl:Restriction>\n"
                + "        <owl:hasValue rdf:resource=\"#male\"/>\n"
                + "        <owl:onProperty>\n"
                + "          <owl:FunctionalProperty rdf:about=\"#hasGender\"/>\n"
                + "        </owl:onProperty>\n"
                + "      </owl:Restriction>\n"
                + "    </owl:equivalentClass>\n"
                + "  </owl:Class>\n"
                + "  <owl:Class rdf:ID=\"Gender\"/>\n"
                + "  <owl:Class rdf:ID=\"Person\">\n"
                + "    <rdfs:subClassOf rdf:resource=\"#Animal\"/>\n"
                + "    <owl:disjointWith rdf:resource=\"#Marsupials\"/>\n"
                + "  </owl:Class>\n"
                + "  <owl:ObjectProperty rdf:ID=\"hasHabitat\">\n"
                + "    <rdfs:range rdf:resource=\"#Habitat\"/>\n"
                + "    <rdfs:domain rdf:resource=\"#Animal\"/>\n"
                + "  </owl:ObjectProperty>\n"
                + "  <owl:ObjectProperty rdf:ID=\"hasDegree\">\n"
                + "    <rdfs:domain rdf:resource=\"#Person\"/>\n"
                + "    <rdfs:range rdf:resource=\"#Degree\"/>\n"
                + "  </owl:ObjectProperty>\n"
                + "  <owl:ObjectProperty rdf:ID=\"hasChildren\">\n"
                + "    <rdfs:range rdf:resource=\"#Animal\"/>\n"
                + "    <rdfs:domain rdf:resource=\"#Animal\"/>\n"
                + "  </owl:ObjectProperty>\n"
                + "  <owl:FunctionalProperty rdf:ID=\"hasGender\">\n"
                + "    <rdfs:range rdf:resource=\"#Gender\"/>\n"
                + "    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#ObjectProperty\"/>\n"
                + "    <rdfs:domain rdf:resource=\"#Animal\"/>\n"
                + "  </owl:FunctionalProperty>\n"
                + "  <owl:FunctionalProperty rdf:ID=\"isHardWorking\">\n"
                + "    <rdfs:range rdf:resource=\"http://www.w3.org/2001/XMLSchema#boolean\"/>\n"
                + "    <rdfs:domain rdf:resource=\"#Person\"/>\n"
                + "    <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#DatatypeProperty\"/>\n"
                + "  </owl:FunctionalProperty>\n"
                + "  <Degree rdf:ID=\"MA\"/>\n" + "</rdf:RDF>";
        OWLOntologyManager m = ThreadSafeOWLManager.createOWLOntologyManager();
        OWLOntology o = null;
        try {
            o = m.loadOntologyFromOntologyDocument(new StringDocumentSource(
                    koala));
            MultiThreadChecker checker = new MultiThreadChecker(10);
            checker.check(new TestCallback(o, m.createOntology()));
            String trace = checker.getTrace();
            System.out.println(trace);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
