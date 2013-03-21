package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;

/** Builder class for OWLSubAnnotationPropertyOfAxiom */
public class BuilderSubAnnotationPropertyOf
        extends
        BaseSubBuilder<OWLSubAnnotationPropertyOfAxiom, BuilderSubAnnotationPropertyOf, OWLAnnotationProperty> {
    @Override
    public OWLSubAnnotationPropertyOfAxiom buildObject() {
        return df.getOWLSubAnnotationPropertyOfAxiom(sub, sup, annotations);
    }
}
