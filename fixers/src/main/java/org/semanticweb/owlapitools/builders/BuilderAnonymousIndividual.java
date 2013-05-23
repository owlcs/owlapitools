package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;

/** Builder class for OWLAnonymousIndividual */
public class BuilderAnonymousIndividual extends
        BaseBuilder<OWLAnonymousIndividual, BuilderAnonymousIndividual> {
    private String id = null;

    /** uninitialized builder */
    public BuilderAnonymousIndividual() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderAnonymousIndividual(OWLAnonymousIndividual expected) {
        withId(expected.getID().getID());
    }

    /** @param arg
     *            blank node id
     * @return builder */
    public BuilderAnonymousIndividual withId(String arg) {
        id = arg;
        return this;
    }

    @Override
    public OWLAnonymousIndividual buildObject() {
        if (id == null) {
            return df.getOWLAnonymousIndividual();
        }
        return df.getOWLAnonymousIndividual(id);
    }
}
