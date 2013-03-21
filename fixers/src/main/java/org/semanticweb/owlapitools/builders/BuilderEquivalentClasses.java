package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

/** Builder class for OWLEquivalentClassesAxiom */
public class BuilderEquivalentClasses
        extends
        BaseSetBuilder<OWLEquivalentClassesAxiom, BuilderEquivalentClasses, OWLClassExpression> {
    @Override
    public OWLEquivalentClassesAxiom buildObject() {
        return df.getOWLEquivalentClassesAxiom(items, annotations);
    }
}
