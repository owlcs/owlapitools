package decomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

/** class to create modules of an ontology wrt module type */
public class TModularizer {
    /** shared signature signature */
    TSignature sig;
    /** internal syntactic locality checker */
    LocalityChecker Checker;
    /** module as a list of axioms */
    List<AxiomWrapper> Module = new ArrayList<AxiomWrapper>();
    /** pointer to a sig index; if not NULL then use optimized algo */
    SigIndex sigIndex = null;
    // / true if no atoms are processed ATM
    boolean noAtomsProcessing;
    /** queue of unprocessed entities */
    List<OWLEntity> WorkQueue = new ArrayList<OWLEntity>();
    /** number of locality check calls */
    long nChecks;
    /** number of non-local axioms */
    long nNonLocal;
    private AxiomStructure as;

    /** update SIG wrt the axiom signature */
    void addAxiomSig(AxiomWrapper axiom) {
        if (sigIndex != null) {
            for (OWLEntity p : axiom.getAxiom().getSignature()) {
                if (sig.add(p)) {
                    WorkQueue.add(p);
                }
            }
        }
    }

    /** add an axiom to a module */
    void addAxiomToModule(AxiomWrapper axiom) {
        axiom.setInModule(true);
        Module.add(axiom);
        // update the signature
        addAxiomSig(axiom);
    }

    /** set sig index to a given value */
    public void setSigIndex(SigIndex p) {
        sigIndex = p;
        nChecks += 2 * p.nProcessedAx();
        nNonLocal += p.getNonLocal(false).size() + p.getNonLocal(true).size();
    }

    /** @return true iff an AXiom is non-local */
    boolean isNonLocal(OWLAxiom ax) {
        ++nChecks;
        if (Checker.local(ax)) {
            return false;
        }
        ++nNonLocal;
        return true;
    }

    /** add an axiom if it is non-local (or if noCheck is true) */
    void addNonLocal(AxiomWrapper ax, boolean noCheck) {
        if (noCheck || isNonLocal(ax.getAxiom())) {
            addAxiomToModule(ax);
        }
    }

    /** mark the ontology O such that all the marked axioms creates the module */
    // wrt SIG
    void extractModuleLoop(Collection<AxiomWrapper> args) {
        int sigSize;
        do {
            sigSize = sig.size();
            for (AxiomWrapper p : args) {
                if (!p.isInModule() && p.isUsed()) {
                    this.addNonLocal(p, false);
                }
            }
        } while (sigSize != sig.size());
    }

    /** add all the non-local axioms from given axiom-set AxSet */
    void addNonLocal(Collection<AxiomWrapper> AxSet, boolean noCheck) {
        for (AxiomWrapper q : AxSet) {
            if (!q.isInModule() && q.isInSearchSpace()) {
                this.addNonLocal(q, noCheck);
            }
        }
    }

    /** build a module traversing axioms by a signature */
    void extractModuleQueue() {
        // init queue with a sig
        for (OWLEntity p : sig.begin()) {
            WorkQueue.add(p);
        }
        // add all the axioms that are non-local wrt given value of a
        // top-locality
        this.addNonLocal(sigIndex.getNonLocal(sig.topCLocal()), true);
        // main cycle
        while (!WorkQueue.isEmpty()) {
            OWLEntity entity = WorkQueue.remove(0);
            // for all the axioms that contains entity in their signature
            this.addNonLocal(sigIndex.getAxioms(entity), false);
        }
    }

    /** extract module wrt presence of a sig index */
    void extractModule(Collection<AxiomWrapper> args) {
        Module.clear();
        // clear the module flag in the input
        for (AxiomWrapper p : args) {
            p.setInModule(false);
        }
        for (AxiomWrapper p : args) {
            if (p.isUsed()) {
                p.setInSearchSpace(true);
            }
        }
        extractModuleQueue();
        for (AxiomWrapper p : args) {
            p.setInSearchSpace(false);
        }
    }

    /** init c'tor */
    public TModularizer(LocalityChecker c, AxiomStructure as) {
        Checker = c;
        sig = c.getSignature();
        sigIndex = new SigIndex(Checker, as);
        nChecks = 0;
        nNonLocal = 0;
        this.as = as;
    }

    /** allow the checker to preprocess an ontology if necessary */
    public void preprocessOntology(Collection<AxiomWrapper> vec) {
        Checker.preprocessOntology(vec);
        sigIndex.clear();
        sigIndex.preprocessOntology(vec);
    }

    // / @return true iff the axiom AX is a tautology wrt given type
    boolean isTautology(OWLAxiom ax, ModuleType type) {
        boolean topLocality = type == ModuleType.TOP;
        sig = new TSignature(ax.getSignature());
        sig.setLocality(topLocality);
        // axiom is a tautology if it is local wrt its own signature
        boolean toReturn = Checker.local(ax);
        if (type != ModuleType.STAR || !toReturn) {
            return toReturn;
        }
        // here it is STAR case and AX is local wrt BOT
        sig.setLocality(!topLocality);
        return Checker.local(ax);
    }

    // / get RW access to the sigIndex (mainly to (un-)register axioms on the
    // fly)
    public SigIndex getSigIndex() {
        return sigIndex;
    }

    /** get access to the Locality checker */
    public LocalityChecker getLocalityChecker() {
        return Checker;
    }

    void extract(AxiomWrapper begin, TSignature signature, ModuleType type) {
        this.extract(Collections.singletonList(begin), signature, type);
    }

    /** extract module wrt SIGNATURE and TYPE from the set of axioms */
    // [BEGIN,END)
    public void extract(Collection<AxiomWrapper> begin, TSignature signature,
            ModuleType type) {
        boolean topLocality = type == ModuleType.TOP;
        sig = signature;
        Checker.setSignatureValue(sig);
        sig.setLocality(topLocality);
        this.extractModule(begin);
        if (type != ModuleType.STAR) {
            return;
        }
        // here there is a star: do the cycle until stabilization
        int size;
        List<AxiomWrapper> oldModule = new ArrayList<AxiomWrapper>();
        do {
            size = Module.size();
            oldModule.clear();
            oldModule.addAll(Module);
            topLocality = !topLocality;
            sig = signature;
            sig.setLocality(topLocality);
            this.extractModule(oldModule);
        } while (size != Module.size());
    }

    /** get number of checks made */
    long getNChecks() {
        return nChecks;
    }

    /** get number of axioms that were local */
    long getNNonLocal() {
        return nNonLocal;
    }

    /** extract module wrt SIGNATURE and TYPE from O; @return result in the Set */
    public List<AxiomWrapper> extractModule(List<AxiomWrapper> list,
            TSignature signature, ModuleType type) {
        this.extract(list, signature, type);
        return Module;
    }

    /** get the last computed module */
    public List<AxiomWrapper> getModule() {
        return Module;
    }

    /** get access to a signature */
    public TSignature getSignature() {
        return sig;
    }
}
