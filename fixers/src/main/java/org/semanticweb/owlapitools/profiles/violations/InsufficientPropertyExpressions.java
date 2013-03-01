package org.semanticweb.owlapitools.profiles.violations;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapitools.profiles.OWLProfileViolation;
import org.semanticweb.owlapitools.profiles.OWLProfileViolationVisitor;

@SuppressWarnings("javadoc")
public class InsufficientPropertyExpressions extends OWLProfileViolation {
    public InsufficientPropertyExpressions(OWLOntology ontology, OWLAxiom axiom) {
        super(ontology, axiom);
    }

    @Override
    public String toString() {
        return toString("Not enough property expressions; at least two needed");
    }

    @Override
    public void accept(OWLProfileViolationVisitor visitor) {
        visitor.visit(this);
    }
}
