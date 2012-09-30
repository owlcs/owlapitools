package decomposition;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

public class AxiomStructure {
    Set<OWLAxiom> usedAxioms = new HashSet<OWLAxiom>();
    Set<OWLAxiom> ss = new HashSet<OWLAxiom>();
    Set<OWLAxiom> inModule = new HashSet<OWLAxiom>();
    Map<OWLAxiom, TOntologyAtom> axiomToAtom = new HashMap<OWLAxiom, TOntologyAtom>();

    public AxiomStructure(Collection<OWLAxiom> axioms) {
        usedAxioms.addAll(axioms);
    }

    public void setAtomForAxiom(OWLAxiom ax, TOntologyAtom atom) {
        axiomToAtom.put(ax, atom);
    }

    public boolean isUsed(OWLAxiom ax) {
        return usedAxioms.contains(ax);
    }

    public boolean isSS(OWLAxiom ax) {
        return ss.contains(ax);
    }

    public boolean isInModule(OWLAxiom ax) {
        return inModule.contains(ax);
    }

    public void use(OWLAxiom ax) {
        usedAxioms.add(ax);
    }

    public void putInModule(OWLAxiom ax) {
        inModule.add(ax);
    }

    public void putInSS(OWLAxiom ax) {
        ss.add(ax);
    }

    public void disuse(OWLAxiom ax) {
        usedAxioms.remove(ax);
    }

    public TOntologyAtom getAtom(OWLAxiom ax) {
        return axiomToAtom.get(ax);
    }

    public void removeFromModule(OWLAxiom p) {
        inModule.remove(p);
    }

    public void removeFromSS(OWLAxiom p) {
        ss.remove(p);
    }
}
