package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;

/** Builder class for OWLObjectOneOf */
public class BuilderOneOf extends
        BaseSetBuilder<OWLObjectOneOf, BuilderOneOf, OWLIndividual> {
    @Override
    public OWLObjectOneOf buildObject() {
        return df.getOWLObjectOneOf(items);
    }
}
