package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;

/** Builder class for OWLObjectIntersectionOf */
public class BuilderObjectIntersectionOf
        extends
        BaseSetBuilder<OWLObjectIntersectionOf, BuilderObjectIntersectionOf, OWLClassExpression> {
    @Override
    public OWLObjectIntersectionOf buildObject() {
        return df.getOWLObjectIntersectionOf(items);
    }
}
