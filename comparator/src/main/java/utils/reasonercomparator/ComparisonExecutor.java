package utils.reasonercomparator;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * Executes a set of reasoner calls
 * 
 * @author ignazio
 */
public class ComparisonExecutor {

    private OWLReasonerFactory[] reasonerFactories;
    protected OWLOntology o1;
    protected OWLReasoner r;
    protected String name;

    /** @return id for the executor */
    public String getId() {
        return "stress test for " + name;
    }

    /**
     * run execution
     */
    public void execute() {
        r.getBottomClassNode();
        r.getBottomDataPropertyNode();
        r.getBottomObjectPropertyNode();
        // r.getFreshEntityPolicy();
        // r.getIndividualNodeSetPolicy();
        // r.getPendingAxiomAdditions();
        // r.getPendingAxiomRemovals();
        // r.getPendingChanges();
        // r.getPrecomputableInferenceTypes();
        r.getTimeOut();
        r.getTopClassNode();
        r.getTopDataPropertyNode();
        r.getTopObjectPropertyNode();
        r.isConsistent();
        r.getUnsatisfiableClasses();
        // r.isEntailmentCheckingSupported(AxiomType.CLASS_ASSERTION);
        // for (AxiomType t : AxiomType.AXIOM_TYPES) {
        // r.isEntailmentCheckingSupported(t);
        // }
        for (InferenceType t : InferenceType.values()) {
            r.isPrecomputed(t);
        }
        Set<OWLClass> classes = o1.getClassesInSignature();
        for (OWLClass c : classes) {
            r.isSatisfiable(c);
        }
        Set<OWLObjectProperty> objectProperties = o1
                .getObjectPropertiesInSignature(Imports.INCLUDED);
        Set<OWLDataProperty> dataProperties = o1.getDataPropertiesInSignature();
        Set<OWLNamedIndividual> individuals = o1.getIndividualsInSignature();
        for (OWLObjectProperty o : objectProperties) {
            checkObject(o);
        }
        OWLClass thing = o1.getOWLOntologyManager().getOWLDataFactory()
                .getOWLThing();
        {
            NodeSet<OWLClass> subclasses = r.getSubClasses(thing, false);
            NodeSet<OWLClass> superclasses = r.getSuperClasses(thing, false);
            checkClasses(thing);
            for (Node<OWLClass> n : subclasses) {
                for (OWLClass sub : n) {
                    for (Node<OWLClass> n1 : superclasses) {
                        for (OWLClass sup : n1) {
                            assertTrue(
                                    "Wrong classification result! "
                                            + sub.getIRI()
                                            + " not subclass of "
                                            + sup.getIRI()
                                            + " but it was supposed to be!",
                                    r.isEntailed(o1.getOWLOntologyManager()
                                            .getOWLDataFactory()
                                            .getOWLSubClassOfAxiom(sub, sup)));
                        }
                    }
                }
            }
        }
        for (OWLClass c : classes) {
            NodeSet<OWLClass> subclasses = r.getSubClasses(c, false);
            NodeSet<OWLClass> superclasses = r.getSuperClasses(c, false);
            checkClasses(c);
            for (Node<OWLClass> n : subclasses) {
                for (OWLClass sub : n) {
                    for (Node<OWLClass> n1 : superclasses) {
                        for (OWLClass sup : n1) {
                            String message = "Wrong classification result! "
                                    + sub.getIRI() + " not subclass of "
                                    + sup.getIRI()
                                    + " but it was supposed to be!";
                            assertTrue(
                                    message,
                                    r.isEntailed(o1.getOWLOntologyManager()
                                            .getOWLDataFactory()
                                            .getOWLSubClassOfAxiom(sub, sup)));
                        }
                    }
                }
            }
        }
        for (OWLDataProperty p : dataProperties) {
            checkDataProperties(p);
        }
        checkIndividuals(objectProperties, dataProperties, individuals);
        checkTautology(0);
        r.flush();
        // r.interrupt();
        // r.dispose();
    }

    /**
     * check tautology
     * 
     * @param index
     *        repetition index
     */
    public void checkTautology(int index) {
        for (OWLAxiom ax : o1.getLogicalAxioms()) {
            if (!r.isEntailed(ax)) {
                System.out.println("ReloadingTestCallBack.checkTautology()");
                r.isEntailed(ax);
            }
            assertTrue("Axiom was supposed to be entailed! repetition: "
                    + index + " axiom:" + ax, r.isEntailed(ax));
        }
    }

    private void checkIndividuals(Set<OWLObjectProperty> objectProperties,
            Set<OWLDataProperty> dataProperties,
            Set<OWLNamedIndividual> individuals) {
        for (OWLNamedIndividual i : individuals) {
            r.getTypes(i, false);
            r.getTypes(i, true);
            r.getSameIndividuals(i);
            r.getDifferentIndividuals(i);
            for (OWLObjectProperty o : objectProperties) {
                r.getObjectPropertyValues(i, o);
            }
            for (OWLDataProperty p : dataProperties) {
                r.getDataPropertyValues(i, p);
            }
        }
    }

    private void checkDataProperties(OWLDataProperty p) {
        r.getSubDataProperties(p, false);
        r.getSuperDataProperties(p, false);
        r.getDataPropertyDomains(p, false);
        r.getSubDataProperties(p, true);
        r.getSuperDataProperties(p, true);
        r.getDataPropertyDomains(p, true);
        r.getEquivalentDataProperties(p);
        r.getDisjointDataProperties(p);
    }

    private void checkClasses(OWLClass c) {
        r.getSubClasses(c, false);
        r.getSuperClasses(c, false);
        r.getInstances(c, false);
        r.getSubClasses(c, true);
        r.getSuperClasses(c, true);
        r.getInstances(c, true);
        r.getEquivalentClasses(c);
        r.getDisjointClasses(c);
    }

    private void checkObject(OWLObjectProperty o) {
        r.getSubObjectProperties(o, false);
        r.getSuperObjectProperties(o, false);
        r.getObjectPropertyDomains(o, false);
        r.getObjectPropertyRanges(o, false);
        r.getSubObjectProperties(o, true);
        r.getSuperObjectProperties(o, true);
        r.getObjectPropertyDomains(o, true);
        r.getObjectPropertyRanges(o, true);
        r.getEquivalentObjectProperties(o);
        r.getDisjointObjectProperties(o);
        r.getInverseObjectProperties(o);
    }

    /**
     * @param o
     *        ontology
     * @param c
     *        configuration
     * @param f
     *        reasoner factories
     */
    public ComparisonExecutor(OWLOntology o, OWLReasonerConfiguration c,
            OWLReasonerFactory... f) {
        o1 = o;
        name = "Comparing";
        reasonerFactories = f;
        r = new ComparisonReasoner(o1, c, reasonerFactories);
        r.precomputeInferences(InferenceType.values());
    }

    /** @return comparison reasoner */
    public ComparisonReasoner getReasoner() {
        return (ComparisonReasoner) r;
    }

    @Override
    public String toString() {
        return r.toString();
    }
}
