package org.semanticweb.owlapitools.cachedreasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.owl.owlapi.alternateimpls.ThreadSafeOWLReasoner;

/**
 * A Reasoner factory that wraps reasoners in a caching layer
 * 
 * @author ignazio
 */
public class CachedReasonerFactory implements OWLReasonerFactory {

    private final OWLReasonerFactory f;

    /**
     * @param f
     *        reasoner factory to use to build actual reasoners
     */
    public CachedReasonerFactory(OWLReasonerFactory f) {
        this.f = f;
    }

    @Override
    public String getReasonerName() {
        return f.getReasonerName() + " cached";
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
        return new CachedOWLReasoner(new ThreadSafeOWLReasoner(f.createNonBufferingReasoner(ontology)),
            ontology.getOWLOntologyManager());
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology) {
        return new CachedOWLReasoner(new ThreadSafeOWLReasoner(f.createReasoner(ontology)),
            ontology.getOWLOntologyManager());
    }

    @Override
    public OWLReasoner createNonBufferingReasoner(OWLOntology ontology,
        OWLReasonerConfiguration config) throws IllegalConfigurationException {
        return new CachedOWLReasoner(new ThreadSafeOWLReasoner(f.createNonBufferingReasoner(ontology, config)),
            ontology.getOWLOntologyManager());
    }

    @Override
    public OWLReasoner createReasoner(OWLOntology ontology,
        OWLReasonerConfiguration config) throws IllegalConfigurationException {
        return new CachedOWLReasoner(new ThreadSafeOWLReasoner(f.createReasoner(ontology, config)),
            ontology.getOWLOntologyManager());
    }
}
