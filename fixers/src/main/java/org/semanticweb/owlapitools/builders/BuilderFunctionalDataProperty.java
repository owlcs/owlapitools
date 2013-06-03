package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;

/** Builder class for OWLFunctionalDataPropertyAxiom */
public class BuilderFunctionalDataProperty
        extends
        BaseDataPropertyBuilder<OWLFunctionalDataPropertyAxiom, BuilderFunctionalDataProperty> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderFunctionalDataProperty(OWLFunctionalDataPropertyAxiom expected) {
        withProperty(expected.getProperty()).withAnnotations(expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderFunctionalDataProperty() {}

    @Override
    public OWLFunctionalDataPropertyAxiom buildObject() {
        return df.getOWLFunctionalDataPropertyAxiom(property, annotations);
    }
}
