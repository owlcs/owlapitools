package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectMinCardinality;

/** Builder class for OWLObjectMinCardinality */
public class BuilderObjectMinCardinality extends
        BaseObjectBuilder<OWLObjectMinCardinality, BuilderObjectMinCardinality> {
    private int cardinality = -1;

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
