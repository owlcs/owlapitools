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
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.coode.suggestor.api.FillerSanctionRule;
import org.coode.suggestor.api.FillerSuggestor;
import org.coode.suggestor.util.ReasonerHelper;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLDatatypeNode;
import org.semanticweb.owlapi.reasoner.impl.OWLDatatypeNodeSet;
import org.semanticweb.owlapi.reasoner.knowledgeexploration.OWLKnowledgeExplorerReasoner;
import org.semanticweb.owlapi.reasoner.knowledgeexploration.OWLKnowledgeExplorerReasoner.RootNode;

/** Default implementation of the FillerSuggestor. */
class FillerSuggestorImpl implements FillerSuggestor {

    protected final OWLReasoner r;
    protected final OWLDataFactory df;
    protected final ReasonerHelper helper;
    private final Set<FillerSanctionRule> sanctioningRules = new HashSet<>();
    private final AbstractMatcher currentMatcher = new AbstractMatcher() {

        @Override
        public boolean isMatch(OWLClassExpression c, OWLPropertyExpression p, OWLPropertyRange f) {
            return helper.isDescendantOf(c, expressionSome(p, f));
        }
    };
    private final AbstractMatcher possibleMatcher = new AbstractMatcher() {

        @Override
        public boolean isMatch(OWLClassExpression c, OWLPropertyExpression p, OWLPropertyRange f) {
            return !r.isSatisfiable(df.getOWLObjectIntersectionOf(c, expressionAll(p, f)));
        }
    };

    OWLClassExpression expressionAll(OWLPropertyExpression p, OWLPropertyRange f) {
        if (p.isOWLDataProperty()) {
            return df.getOWLDataAllValuesFrom(p.asOWLDataProperty(), df.getOWLDataComplementOf((OWLDataRange) f));
        }
        return df.getOWLObjectAllValuesFrom(p.asObjectPropertyExpression(), df.getOWLObjectComplementOf(
            (OWLClassExpression) f));
    }

    OWLClassExpression expressionSome(OWLPropertyExpression p, OWLPropertyRange f) {
        if (p.isOWLDataProperty()) {
            return df.getOWLDataSomeValuesFrom(p.asOWLDataProperty(), (OWLDataRange) f);
        }
        return df.getOWLObjectSomeValuesFrom(p.asObjectPropertyExpression(), (OWLClassExpression) f);
    }

    public FillerSuggestorImpl(OWLReasoner r) {
        this.r = r;
        df = r.getRootOntology().getOWLOntologyManager().getOWLDataFactory();
        helper = new ReasonerHelper(r);
    }

    @Override
    public void addSanctionRule(FillerSanctionRule rule) {
        sanctioningRules.add(rule);
        rule.setSuggestor(this);
    }

    @Override
    public void removeSanctionRule(FillerSanctionRule rule) {
        sanctioningRules.remove(rule);
        rule.setSuggestor(null);
    }

    @Override
    public OWLReasoner getReasoner() {
        return r;
    }

    // BOOLEAN TESTS
    @Override
    public <T extends OWLPropertyExpression> boolean isCurrent(OWLClassExpression c, T p, OWLPropertyRange f) {
        return currentMatcher.isMatch(c, p, f);
    }

    @Override
    public <T extends OWLPropertyExpression> boolean isCurrent(OWLClassExpression c, T p, OWLPropertyRange f,
        boolean direct) {
        return currentMatcher.isMatch(c, p, f, direct);
    }

    @Override
    public <T extends OWLPropertyExpression> boolean isPossible(OWLClassExpression c, T p, OWLPropertyRange f) {
        return possibleMatcher.isMatch(c, p, f);
    }

    @Override
    public <T extends OWLPropertyExpression> boolean isSanctioned(OWLClassExpression c, T p, OWLPropertyRange f) {
        return isPossible(c, p, f) && meetsSanctions(c, p, f);
    }

    @Override
    public boolean isRedundant(OWLClassExpression c, OWLObjectPropertyExpression p, OWLClassExpression f) {
        if (isCurrent(c, p, f)) {
            return true;
        }
        // check the direct subclasses
        return r.getSubClasses(f, true).nodes()
            .map(node -> node.getRepresentativeElement())
            .anyMatch(sub -> isCurrent(c, p, sub) || helper.isDescendantOf(c, df.getOWLObjectAllValuesFrom(p, sub)));
    }

    // GETTERS
    @Override
    public NodeSet<OWLClass> getCurrentNamedFillers(OWLClassExpression c, OWLObjectPropertyExpression p,
        boolean direct) {
        return (NodeSet<OWLClass>) currentMatcher.getLeaves(c, p, helper.getGlobalAssertedRange(p), direct);
    }

    @Override
    public NodeSet<OWLClass> getPossibleNamedFillers(OWLClassExpression c, OWLObjectPropertyExpression p,
        @Nullable OWLClassExpression root, boolean direct) {
        return (NodeSet<OWLClass>) possibleMatcher.getRoots(c, p, root == null ? helper.getGlobalAssertedRange(p)
            : root, direct);
    }

    @Override
    public Set<OWLClass> getSanctionedFillers(OWLClassExpression c, OWLObjectPropertyExpression p,
        OWLClassExpression root, boolean direct) {
        return asSet(getPossibleNamedFillers(c, p, root, direct).entities().filter(f -> meetsSanctions(c, p, f)));
    }

    // INTERNALS
    private boolean meetsSanctions(OWLClassExpression c, OWLPropertyExpression p, OWLPropertyRange f) {
        return sanctioningRules.stream().anyMatch(rule -> rule.meetsSanction(c, p, f));
    }

    // DELEGATES
    private abstract class AbstractMatcher {

        public AbstractMatcher() {}

        public abstract boolean isMatch(OWLClassExpression c, OWLPropertyExpression p, OWLPropertyRange f);

        public boolean isMatch(OWLClassExpression c, OWLPropertyExpression p, OWLPropertyRange f, boolean direct) {
            boolean match = isMatch(c, p, f);
            if (!direct) {
                return match;
            }
            if (!match) {
                return false;
            }
            return getDirectSubs(f).nodes()
                .map(node -> node.getRepresentativeElement())
                .anyMatch(e -> isMatch(c, p, e));
        }

        /**
         * Perform a search on the direct subs of start, adding nodes that
         * match. If direct is false then recurse into descendants of start
         * 
         * @param c
         *        class
         * @param p
         *        property
         * @param start
         *        start
         * @param direct
         *        direct
         * @return set of root nodes
         */
        public NodeSet<? extends OWLPropertyRange> getRoots(OWLClassExpression c, OWLPropertyExpression p,
            OWLPropertyRange start, boolean direct) {
            Set<Node<? extends OWLPropertyRange>> nodes = new HashSet<>();
            getDirectSubs(start).nodes()
                .filter(sub -> isMatch(c, p, sub.getRepresentativeElement()))
                .forEach(sub -> collect(c, p, direct, nodes, sub));
            return createNodeSet(nodes.stream(), p);
        }

        protected void collect(OWLClassExpression c, OWLPropertyExpression p, boolean direct,
            Set<Node<? extends OWLPropertyRange>> nodes, Node<? extends OWLPropertyRange> sub) {
            nodes.add(sub);
            if (!direct) {
                add(nodes, getRoots(c, p, sub.getRepresentativeElement(), direct).nodes());
            }
        }

        protected NodeSet<? extends OWLPropertyRange> getDirectSubs(OWLPropertyRange f) {
            if (f instanceof OWLClassExpression) {
                return r.getSubClasses((OWLClassExpression) f, true);
            }
            return helper.getSubtypes((OWLDataRange) f);
        }

        protected Node<? extends OWLPropertyRange> getEquivalents(OWLPropertyRange f) {
            if (f instanceof OWLClassExpression) {
                return r.getEquivalentClasses((OWLClassExpression) f);
            }
            return helper.getEquivalentTypes((OWLDataRange) f);
        }

        protected NodeSet<? extends OWLPropertyRange> createNodeSet(Stream<Node<? extends OWLPropertyRange>> nodes,
            OWLPropertyExpression p) {
            if (p.isOWLDataProperty()) {
                return new OWLDatatypeNodeSet(nodes.map(n -> (Node<OWLDatatype>) n));
            }
            List<Node<? extends OWLPropertyRange>> l = asList(nodes);
            return new OWLClassNodeSet(l.stream().map(n -> (Node<OWLClass>) n));
        }

        /**
         * Perform a recursive search, adding nodes that match. If direct is
         * true only add nodes if they have no subs that match
         * 
         * @param c
         *        class
         * @param p
         *        property
         * @param start
         *        start
         * @param direct
         *        direct
         * @return set of leave nodes
         */
        public NodeSet<? extends OWLPropertyRange> getLeaves(OWLClassExpression c, OWLPropertyExpression p,
            OWLPropertyRange start, boolean direct) {
            if (!(r instanceof OWLKnowledgeExplorerReasoner)) {
                Set<Node<? extends OWLPropertyRange>> nodes = new HashSet<>();
                if (isMatch(c, p, start)) {
                    getDirectSubs(start)
                        .forEach(sub -> add(nodes, getLeaves(c, p, sub.getRepresentativeElement(), direct).nodes()));
                    if (!direct || nodes.isEmpty() && !start.isTopEntity()) {
                        nodes.add(getEquivalents(start));
                        // non-optimal as we already had the node before
                        // recursing
                    }
                }
                return createNodeSet(nodes.stream(), p);
            }
            // TODO anonymous expressions in returned nodes; currently filtered
            // out
            OWLKnowledgeExplorerReasoner reasoner = (OWLKnowledgeExplorerReasoner) r;
            RootNode root = reasoner.getRoot(c);
            if (p.isOWLDataProperty()) {
                Stream<Node<? extends OWLPropertyRange>> toReturn = dataProperties(reasoner, root)
                    .filter(p1 -> p1 != null)
                    .flatMap(p1 -> dataFillers(p1, reasoner, root))
                    .map(pointer -> dataLabel(direct, reasoner, pointer));
                return createNodeSet(toReturn, p);
            }
            Stream<Node<? extends OWLPropertyRange>> toReturn = objectProperties(reasoner, root)
                .filter(p1 -> p1 != null)
                .flatMap(p1 -> objectFillers(p1, reasoner, root))
                .map(pointer -> objectLabel(direct, reasoner, pointer));
            return createNodeSet(toReturn, p);
        }

        protected Stream<RootNode> dataFillers(OWLPropertyExpression p1,
            OWLKnowledgeExplorerReasoner reasoner, RootNode root) {
            return reasoner.getDataNeighbours(root, p1.asOWLDataProperty()).stream();
        }

        protected Stream<RootNode> objectFillers(OWLPropertyExpression p1,
            OWLKnowledgeExplorerReasoner reasoner, RootNode root) {
            return reasoner.getObjectNeighbours(root, p1.asOWLObjectProperty()).stream();
        }

        protected Stream<OWLDataProperty> dataProperties(OWLKnowledgeExplorerReasoner reasoner, RootNode root) {
            return reasoner.getDataNeighbours(root, true).entities();
        }

        protected Stream<? extends OWLObjectPropertyExpression> objectProperties(OWLKnowledgeExplorerReasoner reasoner,
            RootNode root) {
            return reasoner.getObjectNeighbours(root, true).entities();
        }

        protected Node<OWLDatatype> dataLabel(boolean direct, OWLKnowledgeExplorerReasoner reasoner, RootNode pointer) {
            return new OWLDatatypeNode(reasoner.getDataLabel(pointer, direct).entities()
                .filter(c1 -> c1 != null)
                .map(c1 -> c1.asOWLDatatype()));
        }

        protected Node<OWLClass> objectLabel(boolean direct, OWLKnowledgeExplorerReasoner reasoner, RootNode pointer) {
            return new OWLClassNode(reasoner.getObjectLabel(pointer, direct).entities()
                .filter(c1 -> c1 != null)
                .map(c1 -> c1.asOWLClass()));
        }
    }
}
