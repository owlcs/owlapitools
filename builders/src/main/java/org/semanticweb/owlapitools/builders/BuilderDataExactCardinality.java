package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataExactCardinality;

/** Builder class for OWLDataExactCardinality */
public class BuilderDataExactCardinality extends
        BaseDataBuilder<OWLDataExactCardinality, BuilderDataExactCardinality> {
    private int cardinality = -1;

    /** @param arg
     *            cardinality
     * @return builder */
    public BuilderDataExactCardinality withCardinality(int arg) {
        cardinality = arg;
        return this;
    }

    @Override
    public OWLDataExactCardinality buildObject() {
        return df.getOWLDataExactCardinality(cardinality, property, dataRange);
    }
}
