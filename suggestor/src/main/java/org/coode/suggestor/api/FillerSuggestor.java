/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
@author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.api;

import java.util.Set;

import javax.annotation.Nullable;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * The FillerSuggestor allows us to explore the relationships between the
 * classes in the ontology. <br>
 * For more general discussion of the suggestor idea please see the
 * <a href="package-summary.html">package summary</a> <br>
 * To help with the notion of "property values" or "local ranges". <br>
 * Filler level questions - Given a class description and an object property:
 * <br>
 * <ol>
 * <li>What are the named fillers on these properties?</li>
 * <li>What are the possible named fillers for a new existential restriction?
 * </li>
 * <li>What are the sanctioned named fillers for a new existential restriction?
 * </li>
 * </ol>
 * The following definitions are used in the API definition:
 * <h3>Direct</h3> The direct flag is used to control redundancy. If filler f
 * holds for a query in the general case then the direct case only holds if
 * there is no g where StrictSubClassOf(g, f) and isCurrent(c, p, g) is entailed
 * <br>
 * For the definition of StrictSubClassOf see the OWLAPI
 * {@link org.semanticweb.owlapi.reasoner.OWLReasoner}.
 */
public interface FillerSuggestor {

    /**
     * @param c
     *        a class expression
     * @param p
     *        an object property
     * @param f
     *        a filler class expression
     * @return true if SubClassOf(c, p some f) is entailed
     */
    boolean isCurrent(OWLClassExpression c, OWLObjectPropertyExpression p, OWLClassExpression f);

    /**
     * @param c
     *        a class expression
     * @param p
     *        an object property
     * @param f
     *        a filler class expression
     * @param direct
     *        (see definition above)
     * @return isCurrent(c, p, f). If direct then there is no g where
     *         StrictSubClassOf(g, f) and isCurrent(c, p, g) is true
     */
    boolean isCurrent(OWLClassExpression c, OWLObjectPropertyExpression p, OWLClassExpression f, boolean direct);

    /**
     * @param c
     *        a class expression
     * @param p
     *        a data property
     * @param f
     *        a filler data range
     * @return true if SubClassOf(c, p some f) is entailed
     */
    boolean isCurrent(OWLClassExpression c, OWLDataProperty p, OWLDataRange f);

    // TODO: how do we determine if there is a more specific range on c??
    /**
     * @param c
     *        class
     * @param p
     *        property
     * @param f
     *        filler
     * @param direct
     *        direct
     * @return true if current
     */
    boolean isCurrent(OWLClassExpression c, OWLDataProperty p, OWLDataRange f, boolean direct);

    /**
     * @param c
     *        a class expression
     * @param p
     *        an object property
     * @param f
     *        a filler class expression
     * @return true if isSatisfiable(c and p some f)
     */
    boolean isPossible(OWLClassExpression c, OWLObjectPropertyExpression p, OWLClassExpression f);

    /**
     * @param c
     *        a class expression
     * @param p
     *        a data property
     * @param f
     *        a filler data range
     * @return true if isSatisfiable(c and p some f)
     */
    boolean isPossible(OWLClassExpression c, OWLDataProperty p, OWLDataRange f);

    /**
     * @param c
     *        a class expression
     * @param p
     *        an object property
     * @param f
     *        a filler class expression
     * @return true if isPossible(c, p, f) and ANY filler sanction rule is met
     */
    boolean isSanctioned(OWLClassExpression c, OWLObjectPropertyExpression p, OWLClassExpression f);

    /**
     * @param c
     *        a class expression
     * @param p
     *        a data property
     * @param f
     *        a filler data range
     * @return true if isPossible(c, p, f) and ANY filler sanction rule is met
     */
    boolean isSanctioned(OWLClassExpression c, OWLDataProperty p, OWLDataRange f);

    /**
     * Roughly speaking, would adding SubClassOf(c, p some f) fail to usefully
     * "specialise" c
     * 
     * @param c
     *        a class expression
     * @param p
     *        an object property
     * @param f
     *        a filler class expression
     * @return isCurrent(c, p, f) or there is a g such that StrictSubClassOf(g,
     *         f) and isCurrent(c, p, g) or SubClassOf(c, p only g)
     */
    boolean isRedundant(OWLClassExpression c, OWLObjectPropertyExpression p, OWLClassExpression f);

    /**
     * @param c
     *        a class expression
     * @param p
     *        an object property
     * @param direct
     *        (see definition above)
     * @return a set of named class fillers where every f satisfies isCurrent(c,
     *         p, f, direct)
     */
    NodeSet<OWLClass> getCurrentNamedFillers(OWLClassExpression c, OWLObjectPropertyExpression p, boolean direct);

    /**
     * Find subclasses (or descendants) of root for which isPossible() holds.
     * 
     * @param c
     *        a class expression
     * @param p
     *        an object property
     * @param root
     *        the class from which we start our search
     * @param direct
     *        controls whether subclasses or descendants of root are searched
     * @return a set of named class fillers where every f satisfies
     *         StrictSubClassOf(f, root) or StrictDescendantOf(f, root) and
     *         isPossible(c, p, f)
     */
    NodeSet<OWLClass> getPossibleNamedFillers(OWLClassExpression c, OWLObjectPropertyExpression p,
        @Nullable OWLClassExpression root, boolean direct);

    /**
     * Find subclasses (or descendants) of root for which isSanctioned() holds.
     * 
     * @param c
     *        a class expression
     * @param p
     *        an object property
     * @param root
     *        the class from which we start our search
     * @param direct
     *        controls whether subclasses or descendants of root are searched
     * @return a set of named class fillers where every f satisfies
     *         StrictSubClassOf(f, root) or StrictDescendantOf(f, root) and
     *         isSanctioned(c, p, f, direct)
     */
    Set<OWLClass> getSanctionedFillers(OWLClassExpression c, OWLObjectPropertyExpression p, OWLClassExpression root,
        boolean direct);

    /**
     * Add a FillerSanctionRule to the end of the rules used for sanctioning
     * 
     * @param rule
     *        the rule to add
     */
    void addSanctionRule(FillerSanctionRule rule);

    /**
     * Remove this FillerSanctionRule from the rules used for sanctioning
     * 
     * @param rule
     *        the rule to remove
     */
    void removeSanctionRule(FillerSanctionRule rule);

    /** @return get the reasoner used by this suggestor instance */
    OWLReasoner getReasoner();
}
