package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;

/** Builder class for OWLAnonymousIndividual */
public class BuilderAnonymousIndividual extends
        BaseBuilder<OWLAnonymousIndividual, BuilderAnonymousIndividual> {
    private String id = null;

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
