package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;

/** Builder class for OWLObjectComplementOf */
public class BuilderComplementOf extends
        BaseBuilder<OWLObjectComplementOf, BuilderComplementOf> {
    private OWLClassExpression c = null;

    /** @param arg
     *            class expression
     * @return builder */
    public BuilderComplementOf withClass(OWLClassExpression arg) {
        c = arg;
        return this;
    }

    @Override
    public OWLObjectComplementOf buildObject() {
        return df.getOWLObjectComplementOf(c);
    }
}
