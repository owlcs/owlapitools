package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;

/** Builder class for OWLNegativeDataPropertyAssertionAxiom */
public class BuilderNegativeDataPropertyAssertion extends
        BaseDataPropertyBuilder<OWLNegativeDataPropertyAssertionAxiom, BuilderNegativeDataPropertyAssertion> {
    private OWLIndividual subject = null;
    private OWLLiteral value = null;


    /** @param arg
     *            value
     * @return builder */
    public BuilderNegativeDataPropertyAssertion withValue(OWLLiteral arg) {
        value = arg;
        return this;
    }

    /** @param arg
     *            subject
     * @return builder */
    public BuilderNegativeDataPropertyAssertion withSubject(OWLIndividual arg) {
        subject = arg;
        return this;
    }

    @Override
    public OWLNegativeDataPropertyAssertionAxiom buildObject() {
        return df.getOWLNegativeDataPropertyAssertionAxiom(property, subject, value,
                annotations);
    }
}
