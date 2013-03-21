package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/** Builder class for OWLDisjointObjectPropertiesAxiom */
public class BuilderDisjointObjectProperties
        extends
        BaseSetBuilder<OWLDisjointObjectPropertiesAxiom, BuilderDisjointObjectProperties, OWLObjectPropertyExpression> {
    @Override
    public OWLDisjointObjectPropertiesAxiom buildObject() {
        return df.getOWLDisjointObjectPropertiesAxiom(items, annotations);
    }
}
