package utils.reasonercomparator;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class ReasonerPerformanceResult {
    private String reasonerName;
    private Map<MethodNames, Column> columns = new HashMap<MethodNames, Column>();

    public ReasonerPerformanceResult(String name) {
        reasonerName = name;
    }

    public ReasonerPerformanceResult(OWLReasoner r) {
        reasonerName = r.getClass().getSimpleName()
                .replace("FaCTPlusPlusReasoner", "fact")
                .replace("JFactReasoner", "jfact");
    }

    public void put(MethodNames m, Column c) {
        columns.put(m, c);
    }

    public void add(MethodNames init, long elapsed) {
        columns.get(init).values.add(elapsed);
    }

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

    public int calls(MethodNames m) {
        return columns.get(m).values.size();
    }
}
