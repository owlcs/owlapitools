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

public class Checker {
    public static int defaultRep = 10;
    private int rep = defaultRep;
    private final PrintStream p;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private boolean successful = false;

    public Checker(int i) {
        this();
        if (i > 0) {
            rep = i;
        }
    }

    public Checker() {
        p = System.out;
    }

    public void check(final ComparisonExecutor cb) {
        final ConcurrentLinkedQueue<Long> results = new ConcurrentLinkedQueue<Long>();
        final long start = System.currentTimeMillis();
        ExecutorService service = Executors.newFixedThreadPool(rep);
        for (int i = 0; i < rep; i++) {
            service.execute(new Runnable() {
                public void run() {
                    int replay = Math.min(10, rep);
                    for (int j = 0; j < replay; j++) {
                        try {
                            cb.execute();
                            results.offer(System.currentTimeMillis());
                        } catch (Throwable e) {
                            e.printStackTrace(p);
                            printout(cb, start, results);
                            // throw e;
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
        printout(cb, start, results);
    }

    private void printout(ComparisonExecutor cb, long start,
            ConcurrentLinkedQueue<Long> results) {
        List<Object> list = new ArrayList<Object>();
        list.addAll(Arrays.asList(results.toArray()));
        Collections.sort(list, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                return ((Long) o1).compareTo((Long) o2);
            }
        });
        long end = System.currentTimeMillis();
        if (list.size() > 0) {
            end = (Long) list.get(list.size() - 1);
        }
        int expected = rep * Math.min(10, rep);
        // p.println(cb.getId() + ": elapsed time (ms): " + (end - start));
        // p.println("Successful threads: " + list.size() + "\t expected: "
        // + expected);
        successful = list.size() == expected;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getTrace() {
        p.flush();
        return out.toString();
    }
}
