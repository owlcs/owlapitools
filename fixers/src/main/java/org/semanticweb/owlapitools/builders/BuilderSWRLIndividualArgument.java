package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;

/** Builder class for SWRLIndividualArgument */
public class BuilderSWRLIndividualArgument extends
        BaseBuilder<SWRLIndividualArgument, BuilderSWRLIndividualArgument> {
    private OWLIndividual individual;

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
