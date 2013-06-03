package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;

/** Builder class for OWLObjectIntersectionOf */
public class BuilderObjectIntersectionOf
        extends
        BaseSetBuilder<OWLObjectIntersectionOf, BuilderObjectIntersectionOf, OWLClassExpression> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderObjectIntersectionOf(OWLObjectIntersectionOf expected) {
        withItems(expected.getOperands());
    }

    /** uninitialized builder */
    public BuilderObjectIntersectionOf() {}

    @Override
    public OWLObjectIntersectionOf buildObject() {
        return df.getOWLObjectIntersectionOf(items);
    }
}
