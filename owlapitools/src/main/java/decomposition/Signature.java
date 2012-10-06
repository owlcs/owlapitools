package decomposition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;

import utils.Bag;

/** class to hold the signature of a module */
public class Signature {
    /** set to keep all the elements in signature */
    private final Set<OWLEntity> set = new HashSet<OWLEntity>();
    /** true if concept TOP-locality; false if concept BOTTOM-locality */
    private boolean topCLocality = false;
    /** true if role TOP-locality; false if role BOTTOM-locality */
    private boolean topRLocality = false;

    public Signature() {}

    public Signature(Collection<OWLEntity> sig) {
        addAll(sig);
    }

    /** add names to signature */
    public boolean add(OWLEntity p) {
        return set.add(p);
    }

    public void addAll(Collection<OWLEntity> p) {
        set.addAll(p);
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
        if (obj instanceof Signature) {
            return set.equals(((Signature) obj).set);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    private static Bag<OWLEntity> bag = new Bag<OWLEntity>();

    /** @return true iff signature contains given element */
    public boolean contains(OWLEntity p) {
        // bag.add(p);
        // if (bag.size() % 100 == 0) {
        // final List<OWLEntity> subList = bag.sortedList(false).subList(0, 10);
        // for (OWLEntity e : subList) {
        // System.out.println("Signature.contains() " + bag.check(e) + "\t" +
        // e);
        // }
        // System.out
        // .println("===========================================================");
        // }
        return set.contains(p);
    }

    public Set<OWLEntity> getSignature() {
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

    public List<OWLEntity> intersect(Signature s2) {
        List<OWLEntity> ret = new ArrayList<OWLEntity>();
        Set<OWLEntity> s = new HashSet<OWLEntity>(set);
        s.retainAll(s2.set);
        ret.addAll(s);
        return ret;
    }
}
