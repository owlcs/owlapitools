package owlapitools;

import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import uk.ac.manchester.cs.jfact.JFactFactory;
import utils.reasonercomparator.ComparisonExecutor;

@SuppressWarnings("javadoc")
public class VerifyComplianceTestCase {

    private OWLOntology load(String in) throws OWLOntologyCreationException {
        return OWLManager.createOWLOntologyManager()
                .loadOntologyFromOntologyDocument(
                        VerifyComplianceTestCase.class.getResourceAsStream("/"
                                + in));
    }

    private ComparisonExecutor exec(OWLOntology o) throws Exception {
        ComparisonExecutor ex = new ComparisonExecutor(o,
                new SimpleConfiguration(), new Reasoner.ReasonerFactory(),
                new JFactFactory());
        ex.execute();
        return ex;
    }

    @Test
    public void shouldTestMereology() throws Exception {
        String input = "AF_mereology.owl.xml";
        OWLOntology o = load(input);
        System.out.println(exec(o));
    }

    @Test
    public void shouldTestMiniTambis() throws Exception {
        String input = "AF_miniTambis.owl.xml";
        OWLOntology o = load(input);
        System.out.println(exec(o));
    }

    @Test
    public void shouldTestOWLS() throws Exception {
        String input = "AF_OWL-S.owl.xml";
        OWLOntology o = load(input);
        System.out.println(exec(o));
    }

    @Test
    public void shouldTestPeople() throws Exception {
        String input = "AF_people.owl.xml";
        OWLOntology o = load(input);
        System.out.println(exec(o));
    }

    @Ignore
    @Test
    public void shouldTestTambisFull() throws Exception {
        String input = "AF_tambis-full.owl.xml";
        OWLOntology o = load(input);
        System.out.println(exec(o));
    }

    @Test
    public void shouldTestUniversity() throws Exception {
        String input = "AF_university.owl.xml";
        OWLOntology o = load(input);
        System.out.println(exec(o));
    }

    @Test
    public void shouldTestHost() throws Exception {
        String input = "host-pathogen-interactions-ontology.1569.owl.xml";
        OWLOntology o = load(input);
        System.out.println(exec(o));
    }

    @Ignore("Too long, results equivalent")
    @Test
    public void shouldTestNeomark() throws Exception {
        String input = "neomark-oral-cancer-ontology-version-4.1686.owl.xml";
        OWLOntology o = load(input);
        System.out.println(exec(o));
    }

    @Test
    public void shouldTestSysmo() throws Exception {
        String input = "sysmo-jerm-ontology-of-systems-biology-for-micro-organisms.1488.owl.xml";
        OWLOntology o = load(input);
        System.out.println(exec(o));
    }
}
