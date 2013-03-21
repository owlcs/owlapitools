package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;

/** Builder class for OWLFunctionalObjectPropertyAxiom */
public class BuilderFunctionalObjectProperty extends
        BaseObjectPropertyBuilder<OWLFunctionalObjectPropertyAxiom, BuilderFunctionalObjectProperty> {

    @Override
    public OWLFunctionalObjectPropertyAxiom buildObject() {
        return df.getOWLFunctionalObjectPropertyAxiom(property, annotations);
    }
}
