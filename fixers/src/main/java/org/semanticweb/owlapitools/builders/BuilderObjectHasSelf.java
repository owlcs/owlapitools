package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectHasSelf;

/** Builder class for OWLObjectHasSelf */
public class BuilderObjectHasSelf extends
        BaseObjectPropertyBuilder<OWLObjectHasSelf, BuilderObjectHasSelf> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderObjectHasSelf(OWLObjectHasSelf expected) {
        withProperty(expected.getProperty());
    }

    /** uninitialized builder */
    public BuilderObjectHasSelf() {}

    @Override
    public OWLObjectHasSelf buildObject() {
        return df.getOWLObjectHasSelf(property);
    }
}
