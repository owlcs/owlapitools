package uk.ac.manchester.cs.atomicdecomposition;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

/** base for all atomic decomposers */
public interface BaseAtomicDecomposer {
    /** @param atom
     * @return dependencies for atom, including atom */
    public Set<Atom> getDependencies(Atom atom);

    /** @param atom
     * @return dependents for atom, including atom */
    public Set<Atom> getDependents(Atom atom);

    /** @return all atoms */
    public Set<Atom> getAtoms();

    /** @return all tautologies */
    public Set<OWLAxiom> getTautologies();
}
