package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;

/** Builder class for OWLDisjointUnionAxiom */
public class BuilderDisjointUnion extends
        BaseSetBuilder<OWLDisjointUnionAxiom, BuilderDisjointUnion, OWLClassExpression> {
    private OWLClass ce = null;

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDisjointUnion(OWLDisjointUnionAxiom expected) {
        withClass(expected.getOWLClass()).withItems(expected.getClassExpressions())
                .withAnnotations(expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderDisjointUnion() {}

    /** @param arg
     *            right hand entity
     * @return builder */
    public BuilderDisjointUnion withClass(OWLClass arg) {
        ce = arg;
        return this;
    }

    @Override
    public OWLDisjointUnionAxiom buildObject() {
        return df.getOWLDisjointUnionAxiom(ce, items, annotations);
    }
}
