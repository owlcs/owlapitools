package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;

/** Builder class for OWLSubDataPropertyOfAxiom */
public class BuilderSubDataProperty
        extends
        BaseSubBuilder<OWLSubDataPropertyOfAxiom, BuilderSubDataProperty, OWLDataPropertyExpression> {
    @Override
    public OWLSubDataPropertyOfAxiom buildObject() {
        return df.getOWLSubDataPropertyOfAxiom(sub, sup, annotations);
    }
}
