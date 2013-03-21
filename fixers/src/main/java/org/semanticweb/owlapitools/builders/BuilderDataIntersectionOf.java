package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataRange;

/** Builder class for OWLDataIntersectionOf */
public class BuilderDataIntersectionOf extends
        BaseSetBuilder<OWLDataIntersectionOf, BuilderDataIntersectionOf, OWLDataRange> {
    @Override
    public OWLDataIntersectionOf buildObject() {
        return df.getOWLDataIntersectionOf(items);
    }
}
