package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectMinCardinality;

/** Builder class for OWLObjectMinCardinality */
public class BuilderObjectMinCardinality extends
        BaseObjectBuilder<OWLObjectMinCardinality, BuilderObjectMinCardinality> {
    private int cardinality = -1;

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderObjectMinCardinality(OWLObjectMinCardinality expected) {
        withCardinality(expected.getCardinality()).withProperty(expected.getProperty())
                .withRange(expected.getFiller());
    }

    /** uninitialized builder */
    public BuilderObjectMinCardinality() {}

    /** @param arg
     *            cardinality
     * @return builder */
    public BuilderObjectMinCardinality withCardinality(int arg) {
        cardinality = arg;
        return this;
    }

    @Override
    public OWLObjectMinCardinality buildObject() {
        return df.getOWLObjectMinCardinality(cardinality, property, range);
    }
}
