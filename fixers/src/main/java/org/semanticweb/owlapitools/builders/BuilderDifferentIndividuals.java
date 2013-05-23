package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;

/** Builder class for OWLDifferentIndividualsAxiom */
public class BuilderDifferentIndividuals
        extends
        BaseSetBuilder<OWLDifferentIndividualsAxiom, BuilderDifferentIndividuals, OWLIndividual> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDifferentIndividuals(OWLDifferentIndividualsAxiom expected) {
        withItems(expected.getIndividuals()).withAnnotations(expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderDifferentIndividuals() {}

    @Override
    public OWLDifferentIndividualsAxiom buildObject() {
        return df.getOWLDifferentIndividualsAxiom(items, annotations);
    }
}
