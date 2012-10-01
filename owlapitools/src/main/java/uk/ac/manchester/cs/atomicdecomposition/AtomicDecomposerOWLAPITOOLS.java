package uk.ac.manchester.cs.atomicdecomposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.MultiMap;

import uk.ac.manchester.cs.chainsaw.ArrayIntMap;
import uk.ac.manchester.cs.chainsaw.FastSet;
import decomposition.OntologyAtom;
import decomposition.Test;

public class AtomicDecomposerOWLAPITOOLS implements AtomicDecomposer {
    Set<OWLAxiom> globalAxioms;
    Set<OWLAxiom> tautologies;
    final MultiMap<OWLEntity, Atom> termBasedIndex = new MultiMap<OWLEntity, Atom>();
    List<Atom> atoms = new ArrayList<Atom>();
    Map<Atom, Integer> map = new HashMap<Atom, Integer>();
    ArrayIntMap dependents = new ArrayIntMap();
    ArrayIntMap dependencies = new ArrayIntMap();
    Test delegate;
    private final int type;

    public AtomicDecomposerOWLAPITOOLS(OWLOntology o) {
        type = 0;
        delegate = new Test(o);
        int size = delegate
                .getAtomicDecompositionSize(uk.ac.manchester.cs.owlapi.modularity.ModuleType.BOT);
        atoms.add(null);
        for (int i = 0; i < size; i++) {
            final Atom extendedAtomImpl = new Atom(delegate.getAtomAxioms(i));
            atoms.add(extendedAtomImpl);
            map.put(extendedAtomImpl, i + 1);
            for (OWLEntity e : extendedAtomImpl.getSignature()) {
                termBasedIndex.put(e, extendedAtomImpl);
            }
        }
        for (int i = 1; i <= size; i++) {
            Set<OntologyAtom> dependentIndexes = delegate.getAtomDependents(i - 1);
            for (OntologyAtom j : dependentIndexes) {
                dependencies.put(i, j.getId() + 1);
                dependents.put(j.getId() + 1, i);
            }
        }
    }

    public int getModuleType() {
        return type;
    }

    public DirectedGraph<Atom, Object> getGraph() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<Atom> getAtoms() {
        return new HashSet<Atom>(atoms.subList(1, atoms.size()));
    }

    public Atom getAtomForAxiom(OWLAxiom axiom) {
        for (int i = 1; i < atoms.size(); i++) {
            if (atoms.get(i).contains(axiom)) {
                return atoms.get(i);
            }
        }
        return null;
    }

    public Atom addAtom(Set<OWLAxiom> axioms) {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<Atom> getTopAtoms(Atom atom) {
        Set<Atom> toReturn = getDependencies(atom);
        toReturn.retainAll(getTopAtoms());
        return toReturn;
    }

    public boolean isTopAtom(Atom atom) {
        return !dependencies.containsKey(map.get(atom));
    }

    public boolean isBottomAtom(Atom atom) {
        return !dependents.containsKey(map.get(atom));
    }

    public void addAxiomToAtom(OWLAxiom axiom, Atom atom) {
        // TODO Auto-generated method stub
    }

    public void addDependency(Atom dependentAtom, Atom dependentUponAtom) {
        // TODO Auto-generated method stub
    }

    public Set<OWLAxiom> getPrincipalIdeal(Atom atom) {
        return null;
    }

    public Set<OWLEntity> getPrincipalIdealSignature(Atom atom) {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<Atom> getDependencies(Atom atom) {
        return getDependencies(atom, false);
    }

    public Set<Atom> getDependencies(Atom atom, boolean direct) {
        return explore(atom, direct, dependencies);
    }

    public Set<Atom> getDependents(Atom atom) {
        return getDependents(atom, false);
    }

    public Set<Atom> getDependents(Atom atom, boolean direct) {
        return explore(atom, direct, dependents);
    }

    public Set<Atom> explore(Atom atom, boolean direct, ArrayIntMap multimap) {
        if (direct) {
            return asSet(multimap.get(map.get(atom)));
        }
        Set<Atom> toReturn = new HashSet<Atom>();
        toReturn.add(atom);
        List<Atom> toDo = new ArrayList<Atom>();
        toDo.add(atom);
        for (int i = 0; i < toDo.size(); i++) {
            final Integer key = map.get(toDo.get(i));
            if (key != null) {
                FastSet c = multimap.get(key);
                for (int j = 0; j < c.size(); j++) {
                    Atom a = atoms.get(c.get(j));
                    if (toReturn.add(a)) {
                        toDo.add(a);
                    }
                }
            }
        }
        return toReturn;
    }

    public Set<Atom> getRelatedAtoms(Atom atom) {
        Set<Atom> s = getDependencies(atom);
        s.addAll(getDependents(atom));
        return s;
    }

    public Set<Atom> getTopAtoms() {
        List<Integer> keys = dependents.keySet();
        keys.removeAll(dependents.getAllValues());
        return asSet(keys);
    }

    public Set<Atom> asSet(Iterable<Integer> keys) {
        Set<Atom> s = new HashSet<Atom>();
        for (int i : keys) {
            s.add(atoms.get(i));
        }
        return s;
    }

    public Set<Atom> asSet(FastSet keys) {
        Set<Atom> s = new HashSet<Atom>();
        for (int i = 0; i < keys.size(); i++) {
            s.add(atoms.get(keys.get(i)));
        }
        return s;
    }

    public Set<Atom> getBottomAtoms() {
        List<Integer> keys = dependents.getAllValues();
        keys.removeAll(dependents.keySet());
        return asSet(keys);
    }

    public Atom getAtomByID(Object id) {
        return atoms.get((Integer) id);
    }

    public Set<OWLAxiom> getGlobalAxioms() {
        return globalAxioms;
    }

    public void setGlobalAxioms(Set<OWLAxiom> globalAxioms) {
        this.globalAxioms = globalAxioms;
    }

    public Set<OWLAxiom> getTautologies() {
        return delegate.getTautologies();
    }

    public void setTautologies(Set<OWLAxiom> tautologies) {
        this.tautologies = tautologies;
    }

    public Map<OWLEntity, Set<Atom>> getTermBasedIndex() {
        Map<OWLEntity, Set<Atom>> toReturn = new HashMap<OWLEntity, Set<Atom>>();
        for (OWLEntity e : termBasedIndex.keySet()) {
            toReturn.put(e, new HashSet<Atom>(termBasedIndex.get(e)));
        }
        return toReturn;
    }

    public void setTermBasedIndex(Map<OWLEntity, Set<Atom>> termBasedIndex) {
        // this.termBasedIndex = termBasedIndex;
    }
}
