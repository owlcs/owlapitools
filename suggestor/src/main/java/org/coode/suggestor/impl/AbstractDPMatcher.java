package org.coode.suggestor.impl;

import java.util.Set;

import org.coode.suggestor.util.ReasonerHelper;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLDatatypeNodeSet;

abstract class AbstractDPMatcher extends
        AbstractMatcher<OWLDataRange, OWLDatatype, OWLDataPropertyExpression> {
    private ReasonerHelper helper;

    public AbstractDPMatcher(ReasonerHelper h) {
        this.helper = h;
    }

    @Override
    protected final NodeSet<OWLDatatype> getDirectSubs(OWLDataRange r) {
        return helper.getSubtypes(r);
    }

    @Override
    protected Node<OWLDatatype> getEquivalents(OWLDataRange range) {
        return helper.getEquivalentTypes(range);
    }

    @Override
    protected NodeSet<OWLDatatype> createNodeSet(Set<Node<OWLDatatype>> nodes) {
        return new OWLDatatypeNodeSet(nodes);
    }
}
