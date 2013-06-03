package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;

/** Builder class for OWLAnnotation */
public class BuilderAnnotation extends
        BaseAnnotationtPropertyBuilder<OWLAnnotation, BuilderAnnotation> {
    private OWLAnnotationValue value = null;

    /** uninitialized builder */
    public BuilderAnnotation() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderAnnotation(OWLAnnotation expected) {
        withProperty(expected.getProperty()).withValue(expected.getValue());
    }

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
