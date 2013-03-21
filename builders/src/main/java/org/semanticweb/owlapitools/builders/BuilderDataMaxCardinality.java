package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataMaxCardinality;

/** Builder class for OWLDataMaxCardinality */
public class BuilderDataMaxCardinality extends
        BaseDataBuilder<OWLDataMaxCardinality, BuilderDataMaxCardinality> {
    private int cardinality = -1;

    /** @param arg
     *            cardinality
     * @return builder */
    public BuilderDataMaxCardinality withCardinality(int arg) {
        cardinality = arg;
        return this;
    }

    @Override
    public OWLDataMaxCardinality buildObject() {
        return df.getOWLDataMaxCardinality(cardinality, property, dataRange);
    }
}
