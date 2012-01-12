package utils.reasonercomparator;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ComparatorReasonerFactory implements OWLReasonerFactory {
	public static final String HERMIT = "org.semanticweb.HermiT.Reasoner.ReasonerFactory";
	public static final String FACT = "uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory";
	public static final String JFACT = "uk.ac.manchester.cs.jfact.JFactFactory";

	private static OWLReasonerFactory getFactory(String s) {
		try {
			return (OWLReasonerFactory) Class.forName(s).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Problem instantiating factory: " + s, e);
		}
	}

	public String getReasonerName() {
		return "ComparatorReasoner";
	}

	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
		return new ComparisonReasoner(ontology, null, getFactory(HERMIT),
				getFactory(JFACT), getFactory(FACT));
	}

	public OWLReasoner createReasoner(OWLOntology ontology) {
		return new ComparisonReasoner(ontology, null, getFactory(HERMIT),
				getFactory(JFACT), getFactory(FACT));
	}

	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology,
			OWLReasonerConfiguration config) throws IllegalConfigurationException {
		return new ComparisonReasoner(ontology, config, getFactory(HERMIT),
				getFactory(JFACT), getFactory(FACT));
	}

	public OWLReasoner createReasoner(OWLOntology ontology,
			OWLReasonerConfiguration config) throws IllegalConfigurationException {
		return new ComparisonReasoner(ontology, config, getFactory(HERMIT),
				getFactory(JFACT), getFactory(FACT));
	}
}
