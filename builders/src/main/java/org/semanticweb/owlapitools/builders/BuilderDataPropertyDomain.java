package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;

/** Builder class for OWLDataPropertyDomainAxiom */
public class BuilderDataPropertyDomain
        extends
        BaseDomainBuilder<OWLDataPropertyDomainAxiom, BuilderDataPropertyDomain, OWLDataPropertyExpression> {
    @Override
    public OWLDataPropertyDomainAxiom buildObject() {
        return df.getOWLDataPropertyDomainAxiom(property, domain, annotations);
    }
}
