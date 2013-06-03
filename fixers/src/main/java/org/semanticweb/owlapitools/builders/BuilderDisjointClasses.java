package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;

/** Builder class for OWLDisjointClassesAxiom */
public class BuilderDisjointClasses
        extends
        BaseSetBuilder<OWLDisjointClassesAxiom, BuilderDisjointClasses, OWLClassExpression> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDisjointClasses(OWLDisjointClassesAxiom expected) {
        withItems(expected.getClassExpressions()).withAnnotations(
                expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderDisjointClasses() {}

    @Override
    public OWLDisjointClassesAxiom buildObject() {
        return df.getOWLDisjointClassesAxiom(items, annotations);
    }
}
