package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataProperty;

/** Builder class for OWLDataProperty */
public class BuilderDataProperty extends
        BaseEntityBuilder<OWLDataProperty, BuilderDataProperty> {
    /** uninitialized builder */
    public BuilderDataProperty() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDataProperty(OWLDataProperty expected) {
        withIRI(expected.getIRI());
    }

    @Override
    public OWLDataProperty buildObject() {
        if (pm != null && string != null) {
            return df.getOWLDataProperty(string, pm);
        }
        return df.getOWLDataProperty(iri);
    }
}
