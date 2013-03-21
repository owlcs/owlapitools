package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;

/** Builder class for OWLDataPropertyRangeAxiom */
public class BuilderDataPropertyRange extends
        BaseDataBuilder<OWLDataPropertyRangeAxiom, BuilderDataPropertyRange> {
    @Override
    public OWLDataPropertyRangeAxiom buildObject() {
        return df.getOWLDataPropertyRangeAxiom(property, dataRange, annotations);
    }
}
