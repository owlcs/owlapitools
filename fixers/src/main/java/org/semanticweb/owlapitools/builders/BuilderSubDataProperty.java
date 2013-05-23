package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;

/** Builder class for OWLSubDataPropertyOfAxiom */
public class BuilderSubDataProperty
        extends
        BaseSubBuilder<OWLSubDataPropertyOfAxiom, BuilderSubDataProperty, OWLDataPropertyExpression> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderSubDataProperty(OWLSubDataPropertyOfAxiom expected) {
        withSub(expected.getSubProperty()).withSup(expected.getSuperProperty())
                .withAnnotations(expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderSubDataProperty() {}

    @Override
    public OWLSubDataPropertyOfAxiom buildObject() {
        return df.getOWLSubDataPropertyOfAxiom(sub, sup, annotations);
    }
}
