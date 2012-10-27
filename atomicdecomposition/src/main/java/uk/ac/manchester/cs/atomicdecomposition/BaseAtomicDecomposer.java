package uk.ac.manchester.cs.atomicdecomposition;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

public interface BaseAtomicDecomposer {
    public Set<Atom> getDependencies(Atom atom);

    /*
     * Returns the set of atoms that depend on the given atom depends on. The
     * set contains the given atom.
     */
    public Set<Atom> getDependents(Atom atom);

    public Set<Atom> getAtoms();

    public Set<OWLAxiom> getTautologies();
}
