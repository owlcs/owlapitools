package decomposition;

import static org.semanticweb.owlapi.model.AxiomType.AXIOM_TYPES;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public class AxiomSelector {
    public static List<OWLAxiom> selectAxioms(OWLOntology o) {
        List<OWLAxiom> axioms = new ArrayList<OWLAxiom>();
        for (OWLOntology ont : o.getImportsClosure()) {
            for (AxiomType<? extends OWLAxiom> type : AXIOM_TYPES) {
                if (type.isLogical() || type.equals(AxiomType.DECLARATION)) {
                    for (OWLAxiom ax : ont.getAxioms(type)) {
                        axioms.add(ax);
                    }
                }
            }
        }
        return axioms;
    }

    public static List<AxiomWrapper> wrap(List<OWLAxiom> o) {
        List<AxiomWrapper> axioms = new ArrayList<AxiomWrapper>();
        for (OWLAxiom ax : o) {
            axioms.add(new AxiomWrapper(ax));
        }
        return axioms;
    }
}
