package org.coode.suggestor.impl;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;

abstract class AbstractOPMatcher extends
    AbstractMatcher<OWLClassExpression, OWLClass, OWLObjectPropertyExpression> {

    private OWLReasoner r;

    public AbstractOPMatcher(OWLReasoner r) {
        this.r = r;
    }

    @Override
    protected final NodeSet<OWLClass> getDirectSubs(OWLClassExpression c) {
        return r.getSubClasses(c, true);
    }

    @Override
    protected Node<OWLClass> getEquivalents(OWLClassExpression f) {
        return r.getEquivalentClasses(f);
    }

    @Override
    protected final NodeSet<OWLClass> createNodeSet(Set<Node<OWLClass>> nodes) {
        return new OWLClassNodeSet(nodes);
    }
}
