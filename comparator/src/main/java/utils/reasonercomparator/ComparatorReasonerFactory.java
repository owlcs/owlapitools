/*
 * Date: Jan 13, 2012
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2012, Ignazio Palmisano, University of Manchester
 *
 */
package utils.reasonercomparator;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/** A reasoner factory for comparing reasoners
 * 
 * @author ignazio */
public class ComparatorReasonerFactory implements OWLReasonerFactory {
    /** HermiT class name */
    public static final String HERMIT = "org.semanticweb.HermiT.Reasoner.ReasonerFactory";
    /** FaCT++ class name */
    public static final String FACT = "uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory";
    /** JFact class name */
    public static final String JFACT = "uk.ac.manchester.cs.jfact.JFactFactory";

    private static OWLReasonerFactory getFactory(String s) {
        try {
            return (OWLReasonerFactory) Class.forName(s).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Problem instantiating factory: " + s, e);
        }
    }

    @Override
    public String getReasonerName() {
        return "ComparatorReasoner";
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
        return new ComparisonReasoner(ontology, null, getFactory(HERMIT),
                getFactory(JFACT), getFactory(FACT));
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology) {
        return new ComparisonReasoner(ontology, null, getFactory(HERMIT),
                getFactory(JFACT), getFactory(FACT));
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology,
            OWLReasonerConfiguration config) throws IllegalConfigurationException {
        return new ComparisonReasoner(ontology, config, getFactory(HERMIT),
                getFactory(JFACT), getFactory(FACT));
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology,
            OWLReasonerConfiguration config) throws IllegalConfigurationException {
        return new ComparisonReasoner(ontology, config, getFactory(HERMIT),
                getFactory(JFACT), getFactory(FACT));
    }
}
