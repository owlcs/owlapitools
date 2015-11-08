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

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.coode.suggestor.api.PropertySanctionRule;
import org.coode.suggestor.api.PropertySuggestor;
import org.coode.suggestor.util.ReasonerHelper;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;

/** Default implementation of the PropertySuggestor. */
class PropertySuggestorImpl implements PropertySuggestor {

    protected final OWLReasoner r;
    protected final OWLDataFactory df;
    protected final ReasonerHelper helper;
    private final Set<PropertySanctionRule> sanctionRules = new HashSet<>();
    private final AbstractMatcher currentMatcher = new AbstractMatcher() {

        @Override
        public <P extends OWLPropertyExpression> boolean isMatch(OWLClassExpression c, P p) {
            if (p.isTopEntity()) {
                return true;
            }
            return helper.isDescendantOf(c, expressionSome(p));
        }
    };

    OWLClassExpression expressionSome(OWLPropertyExpression p) {
        if (p.isOWLDataProperty()) {
            return df.getOWLDataSomeValuesFrom(p.asOWLDataProperty(), df.getTopDatatype());
        }
        return df.getOWLObjectSomeValuesFrom(p.asObjectPropertyExpression(), df.getOWLThing());
    }

    private AbstractMatcher possibleMatcher = new AbstractMatcher() {

        @Override
        public <P extends OWLPropertyExpression> boolean isMatch(OWLClassExpression c, P p) {
            if (p.isOWLDataProperty()) {
                return r.isSatisfiable(df.getOWLObjectIntersectionOf(c, expressionSome(p)));
            }
            return !r.isEntailed(df.getOWLSubClassOfAxiom(c, df.getOWLObjectAllValuesFrom(p
                .asObjectPropertyExpression(), df.getOWLNothing())));
        }
    };

    public PropertySuggestorImpl(OWLReasoner r) {
        this.r = r;
        df = r.getRootOntology().getOWLOntologyManager().getOWLDataFactory();
        helper = new ReasonerHelper(r);
    }

    @Override
    public void addSanctionRule(PropertySanctionRule rule) {
        sanctionRules.add(rule);
        rule.setSuggestor(this);
    }

    @Override
    public void removeSanctionRule(PropertySanctionRule rule) {
        sanctionRules.remove(rule);
        rule.setSuggestor(null);
    }

    @Override
    public OWLReasoner getReasoner() {
        return r;
    }

    // BOOLEAN TESTS
    @Override
    public <T extends OWLPropertyExpression> boolean isCurrent(OWLClassExpression c, T p) {
        return currentMatcher.isMatch(c, p);
    }

    @Override
    public <T extends OWLPropertyExpression> boolean isCurrent(OWLClassExpression c, T p, boolean direct) {
        return currentMatcher.isMatch(c, p, direct);
    }

    @Override
    public <T extends OWLPropertyExpression> boolean isPossible(OWLClassExpression c, T p) {
        return possibleMatcher.isMatch(c, p);
    }

    @Override
    public <T extends OWLPropertyExpression> boolean isSanctioned(OWLClassExpression c, T p) {
        return isPossible(c, p) && meetsSanctions(c, p);
    }

    // GETTERS
    @Override
    public <T extends OWLPropertyExpression> NodeSet<T> getCurrentProperties(OWLClassExpression c,
        Class<T> propertyType, boolean direct) {
        Node<T> root = OWLDataProperty.class.isAssignableFrom(propertyType) ? (Node<T>) r.getTopDataPropertyNode()
            : (Node<T>) r.getTopObjectPropertyNode();
        return currentMatcher.getLeaves(c, root, direct);
    }

    @Override
    public <T extends OWLPropertyExpression> NodeSet<T> getPossibleProperties(OWLClassExpression c, T root,
        boolean direct) {
        Node<T> rootNode = root.isOWLDataProperty() ? (Node<T>) r.getEquivalentDataProperties(root.asOWLDataProperty())
            : (Node<T>) r.getEquivalentObjectProperties(root.asObjectPropertyExpression());
        return possibleMatcher.getRoots(c, rootNode, direct);
    }

    @Override
    public <T extends OWLPropertyExpression> Set<T> getSanctionedProperties(OWLClassExpression c, T root,
        boolean direct) {
        return asSet(getPossibleProperties(c, root, direct).entities().filter(pNode -> meetsSanctions(c,
            pNode)));
    }

    // INTERNALS
    private <T extends OWLPropertyExpression> boolean meetsSanctions(OWLClassExpression c, T p) {
        return sanctionRules.stream().anyMatch(rule -> rule.meetsSanction(c, p));
    }

    // DELEGATES
    private abstract class AbstractMatcher {

        public AbstractMatcher() {}

        public abstract <P extends OWLPropertyExpression> boolean isMatch(OWLClassExpression c, P p);

        public <P extends OWLPropertyExpression> boolean isMatch(OWLClassExpression c, P p, boolean direct) {
            boolean match = isMatch(c, p);
            if (!direct) {
                return match;
            }
            if (!match) {
                return false;
            }
            return !getDirectSubs(p).nodes()
                .anyMatch(node -> isMatch(c, node.getRepresentativeElement()));
        }

        /*
         * Perform a recursive search, adding nodes that match and if direct is
         * true only if they have no subs that match
         */
        public <P extends OWLPropertyExpression> NodeSet<P> getLeaves(OWLClassExpression c, Node<P> root,
            boolean direct) {
            Set<Node<P>> nodes = new HashSet<>();
            P p = root.getRepresentativeElement();
            if (isMatch(c, p)) {
                for (Node<P> sub : getDirectSubs(p)) {
                    add(nodes, getLeaves(c, sub, direct).nodes());
                }
                if (!direct || nodes.isEmpty() && !root.isTopNode()) {
                    nodes.add(root);
                }
            }
            return createNodeSet(nodes.stream(), p);
        }

        /*
         * Perform a search on the direct subs of start, adding nodes that
         * match. If not direct then recurse
         */
        public <P extends OWLPropertyExpression> NodeSet<P> getRoots(OWLClassExpression c, Node<P> root,
            boolean direct) {
            Set<Node<P>> nodes = new HashSet<>();
            P p = root.getRepresentativeElement();
            for (Node<P> sub : getDirectSubs(p)) {
                if (isMatch(c, sub.getRepresentativeElement())) {
                    nodes.add(sub);
                    if (!direct) {
                        add(nodes, getRoots(c, sub, direct).nodes());
                    }
                }
            }
            return createNodeSet(nodes.stream(), p);
        }

        protected <T extends OWLPropertyExpression> NodeSet<T> getDirectSubs(T p) {
            return p.isDataPropertyExpression() ? (NodeSet<T>) r.getSubDataProperties(p.asOWLDataProperty(), true)
                : (NodeSet<T>) r.getSubObjectProperties(p.asOWLObjectProperty(), true);
        }

        protected <P extends OWLPropertyExpression> NodeSet<P> createNodeSet(Stream<Node<P>> nodes, P p) {
            if (p.isDataPropertyExpression()) {
                return (NodeSet<P>) new OWLDataPropertyNodeSet(nodes.map(n -> (Node<OWLDataProperty>) n));
            }
            return (NodeSet<P>) new OWLObjectPropertyNodeSet(nodes.map(n -> (Node<OWLObjectPropertyExpression>) n));
        }
    }
}
