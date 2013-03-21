package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataProperty;

/** Builder class for OWLDataProperty */
public class BuilderDataProperty extends
        BaseEntityBuilder<OWLDataProperty, BuilderDataProperty> {
    @Override
    public OWLDataProperty buildObject() {
        if (pm != null && string != null) {
            return df.getOWLDataProperty(string, pm);
        }
        return df.getOWLDataProperty(iri);
    }
}
