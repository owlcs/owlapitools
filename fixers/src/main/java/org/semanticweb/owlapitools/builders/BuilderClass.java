package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClass;

/** Builder class for OWLClass */
public class BuilderClass extends BaseEntityBuilder<OWLClass, BuilderClass> {
    @Override
    public OWLClass buildObject() {
        if (pm != null && string != null) {
            return df.getOWLClass(string, pm);
        }
        return df.getOWLClass(iri);
    }
}
