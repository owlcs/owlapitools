package decomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;

/** class to hold the signature of a module */
public class TSignature {
    /** set to keep all the elements in signature */
    private Set<OWLEntity> set = new HashSet<OWLEntity>();
    /** true if concept TOP-locality; false if concept BOTTOM-locality */
    private boolean topCLocality = false;
    /** true if role TOP-locality; false if role BOTTOM-locality */
    private boolean topRLocality = false;

    public TSignature() {}

    public TSignature(Collection<OWLEntity> sig) {
        addAll(sig);
    }

    public TSignature(TSignature copy) {
        this(copy.set);
        topCLocality = copy.topCLocality;
        topRLocality = copy.topRLocality;
    }

    /** add names to signature */
    public boolean add(OWLEntity p) {
        return set.add(p);
    }

    public void addAll(Collection<OWLEntity> p) {
        set.addAll(p);
    }

    /** remove given element from a signature */
    public void remove(OWLEntity p) {
        set.remove(p);
    }

    /** add another signature to a given one */
    void add(TSignature Sig) {
        set.addAll(Sig.set);
    }

    /** set new locality polarity */
    public void setLocality(boolean top) {
        this.setLocality(top, top);
    }

    /** set new locality polarity */
    public void setLocality(boolean topC, boolean topR) {
        topCLocality = topC;
        topRLocality = topR;
    }

    // comparison
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof TSignature) {
            return set.equals(((TSignature) obj).set);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    /** @return true iff signature contains given element */
    public boolean contains(OWLEntity p) {
        return set.contains(p);
    }

    /** @return size of the signature */
    public int size() {
        return set.size();
    }

    /** clear the signature */
    public void clear() {
        set.clear();
    }

    public Set<OWLEntity> begin() {
        return set;
    }

    /** @return true iff concepts are treated as TOPs */
    public boolean topCLocal() {
        return topCLocality;
    }

    /** @return true iff roles are treated as TOPs */
    public boolean topRLocal() {
        return topRLocality;
    }

    public List<OWLEntity> intersect(TSignature s2) {
        List<OWLEntity> ret = new ArrayList<OWLEntity>();
        Set<OWLEntity> s = new HashSet<OWLEntity>(set);
        s.retainAll(s2.set);
        ret.addAll(s);
        return ret;
    }
}
