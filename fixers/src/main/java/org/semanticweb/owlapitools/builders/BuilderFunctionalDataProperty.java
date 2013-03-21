package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;

/** Builder class for OWLFunctionalDataPropertyAxiom */
public class BuilderFunctionalDataProperty extends
        BaseDataPropertyBuilder<OWLFunctionalDataPropertyAxiom, BuilderFunctionalDataProperty> {
    @Override
    public OWLFunctionalDataPropertyAxiom buildObject() {
        return df.getOWLFunctionalDataPropertyAxiom(property, annotations);
    }
}
