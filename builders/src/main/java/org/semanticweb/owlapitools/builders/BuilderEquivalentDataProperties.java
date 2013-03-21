package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;

/** Builder class for OWLEquivalentDataPropertiesAxiom */
public class BuilderEquivalentDataProperties
        extends
        BaseSetBuilder<OWLEquivalentDataPropertiesAxiom, BuilderEquivalentDataProperties, OWLDataPropertyExpression> {
    @Override
    public OWLEquivalentDataPropertiesAxiom buildObject() {
        return df.getOWLEquivalentDataPropertiesAxiom(items, annotations);
    }
}
