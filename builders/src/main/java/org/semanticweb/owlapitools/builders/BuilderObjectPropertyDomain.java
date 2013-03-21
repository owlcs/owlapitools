package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/** Builder class for OWLObjectPropertyDomainAxiom */
public class BuilderObjectPropertyDomain
        extends
        BaseDomainBuilder<OWLObjectPropertyDomainAxiom, BuilderObjectPropertyDomain, OWLObjectPropertyExpression> {
    @Override
    public OWLObjectPropertyDomainAxiom buildObject() {
        return df.getOWLObjectPropertyDomainAxiom(property, domain, annotations);
    }
}
