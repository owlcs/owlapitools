package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;

/** Builder class for OWLDataPropertyDomainAxiom */
public class BuilderDataPropertyDomain
        extends
        BaseDomainBuilder<OWLDataPropertyDomainAxiom, BuilderDataPropertyDomain, OWLDataPropertyExpression> {
    /** uninitialized builder */
    public BuilderDataPropertyDomain() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderDataPropertyDomain(OWLDataPropertyDomainAxiom expected) {
        withProperty(expected.getProperty()).withDomain(expected.getDomain())
                .withAnnotations(expected.getAnnotations());
    }

    @Override
    public OWLDataPropertyDomainAxiom buildObject() {
        return df.getOWLDataPropertyDomainAxiom(property, domain, annotations);
    }
}
