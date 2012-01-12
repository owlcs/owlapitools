package org.coode.suggestor.impl;
import java.util.HashSet;
import java.util.Set;
import java.util.*;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLDatatypeNodeSet;
import org.coode.suggestor.util.ReasonerHelper;
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