package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/** Builder class for OWLDisjointObjectPropertiesAxiom */
public class BuilderDisjointObjectProperties
        extends
        BaseSetBuilder<OWLDisjointObjectPropertiesAxiom, BuilderDisjointObjectProperties, OWLObjectPropertyExpression> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDisjointObjectProperties(OWLDisjointObjectPropertiesAxiom expected) {
        withItems(expected.getProperties()).withAnnotations(expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderDisjointObjectProperties() {}

    @Override
    public OWLDisjointObjectPropertiesAxiom buildObject() {
        return df.getOWLDisjointObjectPropertiesAxiom(items, annotations);
    }
}
