package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;

/** Builder class for OWLSymmetricObjectPropertyAxiom */
public class BuilderSymmetricObjectProperty extends
        BaseObjectPropertyBuilder<OWLSymmetricObjectPropertyAxiom, BuilderSymmetricObjectProperty> {

    @Override
    public OWLSymmetricObjectPropertyAxiom buildObject() {
        return df.getOWLSymmetricObjectPropertyAxiom(property, annotations);
    }
}
