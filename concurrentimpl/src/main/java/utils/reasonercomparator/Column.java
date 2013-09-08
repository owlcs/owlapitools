package utils.reasonercomparator;

import java.util.ArrayList;
import java.util.List;

public class Column {
    public MethodNames header;
    public List<Long> values = new ArrayList<Long>();

    Column(MethodNames s) {
        header = s;
    }

    @Override
    public String toString() {
        return header + "\t" + values;
    }
}
