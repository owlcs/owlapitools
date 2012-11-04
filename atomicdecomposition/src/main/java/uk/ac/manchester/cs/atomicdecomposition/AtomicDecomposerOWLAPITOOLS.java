package uk.ac.manchester.cs.atomicdecomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.MultiMap;

import uk.ac.manchester.cs.chainsaw.FastSet;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import decomposition.AxiomSelector;
import decomposition.AxiomWrapper;
import decomposition.Decomposer;
import decomposition.IdentityMultiMap;
import decomposition.OntologyAtom;
import decomposition.SyntacticLocalityChecker;

@SuppressWarnings("javadoc")
public class AtomicDecomposerOWLAPITOOLS implements AtomicDecomposer {
    Set<OWLAxiom> globalAxioms;
    Set<OWLAxiom> tautologies;
    final MultiMap<OWLEntity, Atom> termBasedIndex = new MultiMap<OWLEntity, Atom>() {
        @Override
        protected Collection<Atom> createCollection() {
            return Collections.newSetFromMap(new IdentityHashMap<Atom, Boolean>());
        }
    };
    List<Atom> atoms;
    IdentityMultiMap<Atom, Atom> dependents = new IdentityMultiMap<Atom, Atom>();
    IdentityMultiMap<Atom, Atom> dependencies = new IdentityMultiMap<Atom, Atom>();
    Decomposer decomposer;
    private final ModuleType type;

    protected Set<OWLAxiom> asSet(Collection<AxiomWrapper> c) {
        Set<OWLAxiom> toReturn = new HashSet<OWLAxiom>();
        for (AxiomWrapper p : c) {
            toReturn.add(p.getAxiom());
        }
        return toReturn;
    }

    public AtomicDecomposerOWLAPITOOLS(OWLOntology o) {
        this(AxiomSelector.selectAxioms(o), ModuleType.BOT);
    }

    public AtomicDecomposerOWLAPITOOLS(OWLOntology o, ModuleType type) {
        this(AxiomSelector.selectAxioms(o), type);
    }

    public AtomicDecomposerOWLAPITOOLS(List<OWLAxiom> axioms, ModuleType type) {
        this.type = type;
        decomposer = new Decomposer(AxiomSelector.wrap(axioms),
                new SyntacticLocalityChecker());
        int size = decomposer.getAOS(this.type).size();
        atoms = new ArrayList<Atom>();
        for (int i = 0; i < size; i++) {
            final Atom atom = new Atom(asSet(decomposer.getAOS().get(i).getAtomAxioms()));
            atoms.add(atom);
            for (OWLEntity e : atom.getSignature()) {
                termBasedIndex.put(e, atom);
            }
        }
        for (int i = 0; i < size; i++) {
            Set<OntologyAtom> dependentIndexes = decomposer.getAOS().get(i)
                    .getDependencies();
            for (OntologyAtom j : dependentIndexes) {
                dependencies.put(atoms.get(i), atoms.get(j.getId()));
                dependents.put(atoms.get(j.getId()), atoms.get(i));
            }
        }
    }

    @Override
    public int getModuleType() {
        return type.ordinal();
    }

    @Override
    public DirectedGraph<Atom, Object> getGraph() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Atom> getAtoms() {
        return new HashSet<Atom>(atoms);
    }

    @Override
    public Atom getAtomForAxiom(OWLAxiom axiom) {
        for (int i = 0; i < atoms.size(); i++) {
            if (atoms.get(i).contains(axiom)) {
                return atoms.get(i);
            }
        }
        return null;
    }

    @Override
    public Atom addAtom(Set<OWLAxiom> axioms) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Atom> getTopAtoms(Atom atom) {
        Set<Atom> toReturn = getDependencies(atom);
        toReturn.retainAll(getTopAtoms());
        return toReturn;
    }

    @Override
    public boolean isTopAtom(Atom atom) {
        return !dependencies.containsKey(atom);
    }

    @Override
    public boolean isBottomAtom(Atom atom) {
        return !dependents.containsKey(atom);
    }

    @Override
    public void addAxiomToAtom(OWLAxiom axiom, Atom atom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void addDependency(Atom dependentAtom, Atom dependentUponAtom) {
        // TODO Auto-generated method stub
    }

    @Override
    public Set<OWLAxiom> getPrincipalIdeal(Atom atom) {
        return null;
    }

    @Override
    public Set<OWLEntity> getPrincipalIdealSignature(Atom atom) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Atom> getDependencies(Atom atom) {
        return getDependencies(atom, false);
    }

    @Override
    public Set<Atom> getDependencies(Atom atom, boolean direct) {
        return explore(atom, direct, dependencies);
    }

    @Override
    public Set<Atom> getDependents(Atom atom) {
        return getDependents(atom, false);
    }

    @Override
    public Set<Atom> getDependents(Atom atom, boolean direct) {
        return explore(atom, direct, dependents);
    }

    public Set<Atom> explore(Atom atom, boolean direct,
            IdentityMultiMap<Atom, Atom> multimap) {
        if (direct) {
            Set<Atom> hashSet = new HashSet<Atom>(multimap.get(atom));
            for (Atom a : multimap.get(atom)) {
                hashSet.removeAll(multimap.get(a));
            }
            return hashSet;
        }
        Map<Atom, Atom> toReturn = new HashMap<Atom, Atom>();
        toReturn.put(atom, atom);
        List<Atom> toDo = new ArrayList<Atom>();
        toDo.add(atom);
        for (int i = 0; i < toDo.size(); i++) {
            final Atom key = toDo.get(i);
            if (key != null) {
                Collection<Atom> c = multimap.get(key);
                for (Atom a : c) {
                    if (toReturn.put(a, a) == null) {
                        toDo.add(a);
                    }
                }
            }
        }
        return toReturn.keySet();
    }

    @Override
    public Set<Atom> getRelatedAtoms(Atom atom) {
        Set<Atom> s = getDependencies(atom);
        s.addAll(getDependents(atom));
        return s;
    }

    @Override
    public Set<Atom> getTopAtoms() {
        Set<Atom> keys = new HashSet<Atom>(dependents.keySet());
        keys.removeAll(dependents.getAllValues());
        return keys;
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

    @Override
    public Set<Atom> getBottomAtoms() {
        Set<Atom> keys = new HashSet<Atom>(dependents.getAllValues());
        keys.removeAll(dependents.keySet());
        return keys;
    }

    @Override
    public Atom getAtomByID(Object id) {
        return atoms.get((Integer) id);
    }

    @Override
    public Set<OWLAxiom> getGlobalAxioms() {
        return globalAxioms;
    }

    @Override
    public void setGlobalAxioms(Set<OWLAxiom> globalAxioms) {
        this.globalAxioms = globalAxioms;
    }

    @Override
    public Set<OWLAxiom> getTautologies() {
        return asSet(decomposer.getTautologies());
    }

    @Override
    public void setTautologies(Set<OWLAxiom> tautologies) {
        this.tautologies = tautologies;
    }

    @Override
    public Map<OWLEntity, Set<Atom>> getTermBasedIndex() {
        Map<OWLEntity, Set<Atom>> toReturn = new HashMap<OWLEntity, Set<Atom>>();
        for (OWLEntity e : termBasedIndex.keySet()) {
            toReturn.put(e, new HashSet<Atom>(termBasedIndex.get(e)));
        }
        return toReturn;
    }

    @Override
    public void setTermBasedIndex(Map<OWLEntity, Set<Atom>> termBasedIndex) {
        // this.termBasedIndex = termBasedIndex;
    }

    /** get a set of axioms that corresponds to the module of the atom with the
     * id INDEX */
    public Collection<AxiomWrapper> getAtomModule(int index) {
        return decomposer.getAOS().get(index).getModule();
    }

    public Collection<AxiomWrapper> getModule(Set<OWLEntity> signature,
            boolean useSemantics, ModuleType moduletype) {
        return decomposer.getModule(signature, useSemantics, moduletype);
    }
}
