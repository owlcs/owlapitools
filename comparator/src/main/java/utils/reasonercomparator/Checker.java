package utils.reasonercomparator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** The Class Checker. */
public class Checker {
    /** The default rep. */
    public static int defaultRep = 10;
    /** The rep. */
    protected int rep = defaultRep;
    /** The p. */
    protected final PrintStream p;
    /** The out. */
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    /** The successful. */
    private boolean successful = false;

    /**
     * Instantiates a new checker.
     * 
     * @param i
     *        the i
     */
    public Checker(int i) {
        this();
        if (i > 0) {
            rep = i;
        }
    }

    /** Instantiates a new checker. */
    public Checker() {
        p = System.out;
    }

    /**
     * Check.
     * 
     * @param cb
     *        the cb
     */
    public void check(final ComparisonExecutor cb) {
        final ConcurrentLinkedQueue<Long> results = new ConcurrentLinkedQueue<>();
        ExecutorService service = Executors.newFixedThreadPool(rep);
        for (int i = 0; i < rep; i++) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    int replay = Math.min(10, rep);
                    for (int j = 0; j < replay; j++) {
                        try {
                            cb.execute();
                            results.offer(System.currentTimeMillis());
                        } catch (Throwable e) {
                            e.printStackTrace(p);
                            printout(results);
                        }
                    }
                }
            });
        }
        service.shutdown();
        while (!service.isTerminated()) {
            try {
                service.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        printout(results);
    }

    /**
     * Printout.
     * 
     * @param results
     *        the results
     */
    protected void printout(ConcurrentLinkedQueue<Long> results) {
        List<Object> list = new ArrayList<>();
        list.addAll(Arrays.asList(results.toArray()));
        Collections.sort(list, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((Long) o1).compareTo((Long) o2);
            }
        });
        int expected = rep * Math.min(10, rep);
        successful = list.size() == expected;
    }

    /**
     * Checks if is successful.
     * 
     * @return true, if is successful
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Gets the trace.
     * 
     * @return the trace
     */
    public String getTrace() {
        p.flush();
        return out.toString();
    }
}
