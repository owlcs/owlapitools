package uk.ac.manchester.cs.atomicdecomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public class Atom {
    private final Collection<OWLAxiom> axioms;
    private List<OWLEntity> signature;
    private Collection<OWLEntity> label;
    private int hashcode;

    public boolean contains(OWLAxiom ax) {
        return axioms.contains(ax);
    }

    private void initSignature() {
        if (signature == null) {
            signature = new ArrayList<OWLEntity>();
            for (OWLAxiom ax : axioms) {
                signature.addAll(ax.getSignature());
            }
        }
    }

    public Atom(Collection<OWLAxiom> axioms) {
        this.axioms = axioms;
        hashcode = this.axioms.hashCode();
    }

    public Collection<OWLEntity> getSignature() {
        initSignature();
        return signature;
    }

    public Collection<OWLAxiom> getAxioms() {
        return axioms;
    }

    public Collection<OWLEntity> getLabel() {
        return label;
    }

    public void setLabel(Collection<OWLEntity> labelSignature) {
        label = labelSignature;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Atom) {
            return axioms.equals(((Atom) obj).axioms);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public String toString() {
        return axioms.toString();
    }
}
