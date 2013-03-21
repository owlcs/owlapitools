package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;

/** Builder class for OWLAnnotationPropertyRangeAxiom */
public class BuilderAnnotationPropertyRange extends
        BaseAnnotationtPropertyBuilder<OWLAnnotationPropertyRangeAxiom, BuilderAnnotationPropertyRange> {
    private IRI iri = null;


    /** @param arg
     *            range
     * @return builder */
    public BuilderAnnotationPropertyRange withRange(IRI arg) {
        iri = arg;
        return this;
    }

    @Override
    public OWLAnnotationPropertyRangeAxiom buildObject() {
        return df.getOWLAnnotationPropertyRangeAxiom(property, iri, annotations);
    }
}
