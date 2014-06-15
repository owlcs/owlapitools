package org.coode.suggestor.impl;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationValueVisitorEx;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;

/** @author ignazio */
public class IRIMatcher implements OWLAnnotationValueVisitorEx<Boolean> {

    private final IRI propertyIRI;

    @SuppressWarnings("null")
    @Nonnull
    private static Boolean b(boolean b) {
        return Boolean.valueOf(b);
    }

    /**
     * @param propertyIRI
     *        propertyIRI
     */
    public IRIMatcher(IRI propertyIRI) {
        this.propertyIRI = propertyIRI;
    }

    @Override
    public Boolean visit(IRI iri) {
        return b(iri.equals(propertyIRI));
    }

    @Override
    public Boolean visit(OWLAnonymousIndividual owlAnonymousIndividual) {
        return b(false);
    }

    @Override
    public Boolean visit(OWLLiteral owlLiteral) {
        try {
            IRI vIRI = IRI.create(new URI(owlLiteral.getLiteral()));
            if (vIRI.equals(propertyIRI)) {
                return b(true);
            }
        } catch (URISyntaxException e) {
            // do nothing - not a URI
        }
        return b(false);
    }
}
