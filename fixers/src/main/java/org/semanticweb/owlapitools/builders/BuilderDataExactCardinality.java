package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataExactCardinality;

/** Builder class for OWLDataExactCardinality */
public class BuilderDataExactCardinality extends
        BaseDataBuilder<OWLDataExactCardinality, BuilderDataExactCardinality> {
    private int cardinality = -1;

    /** uninitialized builder */
    public BuilderDataExactCardinality() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDataExactCardinality(OWLDataExactCardinality expected) {
        withCardinality(expected.getCardinality()).withProperty(expected.getProperty())
                .withRange(expected.getFiller());
    }

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
