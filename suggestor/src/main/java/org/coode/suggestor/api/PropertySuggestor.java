/**
 * Date: Dec 17, 2007
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2007, The University of Manchester
 *
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics Group
 */
package org.coode.suggestor.api;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * The PropertySuggestor allows us to explore the relationships between the
 * classes and properties in the ontology. <br>
 * For more general discussion of the suggestor idea please see the
 * <a href="package-summary.html">package summary</a><br>
 * Using the suggestor, we can ask the following questions:<br>
 * <ul>
 * <li>Which properties does this class have? (What have we said about this
 * class?)</li>
 * <li>Which properties could we add to this class?</li>
 * <li>Which properties might we be most interested in adding to this class?
 * (Sanctioning)</li>
 * </ul>
 * The following definitions are used in the API definition:<br>
 * <h3>Direct</h3> The direct flag is used to control redundancy. If property p
 * holds for a query in the general case then the direct case only holds if
 * there is no q where StrictSubObjectPropertyOf(q, p) and isCurrent(q, c) is
 * entailed<br>
 * <h3>StrictSub[Object|Data]PropertyOf</h3> For the definition of
 * StrictSubData/ObjectPropertyOf see the OWLAPI
 * {@link org.semanticweb.owlapi.reasoner.OWLReasoner}.
 */
public interface PropertySuggestor {

    /**
     * SubClassOf(c, p some T) is entailed; T can be Thing or top datatype,
     * depending on the input.
     * 
     * @param c
     *        a class expression
     * @param p
     *        a property
     * @param <T>
     *        object or data property;
     * @return true if SubClassOf(c, p some T) is entailed
     */
    <T extends OWLPropertyExpression> boolean isCurrent(OWLClassExpression c, T p);

    /**
     * @param c
     *        a class expression
     * @param p
     *        a property
     * @param direct
     *        (see definition above)
     * @param <T>
     *        object or data property;
     * @return isCurrent(c, p). If direct then there is no q where
     *         StrictSub(Object|Data)PropertyOf(q, p) and isCurrent(c, q) is
     *         entailed
     */
    <T extends OWLPropertyExpression> boolean isCurrent(OWLClassExpression c, T p, boolean direct);

    /**
     * @param c
     *        a class expression
     * @param p
     *        a property
     * @param <T>
     *        object or data property;
     * @return true if isSatisfiable(c and p some (Thing|Top datatype))
     */
    <T extends OWLPropertyExpression> boolean isPossible(OWLClassExpression c, T p);

    /**
     * Determine if property p is sanctioned for class c by iterating through
     * all of the registered property sanction rules until one is successful or
     * all fail. Only possible properties can be sanctioned.
     * 
     * @param c
     *        a class expression
     * @param p
     *        a property
     * @param <T>
     *        object or data property;
     * @return true if isPossible(c, p) and ANY property sanction rule is met
     */
    <T extends OWLPropertyExpression> boolean isSanctioned(OWLClassExpression c, T p);

    /**
     * @param c
     *        a class expression
     * @param direct
     *        (see definition above)
     * @param propertyType
     *        object or data property class
     * @param <T>
     *        object or data property;
     * @return a set of properties where every p satisfies isCurrent(c, p,
     *         direct)
     */
    <T extends OWLPropertyExpression> NodeSet<T> getCurrentProperties(OWLClassExpression c, Class<T> propertyType,
        boolean direct);
    /**
     * @param c
     *        a class expression
     * @param direct
     *        (see definition above)
     * @return a set of objectproperties where every p satisfies isCurrent(c, p,
     *         direct)
     */
    default NodeSet<OWLObjectPropertyExpression> getCurrentObjectProperties(OWLClassExpression c, boolean direct) {
        return getCurrentProperties(c, OWLObjectPropertyExpression.class, direct);
    }
    /**
     * @param c
     *        a class expression
     * @param direct
     *        (see definition above)
     * @return a set of dataproperties where every p satisfies isCurrent(c, p,
     *         direct)
     */
    default NodeSet<OWLDataProperty> getCurrentDataProperties(OWLClassExpression c, boolean direct) {
        return getCurrentProperties(c, OWLDataProperty.class, direct);
    }

    /**
     * @param c
     *        a class expression
     * @param root
     *        a from which we start our search
     * @param direct
     *        whether to search the subclasses or descendants of root
     * @param <T>
     *        object or data property;
     * @return a set of properties where every p satisfies
     *         StrictSubPropertyOf(p, root) or DescendantOf(p, root) and
     *         isPossible(c, p)
     */
    <T extends OWLPropertyExpression> NodeSet<T> getPossibleProperties(OWLClassExpression c, T root, boolean direct);

    /**
     * @param c
     *        a class expression
     * @param root
     *        a from which we start our search
     * @param direct
     *        whether to search the subclasses or descendants of root
     * @param <T>
     *        object or data property;
     * @return a set of properties where every p satisfies
     *         StrictSubPropertyOf(p, root) or DescendantOf(p, root) and
     *         isSanctioned(c, p)
     */
    <T extends OWLPropertyExpression> Set<T> getSanctionedProperties(OWLClassExpression c, T root, boolean direct);

    /**
     * Add a PropertySanctionRule to the end of the rules used for sanctioning
     * 
     * @param rule
     *        the rule to add
     */
    void addSanctionRule(PropertySanctionRule rule);

    /**
     * Remove this PropertySanctionRule from the rules used for sanctioning
     * 
     * @param rule
     *        the rule to remove
     */
    void removeSanctionRule(PropertySanctionRule rule);

    /** @return get the reasoner used by this suggestor instance */
    OWLReasoner getReasoner();
}
