package uk.ac.manchester.cs.atomicdecomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

/** An atom in the atomic decomposition */
public class Atom {
    private final Collection<OWLAxiom> axioms;
    private List<OWLEntity> signature;
    private Collection<OWLEntity> label;
    private int hashcode;

    /** @param ax
     *            axiom
     * @return true if ax is in this atom */
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

    /** @param axioms
     *            build an atom out of a set of axioms */
    public Atom(Collection<OWLAxiom> axioms) {
        this.axioms = axioms;
        hashcode = this.axioms.hashCode();
    }

    /** @return signature for the atom */
    public Collection<OWLEntity> getSignature() {
        initSignature();
        return signature;
    }

    /** @return axioms in the atom */
    public Collection<OWLAxiom> getAxioms() {
        return axioms;
    }

    /** @return label for the atom */
    public Collection<OWLEntity> getLabel() {
        return label;
    }

    /** @param labelSignature
     *            the label for the atom */
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
