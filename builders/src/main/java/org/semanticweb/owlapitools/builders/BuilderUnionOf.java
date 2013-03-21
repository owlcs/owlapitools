package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

/** Builder class for OWLObjectUnionOf */
public class BuilderUnionOf extends
        BaseSetBuilder<OWLObjectUnionOf, BuilderUnionOf, OWLClassExpression> {
    @Override
    public OWLObjectUnionOf buildObject() {
        return df.getOWLObjectUnionOf(items);
    }
}
