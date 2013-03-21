package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataMinCardinality;

/** Builder class for OWLDataMinCardinality */
public class BuilderDataMinCardinality extends
        BaseDataBuilder<OWLDataMinCardinality, BuilderDataMinCardinality> {
    private int cardinality = -1;

    /** @param arg
     *            cardinality
     * @return builder */
    public BuilderDataMinCardinality withCardinality(int arg) {
        cardinality = arg;
        return this;
    }

    @Override
    public OWLDataMinCardinality buildObject() {
        return df.getOWLDataMinCardinality(cardinality, property, dataRange);
    }
}
