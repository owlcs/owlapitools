package decomposition;

import java.util.HashMap;
import java.util.Map;

public class AxiomStructure {
    Map<AxiomWrapper, TOntologyAtom> axiomToAtom = new HashMap<AxiomWrapper, TOntologyAtom>();

    public void setAtomForAxiom(AxiomWrapper ax, TOntologyAtom atom) {
        axiomToAtom.put(ax, atom);
    }

    public TOntologyAtom getAtom(AxiomWrapper ax) {
        return axiomToAtom.get(ax);
    }
}
