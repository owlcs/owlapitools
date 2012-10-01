package decomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

/** class to create modules of an ontology wrt module type */
public class Modularizer {
    /** shared signature signature */
    private Signature sig;
    /** internal syntactic locality checker */
    private LocalityChecker Checker;
    /** module as a list of axioms */
    private List<AxiomWrapper> Module = new ArrayList<AxiomWrapper>();
    /** pointer to a sig index; if not NULL then use optimized algo */
    private SigIndex sigIndex = null;
    /** queue of unprocessed entities */
    private List<OWLEntity> WorkQueue = new ArrayList<OWLEntity>();

    /** update SIG wrt the axiom signature */
    private void addAxiomSig(AxiomWrapper axiom) {
        if (sigIndex != null) {
            for (OWLEntity p : axiom.getAxiom().getSignature()) {
                if (sig.add(p)) {
                    WorkQueue.add(p);
                }
            }
        }
    }

    /** add an axiom to a module */
    private void addAxiomToModule(AxiomWrapper axiom) {
        axiom.setInModule(true);
        Module.add(axiom);
        // update the signature
        addAxiomSig(axiom);
    }

    /** set sig index to a given value */
    public void setSigIndex(SigIndex p) {
        sigIndex = p;
    }

    /** @return true iff an AXiom is non-local */
    private boolean isNonLocal(OWLAxiom ax) {
        return !Checker.local(ax);
    }

    /** add an axiom if it is non-local (or if noCheck is true) */
    private void addNonLocal(AxiomWrapper ax, boolean noCheck) {
        if (noCheck || isNonLocal(ax.getAxiom())) {
            addAxiomToModule(ax);
        }
    }

    /** add all the non-local axioms from given axiom-set AxSet */
    private void addNonLocal(Collection<AxiomWrapper> AxSet, boolean noCheck) {
        for (AxiomWrapper q : AxSet) {
            if (!q.isInModule() && q.isInSearchSpace()) {
                this.addNonLocal(q, noCheck);
            }
        }
    }

    /** build a module traversing axioms by a signature */
    private void extractModuleQueue() {
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
    private void extractModule(Collection<AxiomWrapper> args) {
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

    /** @param c
     *            the clocality checker */
    public Modularizer(LocalityChecker c) {
        Checker = c;
        sig = c.getSignature();
        sigIndex = new SigIndex(Checker);
    }

    /** allow the checker to preprocess an ontology if necessary */
    public void preprocessOntology(Collection<AxiomWrapper> vec) {
        Checker.preprocessOntology(vec);
        sigIndex.clear();
        sigIndex.preprocessOntology(vec);
    }

    /** @return true iff the axiom AX is a tautology wrt given type */
    public boolean isTautology(OWLAxiom ax, ModuleType type) {
        boolean topLocality = type == ModuleType.TOP;
        sig = new Signature(ax.getSignature());
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

    public void extract(AxiomWrapper begin, Signature signature, ModuleType type) {
        this.extract(Collections.singletonList(begin), signature, type);
    }

    /** extract module wrt SIGNATURE and TYPE from the set of axioms */
    // [BEGIN,END)
    public void extract(Collection<AxiomWrapper> begin, Signature signature,
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

    /** extract module wrt SIGNATURE and TYPE from O; @return result in the Set */
    public List<AxiomWrapper> extractModule(List<AxiomWrapper> list, Signature signature,
            ModuleType type) {
        this.extract(list, signature, type);
        return Module;
    }

    /** get the last computed module */
    public List<AxiomWrapper> getModule() {
        return Module;
    }

    /** get access to a signature */
    public Signature getSignature() {
        return sig;
    }
}
