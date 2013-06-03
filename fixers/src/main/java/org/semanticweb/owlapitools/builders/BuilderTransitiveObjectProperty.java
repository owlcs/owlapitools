package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;

/** Builder class for OWLTransitiveObjectPropertyAxiom */
public class BuilderTransitiveObjectProperty
        extends
        BaseObjectPropertyBuilder<OWLTransitiveObjectPropertyAxiom, BuilderTransitiveObjectProperty> {
    /** uninitialized builder */
    public BuilderTransitiveObjectProperty() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderTransitiveObjectProperty(OWLTransitiveObjectPropertyAxiom expected) {
        withProperty(expected.getProperty()).withAnnotations(expected.getAnnotations());
    }

    @Override
    public OWLTransitiveObjectPropertyAxiom buildObject() {
        return df.getOWLTransitiveObjectPropertyAxiom(property, annotations);
    }
}
