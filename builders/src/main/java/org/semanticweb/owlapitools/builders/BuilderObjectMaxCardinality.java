package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;

/** Builder class for OWLObjectMaxCardinality */
public class BuilderObjectMaxCardinality extends
        BaseObjectBuilder<OWLObjectMaxCardinality, BuilderObjectMaxCardinality> {
    private int cardinality = -1;

    /** @param arg
     *            cardinality
     * @return builder */
    public BuilderObjectMaxCardinality withCardinality(int arg) {
        cardinality = arg;
        return this;
    }

    @Override
    public OWLObjectMaxCardinality buildObject() {
        return df.getOWLObjectMaxCardinality(cardinality, property, range);
    }
}
