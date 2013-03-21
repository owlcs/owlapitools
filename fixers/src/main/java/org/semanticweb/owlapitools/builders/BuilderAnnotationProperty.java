package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLAnnotationProperty;

/** Builder class for OWLAnnotationProperty */
public class BuilderAnnotationProperty extends
        BaseEntityBuilder<OWLAnnotationProperty, BuilderAnnotationProperty> {
    @Override
    public OWLAnnotationProperty buildObject() {
        if (pm != null && string != null) {
            return df.getOWLAnnotationProperty(string, pm);
        }
        return df.getOWLAnnotationProperty(iri);
    }
}
