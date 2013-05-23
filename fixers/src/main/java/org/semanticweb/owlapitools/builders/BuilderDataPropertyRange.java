package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;

/** Builder class for OWLDataPropertyRangeAxiom */
public class BuilderDataPropertyRange extends
        BaseDataBuilder<OWLDataPropertyRangeAxiom, BuilderDataPropertyRange> {
    /** uninitialized builder */
    public BuilderDataPropertyRange() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDataPropertyRange(OWLDataPropertyRangeAxiom expected) {
        withProperty(expected.getProperty()).withRange(expected.getRange())
                .withAnnotations(expected.getAnnotations());
    }

    @Override
    public OWLDataPropertyRangeAxiom buildObject() {
        return df.getOWLDataPropertyRangeAxiom(property, dataRange, annotations);
    }
}
