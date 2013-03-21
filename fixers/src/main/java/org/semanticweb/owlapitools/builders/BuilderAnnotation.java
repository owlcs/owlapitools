package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;

/** Builder class for OWLAnnotation */
public class BuilderAnnotation extends
        BaseAnnotationtPropertyBuilder<OWLAnnotation, BuilderAnnotation> {
    private OWLAnnotationValue value = null;


    /** @param arg
     *            the annotation value
     * @return builder */
    public BuilderAnnotation withValue(OWLAnnotationValue arg) {
        value = arg;
        return this;
    }

    @Override
    public OWLAnnotation buildObject() {
        return df.getOWLAnnotation(property, value, annotations);
    }
}
