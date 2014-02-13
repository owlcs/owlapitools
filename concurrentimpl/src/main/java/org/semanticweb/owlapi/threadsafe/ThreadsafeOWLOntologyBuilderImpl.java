package org.semanticweb.owlapi.threadsafe;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyBuilder;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.alternateimpls.LockingOWLOntologyImpl;

public class ThreadsafeOWLOntologyBuilderImpl implements OWLOntologyBuilder {
    @Override
    public OWLOntology createOWLOntology(OWLOntologyManager manager,
            OWLOntologyID ontologyID) {
        return new LockingOWLOntologyImpl(manager, ontologyID);
    }
}
