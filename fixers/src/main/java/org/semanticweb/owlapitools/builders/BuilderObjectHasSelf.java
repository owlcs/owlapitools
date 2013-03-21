package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectHasSelf;

/** Builder class for OWLObjectHasSelf */
public class BuilderObjectHasSelf extends
        BaseObjectPropertyBuilder<OWLObjectHasSelf, BuilderObjectHasSelf> {
    @Override
    public OWLObjectHasSelf buildObject() {
        return df.getOWLObjectHasSelf(property);
    }
}
