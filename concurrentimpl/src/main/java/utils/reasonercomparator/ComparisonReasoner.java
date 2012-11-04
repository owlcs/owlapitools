/*
 * Date: Jan 13, 2012
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2012, Ignazio Palmisano, University of Manchester
 *
 */
package utils.reasonercomparator;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.Version;

@SuppressWarnings("boxing")
public class ComparisonReasoner implements OWLReasoner {
    public static final String[] methodNames = new String[] { "precomputeInferences",
            "isConsistent", "isSatisfiable", "getUnsatisfiableClasses", "isEntailed",
            "getSubClasses", "getSuperClasses", "getEquivalentClasses",
            "getDisjointClasses", "getTopObjectPropertyNode",
            "getBottomObjectPropertyNode", "getSubObjectProperties",
            "getSuperObjectProperties", "getEquivalentObjectProperties",
            "getDisjointObjectProperties", "getInverseObjectProperties",
            "getObjectPropertyDomains", "getObjectPropertyRanges",
            "getTopDataPropertyNode", "getBottomDataPropertyNode",
            "getSubDataProperties", "getSuperDataProperties",
            "getEquivalentDataProperties", "getDisjointDataProperties",
            "getDataPropertyDomains", "getTypes", "getInstances",
            "getObjectPropertyValues", "getDataPropertyValues", "getSameIndividuals",
            "getDifferentIndividuals", "getTopClassNode", "getBottomClassNode", "init",
            "isEntailmentCheckingSupported", "getTimeOut", "getFreshEntityPolicy",
            "getIndividualNodeSetPolicy", "interrupt", "dispose", "flush",
            "isPrecomputed", "getPrecomputableInferenceTypes", "getPendingChanges",
            "getPendingAxiomAdditions", "getPendingAxiomRemovals" };

    public static class Column {
        public String header;
        public List<Long> values = new ArrayList<Long>();

        Column(String s) {
            header = s;
        }

        @Override
        public String toString() {
            return header + "\t" + values;
        }
    }

    List<OWLReasoner> delegates = new ArrayList<OWLReasoner>();
    private OWLOntology root;
    IdentityHashMap<OWLReasoner, Map<String, Column>> timings = new IdentityHashMap<OWLReasoner, Map<String, Column>>();
    boolean tolerateDifferences = false;
    boolean log = false;
    private final ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    public Map<OWLReasoner, Map<String, Column>> getTimings() {
        return timings;
    }

    public ComparisonReasoner(OWLOntology o, OWLReasonerConfiguration config,
            OWLReasonerFactory... delegateFactories) {
        root = o;
        // try {
        // PrintStream out=new
        // PrintStream("testOntology."+bean.getCurrentThreadCpuTime()+".owl");
        // root.getOWLOntologyManager().saveOntology(root, out);
        // out.close();
        // }catch (Exception e) {
        // e.printStackTrace();
        // }
        for (int i = 0; i < delegateFactories.length; i++) {
            long l = bean.getCurrentThreadCpuTime();
            OWLReasoner r = config == null ? delegateFactories[i].createReasoner(o)
                    : delegateFactories[i].createReasoner(o, config);// , new
                                                                     // SimpleConfiguration(new
                                                                     // ConsoleProgressMonitor()));
            long elapsed = bean.getCurrentThreadCpuTime();
            elapsed = elapsed - l;
            // System.out.println("OWLReasoner r = new "+delegateFactories[i].getClass().getName()+"().createReasoner(o);");
            delegates.add(r);
            timings.put(r, new HashMap<String, Column>());
            for (String s : methodNames) {
                timings.get(r).put(s, new Column(s));
            }
            timings.get(r).get("init").values.add(elapsed);
        }
    }

    public ComparisonReasoner(boolean tolerant, OWLOntology o,
            OWLReasonerConfiguration config, OWLReasonerFactory... delegateFactories) {
        this(o, config, delegateFactories);
        tolerateDifferences = tolerant;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("method,calls,");
        for (OWLReasoner r : delegates) {
            b.append(r.getClass().getSimpleName());
            b.append(',');
        }
        b.append('\n');
        for (String s : methodNames) {
            int calls = timings.entrySet().iterator().next().getValue().get(s).values
                    .size();
            b.append(s);
            b.append(',');
            b.append(calls);
            b.append(',');
            for (OWLReasoner r : delegates) {
                Column c = timings.get(r).get(s);
                long sum = 0;
                for (Long l : c.values) {
                    sum += l.longValue();
                }
                double average = sum > 0 ? (double) sum / calls : 0;
                // skip the decimals, they are subnanosecond times - irrelevant
                long avg = Math.round(average);
                b.append(avg);
                b.append(',');
            }
            b.append('\n');
        }
        return b.toString();
    }

    @Override
    public String getReasonerName() {
        return "FederatedReasoner";
    }

    @Override
    public Version getReasonerVersion() {
        return new Version(0, 0, 0, 0);
    }

    @Override
    public BufferingMode getBufferingMode() {
        return BufferingMode.BUFFERING;
    }

    private <T> T verify(List<T> objects) {
        for (int i = 1; i < objects.size(); i++) {
            if (!delegateEquals(objects.get(0), objects.get(i))) {
                String template = "FederatedReasoner.verify() Object %s from reasoner %s:\n%s";
                System.out.println(String.format(template, 0, delegates.get(0)
                        .getReasonerName(), objects.get(0).toString()));
                System.out.println(String.format(template, i, delegates.get(i)
                        .getReasonerName(), objects.get(i).toString()));
                throw new RuntimeException("Spotted difference!");
            }
        }
        return objects.get(0);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    // never mind the warnings, this needs to be generic enough to accept
    // anything
            private static
            boolean delegateEquals(Object o1, Object o2) {
        if (o1 instanceof List) {
            Set s1 = new HashSet((List) o1);
            Set s2 = new HashSet((List) o2);
            return s1.equals(s2);
        }
        return o1.equals(o2);
    }

    @Override
    public OWLOntology getRootOntology() {
        return root;
    }

    @Override
    public void interrupt() {
        if (log) {
            System.out.println("r. interrupt("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        for (int i = 0; i < delegates.size(); i++) {
            try {
                long start = bean.getCurrentThreadCpuTime();
                delegates.get(i).interrupt();
                long elapsed = bean.getCurrentThreadCpuTime() - start;
                timings.get(delegates.get(i)).get("interrupt").values.add(elapsed);
            } catch (RuntimeException e) {
                System.out.println("Method: interrupt");
                System.out.println(Arrays.toString(Arrays.asList().toArray()));
                e.printStackTrace(System.out);
                if (!tolerateDifferences) {
                    // System.exit(0);
                    throw e;
                }
            }
        }
    }

    @Override
    public void flush() {
        if (log) {
            System.out.println("r. flush(" + Arrays.toString(Arrays.asList().toArray())
                    + ")");
        }
        for (int i = 0; i < delegates.size(); i++) {
            try {
                long start = bean.getCurrentThreadCpuTime();
                delegates.get(i).flush();
                long elapsed = bean.getCurrentThreadCpuTime() - start;
                timings.get(delegates.get(i)).get("flush").values.add(elapsed);
            } catch (RuntimeException e) {
                System.out.println("Method: flush");
                System.out.println(Arrays.toString(Arrays.asList().toArray()));
                e.printStackTrace(System.out);
                if (!tolerateDifferences) {
                    // System.exit(0);
                    throw e;
                }
            }
        }
    }

    @Override
    public List<OWLOntologyChange> getPendingChanges() {
        if (log) {
            System.out.println("r. getPendingChanges("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<List<OWLOntologyChange>> objects = new ArrayList<List<OWLOntologyChange>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            List<OWLOntologyChange> value = delegates.get(i).getPendingChanges();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getPendingChanges").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getPendingChanges");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        if (log) {
            System.out.println("r. getPendingAxiomAdditions("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Set<OWLAxiom>> objects = new ArrayList<Set<OWLAxiom>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Set<OWLAxiom> value = delegates.get(i).getPendingAxiomAdditions();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getPendingAxiomAdditions").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getPendingAxiomAdditions");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        if (log) {
            System.out.println("r. getPendingAxiomRemovals("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Set<OWLAxiom>> objects = new ArrayList<Set<OWLAxiom>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Set<OWLAxiom> value = delegates.get(i).getPendingAxiomRemovals();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getPendingAxiomRemovals").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getPendingAxiomRemovals");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public void precomputeInferences(InferenceType... arg0) {
        if (log) {
            System.out.println("r. precomputeInferences("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        for (int i = 0; i < delegates.size(); i++) {
            try {
                long start = bean.getCurrentThreadCpuTime();
                delegates.get(i).precomputeInferences(arg0);
                long elapsed = bean.getCurrentThreadCpuTime() - start;
                timings.get(delegates.get(i)).get("precomputeInferences").values
                        .add(elapsed);
            } catch (RuntimeException e) {
                System.out.println("Method: precomputeInferences");
                System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
                e.printStackTrace(System.out);
                if (!tolerateDifferences) {
                    // System.exit(0);
                    throw e;
                }
            }
        }
    }

    @Override
    public boolean isPrecomputed(InferenceType arg0) {
        if (log) {
            System.out.println("r. isPrecomputed("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<Boolean> objects = new ArrayList<Boolean>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            boolean value = delegates.get(i).isPrecomputed(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("isPrecomputed").values.add(elapsed);
            objects.add(value);
        }
        // try {
        return objects.get(0);
        // return verify(objects);
        // } catch (RuntimeException e) {
        // System.out.println("Method: isPrecomputed");
        // System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
        // e.printStackTrace(System.out);
        // if (tolerateDifferences) {
        // return objects.get(0);
        // } else {
        // //System.exit(0);
        // throw e;
        // }
        // }
    }

    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        if (log) {
            System.out.println("r. getPrecomputableInferenceTypes("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Set<InferenceType>> objects = new ArrayList<Set<InferenceType>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Set<InferenceType> value = delegates.get(i).getPrecomputableInferenceTypes();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getPrecomputableInferenceTypes").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getPrecomputableInferenceTypes");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public boolean isConsistent() {
        if (log) {
            System.out.println("r. isConsistent("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Boolean> objects = new ArrayList<Boolean>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            boolean value = delegates.get(i).isConsistent();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("isConsistent").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: isConsistent");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public boolean isSatisfiable(OWLClassExpression arg0) {
        if (log) {
            System.out.println("r. isSatisfiable("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<Boolean> objects = new ArrayList<Boolean>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            boolean value = delegates.get(i).isSatisfiable(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("isSatisfiable").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: isSatisfiable");
            System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        if (log) {
            System.out.println("r. getUnsatisfiableClasses("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Node<OWLClass>> objects = new ArrayList<Node<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLClass> value = delegates.get(i).getUnsatisfiableClasses();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getUnsatisfiableClasses").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getUnsatisfiableClasses");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public boolean isEntailed(OWLAxiom arg0) {
        if (log) {
            System.out.println("r. isEntailed("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<Boolean> objects = new ArrayList<Boolean>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            boolean value = delegates.get(i).isEntailed(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("isEntailed").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: isEntailed");
            System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> arg0) {
        if (log) {
            System.out.println("r. isEntailed(" + Arrays.toString(new Object[] { arg0 })
                    + ")");
        }
        List<Boolean> objects = new ArrayList<Boolean>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            boolean value = delegates.get(i).isEntailed(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("isEntailed").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: isEntailed");
            System.out.println(Arrays.toString(new Object[] { arg0 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> arg0) {
        if (log) {
            System.out.println("r. isEntailmentCheckingSupported("
                    + Arrays.toString(new Object[] { arg0 }) + ")");
        }
        List<Boolean> objects = new ArrayList<Boolean>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            boolean value = delegates.get(i).isEntailmentCheckingSupported(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("isEntailmentCheckingSupported").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: isEntailmentCheckingSupported");
            System.out.println(Arrays.toString(new Object[] { arg0 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
        if (log) {
            System.out.println("r. getTopClassNode("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Node<OWLClass>> objects = new ArrayList<Node<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLClass> value = delegates.get(i).getTopClassNode();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getTopClassNode").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getTopClassNode");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        if (log) {
            System.out.println("r. getBottomClassNode("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Node<OWLClass>> objects = new ArrayList<Node<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLClass> value = delegates.get(i).getBottomClassNode();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getBottomClassNode").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getBottomClassNode");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression arg0, boolean arg1) {
        if (log) {
            System.out.println("r. getSubClasses("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i).getSubClasses(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getSubClasses").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getSubClasses");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression arg0, boolean arg1) {
        if (log) {
            System.out.println("r. getSuperClasses("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i).getSuperClasses(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getSuperClasses").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getSuperClasses");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression arg0) {
        if (log) {
            System.out.println("r. getEquivalentClasses("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<Node<OWLClass>> objects = new ArrayList<Node<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLClass> value = delegates.get(i).getEquivalentClasses(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getEquivalentClasses").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getEquivalentClasses");
            System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression arg0) {
        if (log) {
            System.out.println("r. getDisjointClasses("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i).getDisjointClasses(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getDisjointClasses").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getDisjointClasses");
            System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        if (log) {
            System.out.println("r. getTopObjectPropertyNode("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Node<OWLObjectPropertyExpression>> objects = new ArrayList<Node<OWLObjectPropertyExpression>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLObjectPropertyExpression> value = delegates.get(i)
                    .getTopObjectPropertyNode();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getTopObjectPropertyNode").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getTopObjectPropertyNode");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        if (log) {
            System.out.println("r. getBottomObjectPropertyNode("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Node<OWLObjectPropertyExpression>> objects = new ArrayList<Node<OWLObjectPropertyExpression>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLObjectPropertyExpression> value = delegates.get(i)
                    .getBottomObjectPropertyNode();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getBottomObjectPropertyNode").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getBottomObjectPropertyNode");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
            OWLObjectPropertyExpression arg0, boolean arg1) {
        if (log) {
            System.out.println("r. getSubObjectProperties("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLObjectPropertyExpression>> objects = new ArrayList<NodeSet<OWLObjectPropertyExpression>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLObjectPropertyExpression> value = delegates.get(i)
                    .getSubObjectProperties(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getSubObjectProperties").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getSubObjectProperties");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
            OWLObjectPropertyExpression arg0, boolean arg1) {
        if (log) {
            System.out.println("r. getSuperObjectProperties("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLObjectPropertyExpression>> objects = new ArrayList<NodeSet<OWLObjectPropertyExpression>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLObjectPropertyExpression> value = delegates.get(i)
                    .getSuperObjectProperties(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getSuperObjectProperties").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getSuperObjectProperties");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
            OWLObjectPropertyExpression arg0) {
        if (log) {
            System.out.println("r. getEquivalentObjectProperties("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<Node<OWLObjectPropertyExpression>> objects = new ArrayList<Node<OWLObjectPropertyExpression>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLObjectPropertyExpression> value = delegates.get(i)
                    .getEquivalentObjectProperties(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getEquivalentObjectProperties").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getEquivalentObjectProperties");
            System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
            OWLObjectPropertyExpression arg0) {
        if (log) {
            System.out.println("r. getDisjointObjectProperties("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<NodeSet<OWLObjectPropertyExpression>> objects = new ArrayList<NodeSet<OWLObjectPropertyExpression>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLObjectPropertyExpression> value = delegates.get(i)
                    .getDisjointObjectProperties(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getDisjointObjectProperties").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getDisjointObjectProperties");
            System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
            OWLObjectPropertyExpression arg0) {
        if (log) {
            System.out.println("r. getInverseObjectProperties("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<Node<OWLObjectPropertyExpression>> objects = new ArrayList<Node<OWLObjectPropertyExpression>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLObjectPropertyExpression> value = delegates.get(i)
                    .getInverseObjectProperties(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getInverseObjectProperties").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getInverseObjectProperties");
            System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression arg0,
            boolean arg1) {
        if (log) {
            System.out.println("r. getObjectPropertyDomains("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i).getObjectPropertyDomains(arg0,
                    arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getObjectPropertyDomains").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getObjectPropertyDomains");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression arg0,
            boolean arg1) {
        if (log) {
            System.out.println("r. getObjectPropertyRanges("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i)
                    .getObjectPropertyRanges(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getObjectPropertyRanges").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getObjectPropertyRanges");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        if (log) {
            System.out.println("r. getTopDataPropertyNode("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Node<OWLDataProperty>> objects = new ArrayList<Node<OWLDataProperty>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLDataProperty> value = delegates.get(i).getTopDataPropertyNode();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getTopDataPropertyNode").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getTopDataPropertyNode");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        if (log) {
            System.out.println("r. getBottomDataPropertyNode("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Node<OWLDataProperty>> objects = new ArrayList<Node<OWLDataProperty>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLDataProperty> value = delegates.get(i).getBottomDataPropertyNode();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getBottomDataPropertyNode").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getBottomDataPropertyNode");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty arg0,
            boolean arg1) {
        if (log) {
            System.out.println("r. getSubDataProperties("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLDataProperty>> objects = new ArrayList<NodeSet<OWLDataProperty>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLDataProperty> value = delegates.get(i).getSubDataProperties(arg0,
                    arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getSubDataProperties").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getSubDataProperties");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty arg0,
            boolean arg1) {
        if (log) {
            System.out.println("r. getSuperDataProperties("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLDataProperty>> objects = new ArrayList<NodeSet<OWLDataProperty>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLDataProperty> value = delegates.get(i).getSuperDataProperties(
                    arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getSuperDataProperties").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getSuperDataProperties");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty arg0) {
        if (log) {
            System.out.println("r. getEquivalentDataProperties("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<Node<OWLDataProperty>> objects = new ArrayList<Node<OWLDataProperty>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLDataProperty> value = delegates.get(i).getEquivalentDataProperties(
                    arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getEquivalentDataProperties").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getEquivalentDataProperties");
            System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(
            OWLDataPropertyExpression arg0) {
        if (log) {
            System.out.println("r. getDisjointDataProperties("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<NodeSet<OWLDataProperty>> objects = new ArrayList<NodeSet<OWLDataProperty>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLDataProperty> value = delegates.get(i).getDisjointDataProperties(
                    arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getDisjointDataProperties").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getDisjointDataProperties");
            System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty arg0, boolean arg1) {
        if (log) {
            System.out.println("r. getDataPropertyDomains("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i).getDataPropertyDomains(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getDataPropertyDomains").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getDataPropertyDomains");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual arg0, boolean arg1) {
        if (log) {
            System.out.println("r. getTypes("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i).getTypes(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getTypes").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getTypes");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLNamedIndividual>
            getInstances(OWLClassExpression arg0, boolean arg1) {
        if (log) {
            System.out.println("r. getInstances("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLNamedIndividual>> objects = new ArrayList<NodeSet<OWLNamedIndividual>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLNamedIndividual> value = delegates.get(i).getInstances(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getInstances").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getInstances");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual arg0,
            OWLObjectPropertyExpression arg1) {
        if (log) {
            System.out.println("r. getObjectPropertyValues("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLNamedIndividual>> objects = new ArrayList<NodeSet<OWLNamedIndividual>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLNamedIndividual> value = delegates.get(i).getObjectPropertyValues(
                    arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getObjectPropertyValues").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getObjectPropertyValues");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual arg0,
            OWLDataProperty arg1) {
        if (log) {
            System.out.println("r. getDataPropertyValues("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<Set<OWLLiteral>> objects = new ArrayList<Set<OWLLiteral>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Set<OWLLiteral> value = delegates.get(i).getDataPropertyValues(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getDataPropertyValues").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getDataPropertyValues");
            System.out.println(Arrays.toString(new Object[] { arg0, arg1 }));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual arg0) {
        if (log) {
            System.out.println("r. getSameIndividuals("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<Node<OWLNamedIndividual>> objects = new ArrayList<Node<OWLNamedIndividual>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLNamedIndividual> value = delegates.get(i).getSameIndividuals(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getSameIndividuals").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getSameIndividuals");
            System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual arg0) {
        if (log) {
            System.out.println("r. getDifferentIndividuals("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<NodeSet<OWLNamedIndividual>> objects = new ArrayList<NodeSet<OWLNamedIndividual>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLNamedIndividual> value = delegates.get(i).getDifferentIndividuals(
                    arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getDifferentIndividuals").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getDifferentIndividuals");
            System.out.println(Arrays.toString(Arrays.asList(arg0).toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public long getTimeOut() {
        if (log) {
            System.out.println("r. getTimeOut("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<Long> objects = new ArrayList<Long>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            long value = delegates.get(i).getTimeOut();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getTimeOut").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getTimeOut");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        if (log) {
            System.out.println("r. getFreshEntityPolicy("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<FreshEntityPolicy> objects = new ArrayList<FreshEntityPolicy>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            FreshEntityPolicy value = delegates.get(i).getFreshEntityPolicy();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getFreshEntityPolicy").values.add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getFreshEntityPolicy");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        if (log) {
            System.out.println("r. getIndividualNodeSetPolicy("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<IndividualNodeSetPolicy> objects = new ArrayList<IndividualNodeSetPolicy>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            IndividualNodeSetPolicy value = delegates.get(i).getIndividualNodeSetPolicy();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).get("getIndividualNodeSetPolicy").values
                    .add(elapsed);
            objects.add(value);
        }
        try {
            return verify(objects);
        } catch (RuntimeException e) {
            System.out.println("Method: getIndividualNodeSetPolicy");
            System.out.println(Arrays.toString(Arrays.asList().toArray()));
            e.printStackTrace(System.out);
            if (tolerateDifferences) {
                return objects.get(0);
            } else {
                // System.exit(0);
                throw e;
            }
        }
    }

    @Override
    public void dispose() {
        if (log) {
            System.out.println("r. dispose(" + Arrays.toString(Arrays.asList().toArray())
                    + ")");
        }
        for (int i = 0; i < delegates.size(); i++) {
            try {
                long start = bean.getCurrentThreadCpuTime();
                delegates.get(i).dispose();
                long elapsed = bean.getCurrentThreadCpuTime() - start;
                timings.get(delegates.get(i)).get("dispose").values.add(elapsed);
            } catch (RuntimeException e) {
                System.out.println("Method: dispose");
                System.out.println(Arrays.toString(Arrays.asList().toArray()));
                e.printStackTrace(System.out);
                if (!tolerateDifferences) {
                    // System.exit(0);
                    throw e;
                }
            }
        }
    }
}
