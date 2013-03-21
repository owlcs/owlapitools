package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

/** Builder class for OWLSubObjectPropertyOfAxiom */
public class BuilderSubObjectProperty
        extends
        BaseSubBuilder<OWLSubObjectPropertyOfAxiom, BuilderSubObjectProperty, OWLObjectPropertyExpression> {
    @Override
    public OWLSubObjectPropertyOfAxiom buildObject() {
        return df.getOWLSubObjectPropertyOfAxiom(sub, sup, annotations);
    }
}
