package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

/** Builder class for OWLObjectUnionOf */
public class BuilderUnionOf extends
        BaseSetBuilder<OWLObjectUnionOf, BuilderUnionOf, OWLClassExpression> {
    /** uninitialized builder */
    public BuilderUnionOf() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderUnionOf(OWLObjectUnionOf expected) {
        withItems(expected.getOperands());
    }

    @Override
    public OWLObjectUnionOf buildObject() {
        return df.getOWLObjectUnionOf(items);
    }
}
