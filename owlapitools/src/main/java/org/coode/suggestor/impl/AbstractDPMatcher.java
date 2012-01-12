package org.coode.suggestor.impl;

import java.util.HashSet;
import java.util.Set;

import org.coode.suggestor.util.ReasonerHelper;

import java.util.*;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLDatatypeNodeSet;


abstract class AbstractDPMatcher extends
		AbstractMatcher<OWLDataRange, OWLDatatype, OWLDataPropertyExpression> {
	private ReasonerHelper helper;

	public AbstractDPMatcher(ReasonerHelper h) {this.helper=h;}

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
