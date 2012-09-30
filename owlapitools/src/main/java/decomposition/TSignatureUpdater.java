package decomposition;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/** update signature by adding the signature of a given axiom to it */
@SuppressWarnings("javadoc")
public class TSignatureUpdater {
    /** load ontology to a given KB */
    public void visitOntology(OWLOntology ontology, TSignature sig) {
        for (OWLAxiom p : ontology.getAxioms()) {
            // if (p.isUsed()) {
            // sig.addAll(p.getSignature());
            // }
        }
    }
}
