package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/** Builder class for OWLEquivalentObjectPropertiesAxiom */
public class BuilderEquivalentObjectProperties
        extends
        BaseSetBuilder<OWLEquivalentObjectPropertiesAxiom, BuilderEquivalentObjectProperties, OWLObjectPropertyExpression> {
    @Override
    public OWLEquivalentObjectPropertiesAxiom buildObject() {
        return df.getOWLEquivalentObjectPropertiesAxiom(items, annotations);
    }
}
