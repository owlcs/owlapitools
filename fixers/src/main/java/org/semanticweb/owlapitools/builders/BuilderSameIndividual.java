package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;

/** Builder class for OWLSameIndividualAxiom */
public class BuilderSameIndividual extends
        BaseSetBuilder<OWLSameIndividualAxiom, BuilderSameIndividual, OWLIndividual> {
    @Override
    public OWLSameIndividualAxiom buildObject() {
        return df.getOWLSameIndividualAxiom(items, annotations);
    }
}
