package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;

/** Builder class for OWLDataAllValuesFrom */
public class BuilderDataAllValuesFrom extends
        BaseDataBuilder<OWLDataAllValuesFrom, BuilderDataAllValuesFrom> {
    /** uninitialized builder */
    public BuilderDataAllValuesFrom() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDataAllValuesFrom(OWLDataAllValuesFrom expected) {
        withProperty(expected.getProperty()).withRange(expected.getFiller());
    }

    @Override
    public OWLDataAllValuesFrom buildObject() {
        return df.getOWLDataAllValuesFrom(property, dataRange);
    }
}
