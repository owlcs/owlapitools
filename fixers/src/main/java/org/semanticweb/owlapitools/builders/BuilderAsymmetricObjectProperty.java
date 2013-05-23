package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;

/** Builder class for OWLAsymmetricObjectPropertyAxiom */
public class BuilderAsymmetricObjectProperty
        extends
        BaseObjectPropertyBuilder<OWLAsymmetricObjectPropertyAxiom, BuilderAsymmetricObjectProperty> {
    /** uninitialized builder */
    public BuilderAsymmetricObjectProperty() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderAsymmetricObjectProperty(OWLAsymmetricObjectPropertyAxiom expected) {
        withProperty(expected.getProperty()).withAnnotations(expected.getAnnotations());
    }

    @Override
    public OWLAsymmetricObjectPropertyAxiom buildObject() {
        return df.getOWLAsymmetricObjectPropertyAxiom(property, annotations);
    }
}
