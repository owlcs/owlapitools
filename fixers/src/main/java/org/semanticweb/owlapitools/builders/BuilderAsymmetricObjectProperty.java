package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;

/** Builder class for OWLAsymmetricObjectPropertyAxiom */
public class BuilderAsymmetricObjectProperty extends
        BaseObjectPropertyBuilder<OWLAsymmetricObjectPropertyAxiom, BuilderAsymmetricObjectProperty> {

    @Override
    public OWLAsymmetricObjectPropertyAxiom buildObject() {
        return df.getOWLAsymmetricObjectPropertyAxiom(property, annotations);
    }
}
