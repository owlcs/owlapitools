package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLLiteral;

/** Builder class for OWLDataHasValue */
public class BuilderDataHasValue extends
        BaseDataPropertyBuilder<OWLDataHasValue, BuilderDataHasValue> {
    private OWLLiteral literal = null;


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
