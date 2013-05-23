package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

/** Builder class for OWLObjectSomeValuesFrom */
public class BuilderObjectSomeValuesFrom extends
        BaseObjectBuilder<OWLObjectSomeValuesFrom, BuilderObjectSomeValuesFrom> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderObjectSomeValuesFrom(OWLObjectSomeValuesFrom expected) {
        withProperty(expected.getProperty()).withRange(expected.getFiller());
    }

    /** uninitialized builder */
    public BuilderObjectSomeValuesFrom() {}

    @Override
    public OWLObjectSomeValuesFrom buildObject() {
        return df.getOWLObjectSomeValuesFrom(property, range);
    }
}
