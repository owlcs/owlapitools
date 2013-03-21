package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;

/** Builder class for OWLObjectAllValuesFrom */
public class BuilderObjectAllValuesFrom extends
        BaseObjectBuilder<OWLObjectAllValuesFrom, BuilderObjectAllValuesFrom> {
    @Override
    public OWLObjectAllValuesFrom buildObject() {
        return df.getOWLObjectAllValuesFrom(property, range);
    }
}
