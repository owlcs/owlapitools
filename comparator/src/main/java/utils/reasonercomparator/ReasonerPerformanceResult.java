package utils.reasonercomparator;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

/** The Class ReasonerPerformanceResult. */
public class ReasonerPerformanceResult {
    /** The reasoner name. */
    private String reasonerName;
    /** The columns. */
    private Map<MethodNames, Column> columns = new HashMap<MethodNames, Column>();

    /** Instantiates a new reasoner performance result.
     * 
     * @param name
     *            the name */
    public ReasonerPerformanceResult(String name) {
        reasonerName = name;
    }

    /** Instantiates a new reasoner performance result.
     * 
     * @param r
     *            the r */
    public ReasonerPerformanceResult(OWLReasoner r) {
        reasonerName = r.getClass().getSimpleName()
                .replace("FaCTPlusPlusReasoner", "fact")
                .replace("JFactReasoner", "jfact");
    }

    /** Put.
     * 
     * @param m
     *            the m
     * @param c
     *            the c */
    public void put(MethodNames m, Column c) {
        columns.put(m, c);
    }

    /** Adds the.
     * 
     * @param init
     *            the init
     * @param elapsed
     *            the elapsed */
    public void add(MethodNames init, long elapsed) {
        columns.get(init).values.add(elapsed);
    }

    /** Average.
     * 
     * @param m
     *            the m
     * @return the long */
    public long average(MethodNames m) {
        Column c = columns.get(m);
        long sum = 0;
        for (Long l : c.values) {
            sum += l.longValue();
        }
        double average = sum > 0 ? (double) sum / c.values.size() : 0;
        // skip the decimals, they are subnanosecond times - irrelevant
        return Math.round(average);
    }

    /** Calls.
     * 
     * @param m
     *            the m
     * @return the int */
    public int calls(MethodNames m) {
        return columns.get(m).values.size();
    }
}
