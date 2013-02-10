package uk.ac.manchester.cs.atomicdecomposition;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

@SuppressWarnings("javadoc")
public interface AtomicDecomposer extends BaseAtomicDecomposer {
    public void setGlobalAxioms(Set<OWLAxiom> axioms);

    public Set<OWLAxiom> getGlobalAxioms();

    public void setTautologies(Set<OWLAxiom> axioms);

    @Override
    public Set<OWLAxiom> getTautologies();

    public int getModuleType();


    public Atom getAtomForAxiom(OWLAxiom axiom);

    public Map<OWLEntity, Set<Atom>> getTermBasedIndex();

    public void setTermBasedIndex(Map<OWLEntity, Set<Atom>> tbIndex);

    public Atom addAtom(Set<OWLAxiom> axioms);

    public Set<Atom> getTopAtoms(Atom atom);

    public boolean isTopAtom(Atom atom);

    public boolean isBottomAtom(Atom atom);

    public void addAxiomToAtom(OWLAxiom axiom, Atom atom);

    public void addDependency(Atom dependentAtom, Atom dependentUponAtom);

    public Set<OWLAxiom> getPrincipalIdeal(Atom atom);

    public Set<OWLEntity> getPrincipalIdealSignature(Atom atom);

    public Set<Atom> getDependencies(Atom atom, boolean direct);

    public Set<Atom> getDependents(Atom atom, boolean direct);

    /*
     * Returns the connected component for the given atom
     */
    public Set<Atom> getRelatedAtoms(Atom atom);

    public Set<Atom> getTopAtoms();

    public Set<Atom> getBottomAtoms();

    public Atom getAtomByID(Object id);
}
