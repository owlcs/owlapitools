package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;

/** Builder class for OWLObjectPropertyRangeAxiom */
public class BuilderObjectPropertyRange extends
        BaseObjectBuilder<OWLObjectPropertyRangeAxiom, BuilderObjectPropertyRange> {
    @Override
    public OWLObjectPropertyRangeAxiom buildObject() {
        return df.getOWLObjectPropertyRangeAxiom(property, range, annotations);
    }
}
