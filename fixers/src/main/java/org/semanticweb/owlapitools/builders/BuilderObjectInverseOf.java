package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectInverseOf;

/** Builder class for OWLObjectInverseOf */
public class BuilderObjectInverseOf extends
        BaseObjectPropertyBuilder<OWLObjectInverseOf, BuilderObjectInverseOf> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderObjectInverseOf(OWLObjectInverseOf expected) {
        withProperty(expected.getInverse());
    }

    /** uninitialized builder */
    public BuilderObjectInverseOf() {}

    @Override
    public OWLObjectInverseOf buildObject() {
        return df.getOWLObjectInverseOf(property);
    }
}
