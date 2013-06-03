package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLLiteral;

/** Builder class for OWLDataHasValue */
public class BuilderDataHasValue extends
        BaseDataPropertyBuilder<OWLDataHasValue, BuilderDataHasValue> {
    private OWLLiteral literal = null;

    /** uninitialized builder */
    public BuilderDataHasValue() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDataHasValue(OWLDataHasValue expected) {
        withProperty(expected.getProperty()).withLiteral(expected.getValue());
    }

    /** @param arg
     *            literal
     * @return builder */
    public BuilderDataHasValue withLiteral(OWLLiteral arg) {
        literal = arg;
        return this;
    }

    @Override
    public OWLDataHasValue buildObject() {
        return df.getOWLDataHasValue(property, literal);
    }
}
