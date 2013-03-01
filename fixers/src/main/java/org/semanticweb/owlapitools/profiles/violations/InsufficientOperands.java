package org.semanticweb.owlapitools.profiles.violations;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapitools.profiles.OWLProfileViolation;
import org.semanticweb.owlapitools.profiles.OWLProfileViolationVisitor;

@SuppressWarnings("javadoc")
public class InsufficientOperands extends OWLProfileViolation {
    private final OWLObject expression;

    public InsufficientOperands(OWLOntology currentOntology, OWLAxiom node, OWLObject c) {
        super(currentOntology, node);
        expression = c;
    }

    @Override
    public void accept(OWLProfileViolationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return toString("Not enough operands; at least two needed: %s", expression);
    }
}
