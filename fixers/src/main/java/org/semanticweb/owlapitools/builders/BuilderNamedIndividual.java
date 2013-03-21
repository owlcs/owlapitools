package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLNamedIndividual;

/** Builder class for OWLNamedIndividual */
public class BuilderNamedIndividual extends
        BaseEntityBuilder<OWLNamedIndividual, BuilderNamedIndividual> {
    @Override
    public OWLNamedIndividual buildObject() {
        if (pm != null && string != null) {
            return df.getOWLNamedIndividual(string, pm);
        }
        return df.getOWLNamedIndividual(iri);
    }
}
