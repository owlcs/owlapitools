package decomposition;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class OntologyAtom {
    static Comparator<OntologyAtom> comparator = new Comparator<OntologyAtom>() {
        public int compare(OntologyAtom arg0, OntologyAtom arg1) {
            return arg0.getId() - arg1.getId();
        }
    };
    /** set of axioms in the atom */
    Set<AxiomWrapper> AtomAxioms = new HashSet<AxiomWrapper>();
    /** set of axioms in the module (Atom's ideal) */
    Set<AxiomWrapper> ModuleAxioms = new HashSet<AxiomWrapper>();
    /** set of atoms current one depends on */
    Set<OntologyAtom> DepAtoms = new HashSet<OntologyAtom>();
    /** set of all atoms current one depends on */
    Set<OntologyAtom> AllDepAtoms = new HashSet<OntologyAtom>();
    /** unique atom's identifier */
    int Id = 0;

    /** remove all atoms in AllDepAtoms from DepAtoms */
    public void filterDep() {
        for (OntologyAtom p : AllDepAtoms) {
            DepAtoms.remove(p);
        }
    }

    /** build all dep atoms; filter them from DepAtoms */
    public void buildAllDepAtoms(Set<OntologyAtom> checked) {
        // first gather all dep atoms from all known dep atoms
        for (OntologyAtom p : DepAtoms) {
            Set<OntologyAtom> Dep = p.getAllDepAtoms(checked);
            AllDepAtoms.addAll(Dep);
        }
        // now filter them out from known dep atoms
        filterDep();
        // add direct deps to all deps
        AllDepAtoms.addAll(DepAtoms);
        // now the atom is checked
        checked.add(this);
    }

    // fill in the sets
    /** set the module axioms */
    public void setModule(Collection<AxiomWrapper> module) {
        ModuleAxioms = new HashSet<AxiomWrapper>(module);
    }

    /** add axiom AX to an atom */
    public void addAxiom(AxiomWrapper ax) {
        AtomAxioms.add(ax);
        ax.setAtom(this);
    }

    /** add atom to the dependency set */
    public void addDepAtom(OntologyAtom atom) {
        if (atom != null && atom != this) {
            DepAtoms.add(atom);
        }
    }

    /** get all the atoms the current one depends on; build this set if */
    // necessary
    public Set<OntologyAtom> getAllDepAtoms(Set<OntologyAtom> checked) {
        if (checked.contains(this)) {
            buildAllDepAtoms(checked);
        }
        return AllDepAtoms;
    }

    // access to axioms
    /** get all the atom's axioms */
    public Set<AxiomWrapper> getAtomAxioms() {
        return AtomAxioms;
    }

    /** get all the module axioms */
    public Set<AxiomWrapper> getModule() {
        return ModuleAxioms;
    }

    /** get atoms a given one depends on */
    public Set<OntologyAtom> getDepAtoms() {
        return DepAtoms;
    }

    /** get the value of the id */
    public int getId() {
        return Id;
    }

    /** set the value of the id to ID */
    public void setId(int id) {
        Id = id;
    }
}
