package uk.ac.manchester.cs.atomicdecomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

public class Atom {
    private final Collection<OWLAxiom> axioms;
    private List<OWLEntity> signature = new ArrayList<OWLEntity>();
    private Collection<OWLEntity> label;

    public boolean contains(OWLAxiom ax) {
        return axioms.contains(ax);
    }

    public Atom(Collection<OWLAxiom> axioms) {
        this.axioms = axioms;
        Set<OWLEntity> set = new HashSet<OWLEntity>();
        for (OWLAxiom ax : axioms) {
            set.addAll(ax.getSignature());
        }
        signature.addAll(set);
    }

    public Collection<OWLEntity> getSignature() {
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
        return axioms.hashCode();
    }

    @Override
    public String toString() {
        return axioms.toString();
    }
}
