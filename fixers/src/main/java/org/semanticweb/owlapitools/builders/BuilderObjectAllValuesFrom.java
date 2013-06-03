package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;

/** Builder class for OWLObjectAllValuesFrom */
public class BuilderObjectAllValuesFrom extends
        BaseObjectBuilder<OWLObjectAllValuesFrom, BuilderObjectAllValuesFrom> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderObjectAllValuesFrom(OWLObjectAllValuesFrom expected) {
        withProperty(expected.getProperty()).withRange(expected.getFiller());
    }

    /** uninitialized builder */
    public BuilderObjectAllValuesFrom() {}

    @Override
    public OWLObjectAllValuesFrom buildObject() {
        return df.getOWLObjectAllValuesFrom(property, range);
    }
}
