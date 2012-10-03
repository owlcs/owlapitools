package decomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

/** class to create modules of an ontology wrt module type */
public class Modularizer {
    /** shared signature signature */
    private Signature sig;
    /** internal syntactic locality checker */
    private LocalityChecker checker;
    /** module as a list of axioms */
    private List<AxiomWrapper> module = new ArrayList<AxiomWrapper>();
    /** pointer to a sig index; if not NULL then use optimized algo */
    private SigIndex sigIndex = null;
    /** queue of unprocessed entities */
    private LinkedList<OWLEntity> workQueue = new LinkedList<OWLEntity>();

    /** update SIG wrt the axiom signature */
    private void addAxiomSig(AxiomWrapper axiom) {
        if (sigIndex != null) {
            for (OWLEntity p : axiom.getAxiom().getSignature()) {
                if (sig.add(p)) {
                    workQueue.add(p);
                }
            }
        }
    }

    /** add an axiom to a module */
    private void addAxiomToModule(AxiomWrapper axiom) {
        axiom.setInModule(true);
        module.add(axiom);
        // update the signature
        addAxiomSig(axiom);
    }

    /** @return true iff an AXiom is non-local */
    private boolean isNonLocal(OWLAxiom ax) {
        return !checker.local(ax);
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
        workQueue.addAll(sig.getSignature());
        // add all the axioms that are non-local wrt given value of a
        // top-locality
        this.addNonLocal(sigIndex.getNonLocal(sig.topCLocal()), true);
        // main cycle
        while (!workQueue.isEmpty()) {
            // for all the axioms that contains entity in their signature
            Collection<AxiomWrapper> axioms = sigIndex.getAxioms(workQueue.pop());
            this.addNonLocal(axioms, false);
        }
    }

    /** extract module wrt presence of a sig index */
    private void extractModule(Collection<AxiomWrapper> args) {
        module.clear();
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
        checker = c;
        sig = c.getSignature();
        sigIndex = new SigIndex(checker);
    }

    /** allow the checker to preprocess an ontology if necessary */
    public void preprocessOntology(Collection<AxiomWrapper> vec) {
        checker.preprocessOntology(vec);
        sigIndex.clear();
        sigIndex.preprocessOntology(vec);
    }

    /** @return true iff the axiom AX is a tautology wrt given type */
    public boolean isTautology(OWLAxiom ax, ModuleType type) {
        boolean topLocality = type == ModuleType.TOP;
        sig = new Signature(ax.getSignature());
        sig.setLocality(topLocality);
        // axiom is a tautology if it is local wrt its own signature
        boolean toReturn = checker.local(ax);
        if (type != ModuleType.STAR || !toReturn) {
            return toReturn;
        }
        // here it is STAR case and AX is local wrt BOT
        sig.setLocality(!topLocality);
        return checker.local(ax);
    }

    /** get access to the Locality checker */
    public LocalityChecker getLocalityChecker() {
        return checker;
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
        checker.setSignatureValue(sig);
        sig.setLocality(topLocality);
        extractModule(begin);
        if (type != ModuleType.STAR) {
            return;
        }
        // here there is a star: do the cycle until stabilization
        int size;
        List<AxiomWrapper> oldModule = new ArrayList<AxiomWrapper>();
        do {
            size = module.size();
            oldModule.clear();
            oldModule.addAll(module);
            topLocality = !topLocality;
            sig = signature;
            sig.setLocality(topLocality);
            extractModule(oldModule);
        } while (size != module.size());
    }

    /** get the last computed module */
    public List<AxiomWrapper> getModule() {
        return module;
    }
}
