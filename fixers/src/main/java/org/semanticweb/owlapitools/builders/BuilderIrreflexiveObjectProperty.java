package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;

/** Builder class for OWLIrreflexiveObjectPropertyAxiom */
public class BuilderIrreflexiveObjectProperty extends
        BaseObjectPropertyBuilder<OWLIrreflexiveObjectPropertyAxiom, BuilderIrreflexiveObjectProperty> {

    @Override
    public OWLIrreflexiveObjectPropertyAxiom buildObject() {
        return df.getOWLIrreflexiveObjectPropertyAxiom(property, annotations);
    }
}
