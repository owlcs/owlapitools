package utils.reasonercomparator;

import java.util.ArrayList;
import java.util.List;

/**
 * column
 * 
 * @author ignazio
 */
public class Column {
    /** header */
    public MethodNames header;
    /** values */
    public List<Long> values = new ArrayList<>();

    Column(MethodNames s) {
        header = s;
    }

    @Override
    public String toString() {
        return header + "\t" + values;
    }
}
