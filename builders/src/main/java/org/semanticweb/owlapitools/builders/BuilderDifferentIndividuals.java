package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;

/** Builder class for OWLDifferentIndividualsAxiom */
public class BuilderDifferentIndividuals
        extends
        BaseSetBuilder<OWLDifferentIndividualsAxiom, BuilderDifferentIndividuals, OWLIndividual> {
    @Override
    public OWLDifferentIndividualsAxiom buildObject() {
        return df.getOWLDifferentIndividualsAxiom(items, annotations);
    }
}
