package decomposition;

import static org.semanticweb.owlapi.model.AxiomType.AXIOM_TYPES;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A filter for axioms
 * 
 * @author ignazio
 */
public class AxiomSelector {

    /**
     * @param o
     *            the ontology to filter
     * @return list of declarations and logical axioms
     */
    public static List<OWLAxiom> selectAxioms(OWLOntology o) {
        List<OWLAxiom> axioms = new ArrayList<>();
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

    /**
     * @param o
     *            axioms to wrap
     * @return axioms wrapped as AxiomWrapper
     */
    public static List<AxiomWrapper> wrap(List<OWLAxiom> o) {
        List<AxiomWrapper> axioms = new ArrayList<>();
        for (OWLAxiom ax : o) {
            axioms.add(new AxiomWrapper(ax));
        }
        return axioms;
    }
}
