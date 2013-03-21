package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectProperty;

/** Builder class for OWLObjectProperty */
public class BuilderObjectProperty extends
        BaseEntityBuilder<OWLObjectProperty, BuilderObjectProperty> {
    @Override
    public OWLObjectProperty buildObject() {
        if (pm != null && string != null) {
            return df.getOWLObjectProperty(string, pm);
        }
        return df.getOWLObjectProperty(iri);
    }
}
