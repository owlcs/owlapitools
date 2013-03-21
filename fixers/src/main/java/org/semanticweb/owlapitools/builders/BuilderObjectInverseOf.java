package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectInverseOf;

/** Builder class for OWLObjectInverseOf */
public class BuilderObjectInverseOf extends
        BaseObjectPropertyBuilder<OWLObjectInverseOf, BuilderObjectInverseOf> {

    @Override
    public OWLObjectInverseOf buildObject() {
        return df.getOWLObjectInverseOf(property);
    }
}
