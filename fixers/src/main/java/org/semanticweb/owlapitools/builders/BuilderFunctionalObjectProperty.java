package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;

/** Builder class for OWLFunctionalObjectPropertyAxiom */
public class BuilderFunctionalObjectProperty
        extends
        BaseObjectPropertyBuilder<OWLFunctionalObjectPropertyAxiom, BuilderFunctionalObjectProperty> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderFunctionalObjectProperty(OWLFunctionalObjectPropertyAxiom expected) {
        withProperty(expected.getProperty()).withAnnotations(expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderFunctionalObjectProperty() {}

    @Override
    public OWLFunctionalObjectPropertyAxiom buildObject() {
        return df.getOWLFunctionalObjectPropertyAxiom(property, annotations);
    }
}
