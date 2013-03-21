package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;

/** Builder class for OWLDisjointUnionAxiom */
public class BuilderDisjointUnion extends
        BaseSetBuilder<OWLDisjointUnionAxiom, BuilderDisjointUnion, OWLClassExpression> {
    private OWLClass ce = null;

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
