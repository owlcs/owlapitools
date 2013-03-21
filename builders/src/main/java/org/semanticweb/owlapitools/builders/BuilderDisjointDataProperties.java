package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;

/** Builder class for OWLDisjointDataPropertiesAxiom */
public class BuilderDisjointDataProperties
        extends
        BaseSetBuilder<OWLDisjointDataPropertiesAxiom, BuilderDisjointDataProperties, OWLDataPropertyExpression> {
    @Override
    public OWLDisjointDataPropertiesAxiom buildObject() {
        return df.getOWLDisjointDataPropertiesAxiom(items, annotations);
    }
}
