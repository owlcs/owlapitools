package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

/** Builder class for OWLHasKeyAxiom */
public class BuilderHasKey extends
        BaseSetBuilder<OWLHasKeyAxiom, BuilderHasKey, OWLPropertyExpression<?, ?>> {
    private OWLClassExpression ce;

    /** @param arg
     *            class expression
     * @return builder */
    public BuilderHasKey withClass(OWLClassExpression arg) {
        ce = arg;
        return this;
    }

    @Override
    public OWLHasKeyAxiom buildObject() {
        return df.getOWLHasKeyAxiom(ce, items, annotations);
    }
}
