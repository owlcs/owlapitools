package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;

/** Builder class for OWLObjectOneOf */
public class BuilderOneOf extends
        BaseSetBuilder<OWLObjectOneOf, BuilderOneOf, OWLIndividual> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderOneOf(OWLObjectOneOf expected) {
        withItems(expected.getIndividuals());
    }

    /** uninitialized builder */
    public BuilderOneOf() {}

    @Override
    public OWLObjectOneOf buildObject() {
        return df.getOWLObjectOneOf(items);
    }
}
