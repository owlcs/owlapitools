package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataUnionOf;

/** Builder class for OWLDataUnionOf */
public class BuilderDataUnionOf extends
        BaseSetBuilder<OWLDataUnionOf, BuilderDataUnionOf, OWLDataRange> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDataUnionOf(OWLDataUnionOf expected) {
        withItems(expected.getOperands());
    }

    /** uninitialized builder */
    public BuilderDataUnionOf() {}

    @Override
    public OWLDataUnionOf buildObject() {
        return df.getOWLDataUnionOf(items);
    }
}
