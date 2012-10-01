package decomposition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** atomical ontology structure */
public class AtomList {
    /** all the atoms */
    private List<OntologyAtom> Atoms = new ArrayList<OntologyAtom>();

    /** create a new atom and get a pointer to it */
    public OntologyAtom newAtom() {
        OntologyAtom ret = new OntologyAtom();
        ret.setId(Atoms.size());
        Atoms.add(ret);
        return ret;
    }

    /** reduce graph of the atoms in the structure */
    public void reduceGraph() {
        Set<OntologyAtom> checked = new HashSet<OntologyAtom>();
        for (OntologyAtom p : Atoms) {
            p.getAllDepAtoms(checked);
        }
    }

    /** get RW atom by its index */
    public OntologyAtom get(int index) {
        return Atoms.get(index);
    }

    /** size of the structure */
    public int size() {
        return Atoms.size();
    }
}
