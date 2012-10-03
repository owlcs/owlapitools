package decomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OntologyAtom {
    static Comparator<OntologyAtom> comparator = new Comparator<OntologyAtom>() {
        public int compare(OntologyAtom arg0, OntologyAtom arg1) {
            return arg0.getId() - arg1.getId();
        }
    };
    /** set of axioms in the atom */
    List<AxiomWrapper> axioms = new ArrayList<AxiomWrapper>();
    /** set of axioms in the module (Atom's ideal) */
    List<AxiomWrapper> module = new ArrayList<AxiomWrapper>();
    /** set of atoms current one depends on */
    Set<OntologyAtom> dependencies = new HashSet<OntologyAtom>();
    /** set of all atoms current one depends on */
    Set<OntologyAtom> allDependencies = new HashSet<OntologyAtom>();
    /** unique atom's identifier */
    int id = 0;

    /** remove all atoms in AllDepAtoms from DepAtoms */
    public void filterDep() {
        for (OntologyAtom p : allDependencies) {
            dependencies.remove(p);
        }
    }

    /** build all dep atoms; filter them from DepAtoms */
    public void buildAllDepAtoms(Set<OntologyAtom> checked) {
        // first gather all dep atoms from all known dep atoms
        for (OntologyAtom p : dependencies) {
            Set<OntologyAtom> Dep = p.getAllDepAtoms(checked);
            allDependencies.addAll(Dep);
        }
        // now filter them out from known dep atoms
        filterDep();
        // add direct deps to all deps
        allDependencies.addAll(dependencies);
        // now the atom is checked
        checked.add(this);
    }

    // fill in the sets
    /** set the module axioms */
    public void setModule(Collection<AxiomWrapper> module) {
        this.module = new ArrayList<AxiomWrapper>(module);
    }

    /** add axiom AX to an atom */
    public void addAxiom(AxiomWrapper ax) {
        axioms.add(ax);
        ax.setAtom(this);
    }

    public void addAxioms(Collection<AxiomWrapper> axs) {
        for (AxiomWrapper ax : axs) {
            addAxiom(ax);
        }
    }

    /** add atom to the dependency set */
    public void addDepAtom(OntologyAtom atom) {
        if (atom != null && atom != this) {
            dependencies.add(atom);
        }
    }

    /** get all the atoms the current one depends on; build this set if */
    // necessary
    public Set<OntologyAtom> getAllDepAtoms(Set<OntologyAtom> checked) {
        if (checked.contains(this)) {
            buildAllDepAtoms(checked);
        }
        return allDependencies;
    }

    // access to axioms
    /** get all the atom's axioms */
    public Collection<AxiomWrapper> getAtomAxioms() {
        return axioms;
    }

    /** get all the module axioms */
    public Collection<AxiomWrapper> getModule() {
        return module;
    }

    /** get atoms a given one depends on */
    public Set<OntologyAtom> getDependencies() {
        return dependencies;
    }

    /** get the value of the id */
    public int getId() {
        return id;
    }

    /** set the value of the id to ID */
    public void setId(int id) {
        this.id = id;
    }
}
