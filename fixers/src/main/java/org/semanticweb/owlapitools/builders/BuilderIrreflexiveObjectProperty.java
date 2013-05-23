package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;

/** Builder class for OWLIrreflexiveObjectPropertyAxiom */
public class BuilderIrreflexiveObjectProperty
        extends
        BaseObjectPropertyBuilder<OWLIrreflexiveObjectPropertyAxiom, BuilderIrreflexiveObjectProperty> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderIrreflexiveObjectProperty(OWLIrreflexiveObjectPropertyAxiom expected) {
        withProperty(expected.getProperty()).withAnnotations(expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderIrreflexiveObjectProperty() {}

    @Override
    public OWLIrreflexiveObjectPropertyAxiom buildObject() {
        return df.getOWLIrreflexiveObjectPropertyAxiom(property, annotations);
    }
}
