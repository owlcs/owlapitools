package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;

/** Builder class for OWLAnnotationPropertyRangeAxiom */
public class BuilderAnnotationPropertyRange
        extends
        BaseAnnotationtPropertyBuilder<OWLAnnotationPropertyRangeAxiom, BuilderAnnotationPropertyRange> {
    private IRI iri = null;

    /** uninitialized builder */
    public BuilderAnnotationPropertyRange() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderAnnotationPropertyRange(OWLAnnotationPropertyRangeAxiom expected) {
        withProperty(expected.getProperty()).withRange(expected.getRange())
                .withAnnotations(expected.getAnnotations());
    }

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
