package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;

/** Builder class for OWLAnnotationPropertyDomainAxiom */
public class BuilderAnnotationPropertyDomain
        extends
        BaseAnnotationtPropertyBuilder<OWLAnnotationPropertyDomainAxiom, BuilderAnnotationPropertyDomain> {
    private IRI domain = null;

    /** uninitialized builder */
    public BuilderAnnotationPropertyDomain() {}

    /** builder initialized from an existing object
     * 
     * @param expected
     *            the existing object */
    public BuilderAnnotationPropertyDomain(OWLAnnotationPropertyDomainAxiom expected) {
        withProperty(expected.getProperty()).withDomain(expected.getDomain())
                .withAnnotations(expected.getAnnotations());
    }

    /** @param arg
     *            domain
     * @return builder */
    public BuilderAnnotationPropertyDomain withDomain(IRI arg) {
        domain = arg;
        return this;
    }

    @Override
    public OWLAnnotationPropertyDomainAxiom buildObject() {
        return df.getOWLAnnotationPropertyDomainAxiom(property, domain, annotations);
    }
}
