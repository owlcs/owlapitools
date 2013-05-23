package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/** Builder class for OWLEquivalentObjectPropertiesAxiom */
public class BuilderEquivalentObjectProperties
        extends
        BaseSetBuilder<OWLEquivalentObjectPropertiesAxiom, BuilderEquivalentObjectProperties, OWLObjectPropertyExpression> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderEquivalentObjectProperties(OWLEquivalentObjectPropertiesAxiom expected) {
        withItems(expected.getProperties()).withAnnotations(expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderEquivalentObjectProperties() {}

    @Override
    public OWLEquivalentObjectPropertiesAxiom buildObject() {
        return df.getOWLEquivalentObjectPropertiesAxiom(items, annotations);
    }
}
