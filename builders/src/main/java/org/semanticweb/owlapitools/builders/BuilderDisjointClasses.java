package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;

/** Builder class for OWLDisjointClassesAxiom */
public class BuilderDisjointClasses
        extends
        BaseSetBuilder<OWLDisjointClassesAxiom, BuilderDisjointClasses, OWLClassExpression> {
    @Override
    public OWLDisjointClassesAxiom buildObject() {
        return df.getOWLDisjointClassesAxiom(items, annotations);
    }
}
