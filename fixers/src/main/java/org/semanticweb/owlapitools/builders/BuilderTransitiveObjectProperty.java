package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;

/** Builder class for OWLTransitiveObjectPropertyAxiom */
public class BuilderTransitiveObjectProperty extends
        BaseObjectPropertyBuilder<OWLTransitiveObjectPropertyAxiom, BuilderTransitiveObjectProperty> {

    @Override
    public OWLTransitiveObjectPropertyAxiom buildObject() {
        return df.getOWLTransitiveObjectPropertyAxiom(property, annotations);
    }
}
