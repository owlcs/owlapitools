package org.coode.suggestor.impl;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.add;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

abstract class AbstractMatcher<R extends OWLPropertyRange, F extends R, P extends OWLPropertyExpression>
    implements Matcher<R, F, P> {

    public AbstractMatcher() {}

    @Override
    public final boolean isMatch(OWLClassExpression c, P p, R f, boolean direct) {
        if (!direct) {
            return isMatch(c, p, f);
        }
        if (!isMatch(c, p, f)) {
            return false;
        }
        NodeSet<F> directSubs = getDirectSubs(f);
        for (Node<F> node : directSubs) {
            F representativeElement = node.getRepresentativeElement();
            if (isMatch(c, p, representativeElement)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final NodeSet<F> getLeaves(OWLClassExpression c, P p, R start,
        boolean direct) {
        Set<Node<F>> nodes = new HashSet<>();
        if (isMatch(c, p, start)) {
            for (Node<F> sub : getDirectSubs(start)) {
                add(nodes, getLeaves(c, p, sub.getRepresentativeElement(), direct).nodes());
            }
            if (!direct || nodes.isEmpty() && !start.isTopEntity()) {
                nodes.add(getEquivalents(start));
                // non-optimal as we already had the node before recursing
            }
        }
        return createNodeSet(nodes);
    }

    @Override
    public final NodeSet<F> getRoots(OWLClassExpression c, P p, R start,
        boolean direct) {
        Set<Node<F>> nodes = new HashSet<>();
        for (Node<F> sub : getDirectSubs(start)) {
            if (isMatch(c, p, sub.getRepresentativeElement())) {
                nodes.add(sub);
                if (!direct) {
                    add(nodes, getRoots(c, p, sub.getRepresentativeElement(), direct).nodes());
                }
            }
        }
        return createNodeSet(nodes);
    }

    protected abstract NodeSet<F> getDirectSubs(R f);

    protected abstract Node<F> getEquivalents(R f);

    protected abstract NodeSet<F> createNodeSet(Set<Node<F>> nodes);
}
