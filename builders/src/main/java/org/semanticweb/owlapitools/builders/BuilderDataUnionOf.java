package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataUnionOf;

/** Builder class for OWLDataUnionOf */
public class BuilderDataUnionOf extends
        BaseSetBuilder<OWLDataUnionOf, BuilderDataUnionOf, OWLDataRange> {
    @Override
    public OWLDataUnionOf buildObject() {
        return df.getOWLDataUnionOf(items);
    }
}
