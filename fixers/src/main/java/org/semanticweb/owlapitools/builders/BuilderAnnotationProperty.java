package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLAnnotationProperty;

/** Builder class for OWLAnnotationProperty */
public class BuilderAnnotationProperty extends
        BaseEntityBuilder<OWLAnnotationProperty, BuilderAnnotationProperty> {
    /** uninitialized builder */
    public BuilderAnnotationProperty() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderAnnotationProperty(OWLAnnotationProperty expected) {
        withIRI(expected.getIRI());
    }

    @Override
    public OWLAnnotationProperty buildObject() {
        if (pm != null && string != null) {
            return df.getOWLAnnotationProperty(string, pm);
        }
        return df.getOWLAnnotationProperty(iri);
    }
}
