package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;

/** Builder class for SWRLLiteralArgument */
public class BuilderSWRLLiteralArgument extends
        BaseBuilder<SWRLLiteralArgument, BuilderSWRLLiteralArgument> {
    private OWLLiteral literal;

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderSWRLLiteralArgument(SWRLLiteralArgument expected) {
        with(expected.getLiteral());
    }

    /** uninitialized builder */
    public BuilderSWRLLiteralArgument() {}

    /** @param arg
     *            literal
     * @return builder */
    public BuilderSWRLLiteralArgument with(OWLLiteral arg) {
        literal = arg;
        return this;
    }

    @Override
    public SWRLLiteralArgument buildObject() {
        return df.getSWRLLiteralArgument(literal);
    }
}
