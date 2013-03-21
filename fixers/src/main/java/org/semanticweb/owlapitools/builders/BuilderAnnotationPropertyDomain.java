package org.semanticweb.owlapitools.builders;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;

/** Builder class for OWLAnnotationPropertyDomainAxiom */
public class BuilderAnnotationPropertyDomain extends
        BaseAnnotationtPropertyBuilder<OWLAnnotationPropertyDomainAxiom, BuilderAnnotationPropertyDomain> {
    private IRI domain = null;


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
