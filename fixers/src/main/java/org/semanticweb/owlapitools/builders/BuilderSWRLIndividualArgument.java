package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;

/** Builder class for SWRLIndividualArgument */
public class BuilderSWRLIndividualArgument extends
        BaseBuilder<SWRLIndividualArgument, BuilderSWRLIndividualArgument> {
    private OWLIndividual individual;

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderSWRLIndividualArgument(SWRLIndividualArgument expected) {
        with(expected.getIndividual());
    }

    /** uninitialized builder */
    public BuilderSWRLIndividualArgument() {}

    /** @param arg
     *            individual
     * @return builder */
    public BuilderSWRLIndividualArgument with(OWLIndividual arg) {
        individual = arg;
        return this;
    }

    @Override
    public SWRLIndividualArgument buildObject() {
        return df.getSWRLIndividualArgument(individual);
    }
}
