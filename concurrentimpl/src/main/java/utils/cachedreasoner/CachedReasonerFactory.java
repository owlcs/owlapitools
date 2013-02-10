package utils.cachedreasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/** A Reasoner factory that wraps reasoners in a caching layer
 * 
 * @author ignazio */
public class CachedReasonerFactory implements OWLReasonerFactory {
	private final OWLReasonerFactory f;

    /** @param f
     *            reasoner factory to use to build actual reasoners */
	public CachedReasonerFactory(OWLReasonerFactory f) {
		this.f=f;
	}
	@Override
    public String getReasonerName() {

		return "cached";
	}

	@Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {

		return new CachedOWLReasoner( f.createNonBufferingReasoner(ontology), ontology.getOWLOntologyManager());
	}

	@Override
    public OWLReasoner createReasoner(OWLOntology ontology) {
		return new CachedOWLReasoner( f.createReasoner(ontology), ontology.getOWLOntologyManager());
	}

	@Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology,
			OWLReasonerConfiguration config) throws IllegalConfigurationException {
		return new CachedOWLReasoner( f.createNonBufferingReasoner(ontology, config), ontology.getOWLOntologyManager());
	}

	@Override
    public OWLReasoner createReasoner(OWLOntology ontology,
			OWLReasonerConfiguration config) throws IllegalConfigurationException {
		return new CachedOWLReasoner( f.createReasoner(ontology, config), ontology.getOWLOntologyManager());
	}
}
