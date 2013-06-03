package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/** Builder class for OWLObjectPropertyDomainAxiom */
public class BuilderObjectPropertyDomain
        extends
        BaseDomainBuilder<OWLObjectPropertyDomainAxiom, BuilderObjectPropertyDomain, OWLObjectPropertyExpression> {
    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderObjectPropertyDomain(OWLObjectPropertyDomainAxiom expected) {
        withDomain(expected.getDomain()).withProperty(expected.getProperty())
                .withAnnotations(expected.getAnnotations());
    }

    /** uninitialized builder */
    public BuilderObjectPropertyDomain() {}

    @Override
    public OWLObjectPropertyDomainAxiom buildObject() {
        return df.getOWLObjectPropertyDomainAxiom(property, domain, annotations);
    }
}
