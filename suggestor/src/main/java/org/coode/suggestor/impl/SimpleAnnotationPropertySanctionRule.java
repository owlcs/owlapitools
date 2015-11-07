/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.impl;

import java.util.stream.Stream;

import org.coode.suggestor.api.PropertySanctionRule;
import org.coode.suggestor.api.PropertySuggestor;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.Searcher;

/**
 * Checks the class to see if it has an annotation (specified by the
 * constructor) matching the URI of the property. If recursive is true, then all
 * ancestors of the class are also checked.
 */
public class SimpleAnnotationPropertySanctionRule implements
    PropertySanctionRule {

    private OWLReasoner r;
    private final OWLAnnotationProperty annotationProperty;
    private final boolean recursive;

    /**
     * @param annotationProperty
     *        property to use
     * @param recursive
     *        true if recursive
     */
    public SimpleAnnotationPropertySanctionRule(
        OWLAnnotationProperty annotationProperty, boolean recursive) {
        this.annotationProperty = annotationProperty;
        this.recursive = recursive;
    }

    @Override
    public void setSuggestor(PropertySuggestor ps) {
        r = ps.getReasoner();
    }

    @Override
    public <T extends OWLPropertyExpression> boolean meetsSanction(OWLClassExpression c, T p) {
        return hasAnnotation(c, p);
    }

    private boolean hasAnnotation(OWLClassExpression c, OWLPropertyExpression p) {
        if (p.isAnonymous()) {
            return false;
        }
        if (!c.isAnonymous()
            && hasSanctionAnnotation(c.asOWLClass(), (OWLProperty) p)) {
            return true;
        }
        if (!recursive) {
            return false;
        }
        // check the ancestors
        return r.getSuperClasses(c, true).entities().anyMatch(s -> hasAnnotation(s, p));
    }

    private boolean hasSanctionAnnotation(OWLClass c, OWLProperty p) {
        IRIMatcher iriMatcher = new IRIMatcher(p.getIRI());
        Stream<OWLAnnotationAssertionAxiom> axioms = r.getRootOntology()
            .annotationAssertionAxioms(c.getIRI(), Imports.INCLUDED);
        return Searcher.annotations(axioms, annotationProperty).anyMatch(a -> a.getValue().accept(iriMatcher));
    }
}
