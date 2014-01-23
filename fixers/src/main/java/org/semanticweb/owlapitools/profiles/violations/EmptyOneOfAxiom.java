package org.semanticweb.owlapitools.profiles.violations;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapitools.profiles.OWLProfileViolation;
import org.semanticweb.owlapitools.profiles.OWLProfileViolationVisitor;
import org.semanticweb.owlapitools.profiles.OWLProfileViolationVisitorEx;

@SuppressWarnings("javadoc")
public class EmptyOneOfAxiom extends OWLProfileViolation<Object> {
    public EmptyOneOfAxiom(OWLOntology currentOntology, OWLAxiom currentAxiom) {
        super(currentOntology, currentAxiom, null);
    }

    @Override
    public String toString() {
        return toString("Empty OneOf: at least one value needed");
    }

    @Override
    public void accept(OWLProfileViolationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <O> O accept(OWLProfileViolationVisitorEx<O> visitor) {
        return visitor.visit(this);
    }
}
