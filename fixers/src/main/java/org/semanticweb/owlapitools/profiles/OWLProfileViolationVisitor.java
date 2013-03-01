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
package org.semanticweb.owlapitools.profiles;

import org.semanticweb.owlapitools.profiles.violations.CycleInDatatypeDefinition;
import org.semanticweb.owlapitools.profiles.violations.DatatypeIRIAlsoUsedAsClassIRI;
import org.semanticweb.owlapitools.profiles.violations.EmptyOneOfAxiom;
import org.semanticweb.owlapitools.profiles.violations.IllegalPunning;
import org.semanticweb.owlapitools.profiles.violations.InsufficientIndividuals;
import org.semanticweb.owlapitools.profiles.violations.InsufficientOperands;
import org.semanticweb.owlapitools.profiles.violations.InsufficientPropertyExpressions;
import org.semanticweb.owlapitools.profiles.violations.LastPropertyInChainNotInImposedRange;
import org.semanticweb.owlapitools.profiles.violations.LexicalNotInLexicalSpace;
import org.semanticweb.owlapitools.profiles.violations.OntologyIRINotAbsolute;
import org.semanticweb.owlapitools.profiles.violations.OntologyVersionIRINotAbsolute;
import org.semanticweb.owlapitools.profiles.violations.UseOfAnonymousIndividual;
import org.semanticweb.owlapitools.profiles.violations.UseOfBuiltInDatatypeInDatatypeDefinition;
import org.semanticweb.owlapitools.profiles.violations.UseOfDefinedDatatypeInDatatypeRestriction;
import org.semanticweb.owlapitools.profiles.violations.UseOfIllegalAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfIllegalClassExpression;
import org.semanticweb.owlapitools.profiles.violations.UseOfIllegalDataRange;
import org.semanticweb.owlapitools.profiles.violations.UseOfIllegalFacetRestriction;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonAbsoluteIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonAtomicClassExpression;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonEquivalentClassExpression;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInCardinalityRestriction;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInDisjointPropertiesAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInFunctionalPropertyAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInIrreflexivePropertyAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSimplePropertyInObjectHasSelf;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSubClassExpression;
import org.semanticweb.owlapitools.profiles.violations.UseOfNonSuperClassExpression;
import org.semanticweb.owlapitools.profiles.violations.UseOfObjectPropertyInverse;
import org.semanticweb.owlapitools.profiles.violations.UseOfPropertyInChainCausesCycle;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForAnnotationPropertyIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForClassIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForDataPropertyIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForIndividualIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForObjectPropertyIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForOntologyIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfReservedVocabularyForVersionIRI;
import org.semanticweb.owlapitools.profiles.violations.UseOfTopDataPropertyAsSubPropertyInSubPropertyAxiom;
import org.semanticweb.owlapitools.profiles.violations.UseOfUndeclaredAnnotationProperty;
import org.semanticweb.owlapitools.profiles.violations.UseOfUndeclaredClass;
import org.semanticweb.owlapitools.profiles.violations.UseOfUndeclaredDataProperty;
import org.semanticweb.owlapitools.profiles.violations.UseOfUndeclaredDatatype;
import org.semanticweb.owlapitools.profiles.violations.UseOfUndeclaredObjectProperty;
import org.semanticweb.owlapitools.profiles.violations.UseOfUnknownDatatype;

/** Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 03-Aug-2009 */
@SuppressWarnings("javadoc")
public interface OWLProfileViolationVisitor {
    void visit(IllegalPunning violation);

    void visit(CycleInDatatypeDefinition violation);

    void visit(UseOfBuiltInDatatypeInDatatypeDefinition violation);

    void visit(DatatypeIRIAlsoUsedAsClassIRI violation);

    void visit(UseOfNonSimplePropertyInAsymmetricObjectPropertyAxiom violation);

    void visit(UseOfNonSimplePropertyInCardinalityRestriction violation);

    void visit(UseOfNonSimplePropertyInDisjointPropertiesAxiom violation);

    void visit(UseOfNonSimplePropertyInFunctionalPropertyAxiom violation);

    void visit(UseOfNonSimplePropertyInInverseFunctionalObjectPropertyAxiom violation);

    void visit(UseOfNonSimplePropertyInIrreflexivePropertyAxiom violation);

    void visit(UseOfNonSimplePropertyInObjectHasSelf violation);

    void visit(UseOfPropertyInChainCausesCycle violation);

    void visit(UseOfReservedVocabularyForAnnotationPropertyIRI violation);

    void visit(UseOfReservedVocabularyForClassIRI violation);

    void visit(UseOfReservedVocabularyForDataPropertyIRI violation);

    void visit(UseOfReservedVocabularyForIndividualIRI violation);

    void visit(UseOfReservedVocabularyForObjectPropertyIRI violation);

    void visit(UseOfReservedVocabularyForOntologyIRI violation);

    void visit(UseOfReservedVocabularyForVersionIRI violation);

    void visit(UseOfTopDataPropertyAsSubPropertyInSubPropertyAxiom violation);

    void visit(UseOfUndeclaredAnnotationProperty violation);

    void visit(UseOfUndeclaredClass violation);

    void visit(UseOfUndeclaredDataProperty violation);

    void visit(UseOfUndeclaredDatatype violation);

    void visit(UseOfUndeclaredObjectProperty violation);

    void visit(InsufficientPropertyExpressions violation);

    void visit(InsufficientIndividuals violation);

    void visit(InsufficientOperands violation);

    void visit(EmptyOneOfAxiom violation);

    void visit(LastPropertyInChainNotInImposedRange lastPropertyInChainNotInImposedRange);

    void visit(OntologyIRINotAbsolute ontologyIRINotAbsolute);

            void
            visit(UseOfDefinedDatatypeInDatatypeRestriction useOfDefinedDatatypeInDatatypeRestriction);

    void visit(UseOfIllegalClassExpression useOfIllegalClassExpression);

    void visit(UseOfIllegalDataRange useOfIllegalDataRange);

    void visit(UseOfUnknownDatatype useOfUnknownDatatype);

    void visit(UseOfObjectPropertyInverse useOfObjectPropertyInverse);

    void visit(UseOfNonSuperClassExpression useOfNonSuperClassExpression);

    void visit(UseOfNonSubClassExpression useOfNonSubClassExpression);

    void visit(UseOfNonEquivalentClassExpression useOfNonEquivalentClassExpression);

    void visit(UseOfNonAtomicClassExpression useOfNonAtomicClassExpression);

    void visit(LexicalNotInLexicalSpace lexicalNotInLexicalSpace);

    void visit(OntologyVersionIRINotAbsolute ontologyVersionIRINotAbsolute);

    void visit(UseOfAnonymousIndividual useOfAnonymousIndividual);

    void visit(UseOfIllegalAxiom useOfIllegalAxiom);

    void visit(UseOfIllegalFacetRestriction useOfIllegalFacetRestriction);

    void visit(UseOfNonAbsoluteIRI useOfNonAbsoluteIRI);
}
