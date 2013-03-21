package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;

/** Builder class for OWLReflexiveObjectPropertyAxiom */
public class BuilderReflexiveObjectProperty extends
        BaseObjectPropertyBuilder<OWLReflexiveObjectPropertyAxiom, BuilderReflexiveObjectProperty> {

    @Override
    public OWLReflexiveObjectPropertyAxiom buildObject() {
        return df.getOWLReflexiveObjectPropertyAxiom(property, annotations);
    }
}
