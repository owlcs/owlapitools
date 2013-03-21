package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

/** Builder class for OWLObjectSomeValuesFrom */
public class BuilderObjectSomeValuesFrom extends
        BaseObjectBuilder<OWLObjectSomeValuesFrom, BuilderObjectSomeValuesFrom> {
    @Override
    public OWLObjectSomeValuesFrom buildObject() {
        return df.getOWLObjectSomeValuesFrom(property, range);
    }
}
