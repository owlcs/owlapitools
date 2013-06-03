package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/** Builder class for OWLSubClassOfAxiom */
public class BuilderSubClass extends
        BaseSubBuilder<OWLSubClassOfAxiom, BuilderSubClass, OWLClassExpression> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderSubClass(OWLSubClassOfAxiom expected) {
        withSub(expected.getSubClass()).withSup(expected.getSuperClass())
                .withAnnotations(expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderSubClass() {}

    @Override
    public OWLSubClassOfAxiom buildObject() {
        return df.getOWLSubClassOfAxiom(sub, sup, annotations);
    }
}
