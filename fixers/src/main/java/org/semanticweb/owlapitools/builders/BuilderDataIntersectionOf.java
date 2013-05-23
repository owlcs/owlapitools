package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataRange;

/** Builder class for OWLDataIntersectionOf */
public class BuilderDataIntersectionOf extends
        BaseSetBuilder<OWLDataIntersectionOf, BuilderDataIntersectionOf, OWLDataRange> {
    /** uninitialized builder */
    public BuilderDataIntersectionOf() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDataIntersectionOf(OWLDataIntersectionOf expected) {
        withItems(expected.getOperands());
    }

    @Override
    public OWLDataIntersectionOf buildObject() {
        return df.getOWLDataIntersectionOf(items);
    }
}
