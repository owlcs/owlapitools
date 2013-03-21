package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectExactCardinality;

/** Builder class for OWLObjectExactCardinality */
public class BuilderObjectExactCardinality extends
        BaseObjectBuilder<OWLObjectExactCardinality, BuilderObjectExactCardinality> {
    private int cardinality = -1;

    /** @param arg
     *            cardinality
     * @return builder */
    public BuilderObjectExactCardinality withCardinality(int arg) {
        cardinality = arg;
        return this;
    }

    @Override
    public OWLObjectExactCardinality buildObject() {
        return df.getOWLObjectExactCardinality(cardinality, property, range);
    }
}
