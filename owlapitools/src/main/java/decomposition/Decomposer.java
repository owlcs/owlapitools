package decomposition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

/** atomical decomposer of the ontology */
public class Decomposer {
    /** atomic structure to build */
    private AtomList atomList = null;
    /** modularizer to build modules */
    private Modularizer modularizer;
    /** tautologies of the ontology */
    private List<AxiomWrapper> Tautologies = new ArrayList<AxiomWrapper>();
    /** fake atom that represents the whole ontology */
    private OntologyAtom rootAtom = null;
    /** module type for current AOS creation */
    private ModuleType type;

    public Decomposer(Modularizer c) {
        modularizer = c;
    }

    public Modularizer getModularizer() {
        return modularizer;
    }

    /** restore all tautologies back */
    private void restoreTautologies() {
        for (AxiomWrapper p : Tautologies) {
            p.setUsed(true);
        }
    }

    // #define RKG_DEBUG_AD
    /** remove tautologies (axioms that are always local) from the ontology
     * temporarily */
    private void removeTautologies(List<AxiomWrapper> axioms) {
        // we might use it for another decomposition
        Tautologies.clear();
        for (AxiomWrapper p : axioms) {
            final String string = p.toString();
            if (string.contains("Declaration")) {
                modularizer.extract(p, new Signature(p.getAxiom().getSignature()), type);
            }
            if (p.isUsed()) {
                // check whether an axiom is local wrt its own signature
                modularizer.extract(p, new Signature(p.getAxiom().getSignature()), type);
                if (modularizer.isTautology(p.getAxiom(), type)) {
                    Tautologies.add(p);
                    p.setUsed(false);
                }
            }
        }
    }

    public List<AxiomWrapper> getTautologies() {
        return new ArrayList<AxiomWrapper>(Tautologies);
    }

    /** build a module for given axiom AX; use parent atom's module as a base */
    // for the module search
    OntologyAtom buildModule(Signature sig, OntologyAtom parent) {
        // build a module for a given signature
        modularizer.extract(parent.getModule(), sig, type);
        List<AxiomWrapper> Module = modularizer.getModule();
        // if module is empty (empty bottom atom) -- do nothing
        if (Module.isEmpty()) {
            return null;
        }
        // check if the module corresponds to a PARENT one; modules are the same
        // iff their sizes are the same
        if (parent != rootAtom && Module.size() == parent.getModule().size()) {
            return parent;
        }
        // create new atom with that module
        OntologyAtom atom = atomList.newAtom();
        atom.setModule(Module);
        return atom;
    }

    /** create atom for given axiom AX; use parent atom's module as a base for
     * the module search */
    private OntologyAtom createAtom(AxiomWrapper ax, OntologyAtom parent) {
        // check whether axiom already has an atom
        OntologyAtom atom = ax.getAtom();
        if (atom != null) {
            return atom;
        }
        // build an atom: use a module to find atomic dependencies
        atom = buildModule(new Signature(ax.getAxiom().getSignature()), parent);
        // no empty modules should be here
        assert atom != null;
        // register axiom as a part of an atom
        atom.addAxiom(ax);
        // if atom is the same as parent -- nothing more to do
        if (atom == parent) {
            return parent;
        }
        // not the same as parent: for all atom's axioms check their atoms and
        // make ATOM depend on them
        /** do cycle via set to keep the order */
        for (AxiomWrapper q : atom.getModule()) {
            // #endif
            if (!q.getAxiom().equals(ax.getAxiom())) {
                atom.addDepAtom(createAtom(q, atom));
            }
        }
        return atom;
    }

    public AtomList getAOS() {
        return atomList;
    }

    /** get the atomic structure for given module type T */
    public AtomList getAOS(List<AxiomWrapper> axioms, ModuleType t) {
        // remember the type of the module
        type = t;
        // prepare a new AO structure
        atomList = new AtomList();
        // init semantic locality checker
        modularizer.preprocessOntology(axioms);
        // we don't need tautologies here
        removeTautologies(axioms);
        // init the root atom
        rootAtom = new OntologyAtom();
        rootAtom.setModule(new HashSet<AxiomWrapper>(axioms));
        // build the "bottom" atom for an empty signature
        OntologyAtom BottomAtom = buildModule(new Signature(), rootAtom);
        if (BottomAtom != null) {
            for (AxiomWrapper q : BottomAtom.getModule()) {
                BottomAtom.addAxiom(q);
            }
        }
        // create atoms for all the axioms in the ontology
        for (AxiomWrapper p : axioms) {
            if (p.isUsed() && p.getAtom() == null) {
                createAtom(p, rootAtom);
            }
        }
        // restore tautologies in the ontology
        restoreTautologies();
        rootAtom = null;
        // reduce graph
        atomList.reduceGraph();
        return atomList;
    }
}
