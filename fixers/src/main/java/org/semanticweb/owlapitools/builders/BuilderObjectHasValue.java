package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectHasValue;

/** Builder class for OWLObjectHasValue */
public class BuilderObjectHasValue extends
        BaseObjectPropertyBuilder<OWLObjectHasValue, BuilderObjectHasValue> {
    private OWLIndividual value = null;

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderObjectHasValue(OWLObjectHasValue expected) {
        withProperty(expected.getProperty()).withValue(expected.getValue());
    }

    /** uninitialized builder */
    public BuilderObjectHasValue() {}

    /** @param arg
     *            argument
     * @return builder */
    public BuilderObjectHasValue withValue(OWLIndividual arg) {
        value = arg;
        return this;
    }

    @Override
    public OWLObjectHasValue buildObject() {
        return df.getOWLObjectHasValue(property, value);
    }
}
