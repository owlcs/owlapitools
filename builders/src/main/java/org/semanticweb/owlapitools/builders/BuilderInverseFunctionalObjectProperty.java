package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;

/** Builder class for OWLInverseFunctionalObjectPropertyAxiom */
public class BuilderInverseFunctionalObjectProperty extends
        BaseObjectPropertyBuilder<OWLInverseFunctionalObjectPropertyAxiom, BuilderInverseFunctionalObjectProperty> {
    @Override
    public OWLInverseFunctionalObjectPropertyAxiom buildObject() {
        return df.getOWLInverseFunctionalObjectPropertyAxiom(property, annotations);
    }
}
