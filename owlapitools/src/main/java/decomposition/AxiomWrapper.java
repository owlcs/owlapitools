package decomposition;

import org.semanticweb.owlapi.model.OWLAxiom;

public class AxiomWrapper {
    private OWLAxiom axiom;
    private boolean used;
    private boolean searchspace;
    private boolean module;
    private OntologyAtom atom;

    public AxiomWrapper(OWLAxiom axiom) {
        this.axiom = axiom;
    }

    public OWLAxiom getAxiom() {
        return axiom;
    }

    public void setUsed(boolean b) {
        used = b;
    }

    public boolean isUsed() {
        return used;
    }

    public void setInSearchSpace(boolean b) {
        searchspace = b;
    }

    public boolean isInSearchSpace() {
        return searchspace;
    }

    public void setInModule(boolean b) {
        module = b;
    }

    public boolean isInModule() {
        return module;
    }

    public void setAtom(OntologyAtom atom) {
        this.atom = atom;
    }

    public OntologyAtom getAtom() {
        return atom;
    }
}
