package decomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

public class Test {
    private List<OWLAxiom> axioms = new ArrayList<OWLAxiom>();
    AtomicDecomposer AD;

    public Test(OWLOntology o) {
        for (OWLOntology ont : o.getImportsClosure()) {
            for (OWLAxiom ax : ont.getLogicalAxioms()) {
                OWLAxiom axiom = ax.getAxiomWithoutAnnotations();
                axioms.add(axiom);
            }
            for (OWLAxiom ax : ont.getAxioms(AxiomType.DECLARATION)) {
                OWLAxiom axiom = ax.getAxiomWithoutAnnotations();
                axioms.add(axiom);
            }
        }
        AxiomStructure as = new AxiomStructure(axioms);
        TModularizer Mod = new TModularizer(new SyntacticLocalityChecker(as), as);
        AD = new AtomicDecomposer(Mod, as);
        Mod.preprocessOntology(axioms);
    }

    public int getAtomicDecompositionSize(ModuleType type) {
        return AD.getAOS(axioms, type).size();
    }

    public Set<OWLAxiom> getTautologies() {
        return new HashSet<OWLAxiom>(AD.getTautologies());
    }

    /** get a set of axioms that corresponds to the atom with the id INDEX */
    public Set<OWLAxiom> getAtomAxioms(int index) {
        return AD.getAOS().get(index).getAtomAxioms();
    }

    /** get a set of axioms that corresponds to the module of the atom with the */
    // id INDEX
    public Set<OWLAxiom> getAtomModule(int index) {
        return AD.getAOS().get(index).getModule();
    }

    /** get a set of atoms on which atom with index INDEX depends */
    public Set<TOntologyAtom> getAtomDependents(int index) {
        return AD.getAOS().get(index).getDepAtoms();
    }

    /** get a set of axioms that corresponds to the atom with the id INDEX */
    public Collection<OWLAxiom> getModule(Set<OWLEntity> signature, boolean useSemantics,
            ModuleType moduletype) {
        // init signature
        TSignature Sig = new TSignature(signature);
        Sig.setLocality(false);
        TModularizer Mod = AD.getModularizer();
        Mod.extract(axioms, Sig, moduletype);
        return Mod.getModule();
    }

    /** get a set of axioms that corresponds to the atom with the id INDEX */
    public Set<OWLAxiom> getNonLocal(Set<OWLEntity> signature, ModuleType moduletype) {
        // init signature
        TSignature Sig = new TSignature(signature);
        Sig.setLocality(false);
        // do check
        TModularizer Mod = AD.getModularizer();
        Mod.getLocalityChecker().setSignatureValue(Sig);
        Set<OWLAxiom> Result = new HashSet<OWLAxiom>();
        for (OWLAxiom p : axioms) {
            if (!Mod.getLocalityChecker().local(p)) {
                Result.add(p);
            }
        }
        return Result;
    }
}
