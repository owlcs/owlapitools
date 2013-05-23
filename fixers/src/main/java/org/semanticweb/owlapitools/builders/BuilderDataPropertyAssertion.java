package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

/** Builder class for OWLDataPropertyAssertionAxiom */
public class BuilderDataPropertyAssertion
        extends
        BaseDataPropertyBuilder<OWLDataPropertyAssertionAxiom, BuilderDataPropertyAssertion> {
    private OWLIndividual subject = null;
    private OWLLiteral object = null;

    /** uninitialized builder */
    public BuilderDataPropertyAssertion() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDataPropertyAssertion(OWLDataPropertyAssertionAxiom expected) {
        withProperty(expected.getProperty()).withSubject(expected.getSubject())
                .withValue(expected.getObject())
                .withAnnotations(expected.getAnnotations());
    }

    /** @param arg
     *            value
     * @return builder */
    public BuilderDataPropertyAssertion withValue(OWLLiteral arg) {
        object = arg;
        return this;
    }

    /** @param arg
     *            individual
     * @return builder */
    public BuilderDataPropertyAssertion withSubject(OWLIndividual arg) {
        subject = arg;
        return this;
    }

    @Override
    public OWLDataPropertyAssertionAxiom buildObject() {
        return df
                .getOWLDataPropertyAssertionAxiom(property, subject, object, annotations);
    }
}
