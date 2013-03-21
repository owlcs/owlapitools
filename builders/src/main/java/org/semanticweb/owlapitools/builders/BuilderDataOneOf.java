package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLLiteral;

/** Builder class for OWLDataOneOf */
public class BuilderDataOneOf extends
        BaseSetBuilder<OWLDataOneOf, BuilderDataOneOf, OWLLiteral> {
    @Override
    public OWLDataOneOf buildObject() {
        return df.getOWLDataOneOf(items);
    }
}
