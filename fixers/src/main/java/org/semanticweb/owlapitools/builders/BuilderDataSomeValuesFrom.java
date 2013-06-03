package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;

/** Builder class for OWLDataSomeValuesFrom */
public class BuilderDataSomeValuesFrom extends
        BaseDataBuilder<OWLDataSomeValuesFrom, BuilderDataSomeValuesFrom> {
    /** uninitialized builder */
    public BuilderDataSomeValuesFrom() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDataSomeValuesFrom(OWLDataSomeValuesFrom expected) {
        withProperty(expected.getProperty()).withRange(expected.getFiller());
    }

    @Override
    public OWLDataSomeValuesFrom buildObject() {
        return df.getOWLDataSomeValuesFrom(property, dataRange);
    }
}
