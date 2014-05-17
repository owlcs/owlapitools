package utils.reasonercomparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/** The Class PerformanceComparator. */
public class PerformanceComparator {

    /**
     * Run test.
     * 
     * @param o1
     *        the o1
     * @param factories
     *        the factories
     * @return the list
     * @throws Exception
     *         the exception
     */
    public static List<ReasonerPerformanceResult> runTest(OWLOntology o1,
            OWLReasonerFactory... factories) throws Exception {
        Checker checker = new Checker(1);
        // OWL2DLProfile profile = new OWL2DLProfile();
        // OWLProfileReport report = profile.checkOntology(o1);
        // for (OWLProfileViolation<?> v : report.getViolations()) {
        // System.out.println(v);
        // }
        List<ReasonerPerformanceResult> toReturn = new ArrayList<ReasonerPerformanceResult>();
        // TODO number of runs
        ComparisonExecutor ccb = new ComparisonExecutor(o1, null, factories);
        checker.check(ccb);
        String trace = checker.getTrace();
        System.out.println(trace);
        ccb.getReasoner().interrupt();
        ccb.getReasoner().dispose();
        toReturn.addAll(ccb.getReasoner().getTimings().values());
        return toReturn;
    }

    /**
     * Run.
     * 
     * @param ontologies
     *        the ontologies
     * @param factories
     *        the factories
     * @return the string
     * @throws Exception
     *         the exception
     */
    public static String run(Collection<OWLOntology> ontologies,
            OWLReasonerFactory... factories) throws Exception {
        Map<String, List<ReasonerPerformanceResult>> list = new HashMap<String, List<ReasonerPerformanceResult>>();
        for (OWLOntology o : ontologies) {
            list.put(o.getOntologyID().getOntologyIRI().toString(),
                    runTest(o, factories));
        }
        return toString(list);
    }

    /**
     * To string.
     * 
     * @param timingsList
     *        the timings list
     * @return the string
     */
    public static String toString(
            Map<String, List<ReasonerPerformanceResult>> timingsList) {
        StringBuilder b = new StringBuilder("method,");
        b.append("calls,");
        for (MethodNames m : MethodNames.values()) {
            b.append(m);
            b.append(',');
        }
        b.append('\n');
        for (MethodNames s : MethodNames.values()) {
            b.append(s);
            b.append(',');
            for (List<ReasonerPerformanceResult> map : timingsList.values()) {
                int calls = map.get(0).calls(s);
                b.append(calls);
                b.append(',');
                for (ReasonerPerformanceResult result : map) {
                    long avg = result.average(s);
                    b.append(avg);
                    b.append(',');
                }
            }
            b.append('\n');
        }
        return b.toString();
    }
}
