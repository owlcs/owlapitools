package org.semanticweb.owlapitools.profiles.violations;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public interface UndeclaredEntityViolation {
    OWLEntity getEntity();

    OWLOntology getOntology();
}
