package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectHasValue;

/** Builder class for OWLObjectHasValue */
public class BuilderObjectHasValue extends
        BaseObjectPropertyBuilder<OWLObjectHasValue, BuilderObjectHasValue> {
    private OWLIndividual value = null;


    /** @param arg
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
