/*
 * Date: Jan 13, 2012
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2012, Ignazio Palmisano, University of Manchester
 *
 */
package utils.reasonercomparator;

import static utils.reasonercomparator.MethodNames.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
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

/** an OWLReasoner which compares the results from different reasoners. */
@SuppressWarnings("boxing")
public class ComparisonReasoner implements OWLReasoner {

    List<OWLReasoner> delegates = new ArrayList<OWLReasoner>();
    private OWLOntology root;
    IdentityHashMap<OWLReasoner, ReasonerPerformanceResult> timings = new IdentityHashMap<OWLReasoner, ReasonerPerformanceResult>();
    boolean tolerateDifferences = true;
    boolean log = false;
    private final ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    /** @return the timings */
    public Map<OWLReasoner, ReasonerPerformanceResult> getTimings() {
        return timings;
    }

    @SuppressWarnings("null")
    @Nonnull
    static <T> T o(List<T> l) {
        return l.get(0);
    }

    /**
     * @param o
     *        the ontology to reason on
     * @param config
     *        the configuration for the reasoners; optional, can be null
     * @param delegateFactories
     *        the factories to be used to build the reasoners
     */
    public ComparisonReasoner(@Nonnull OWLOntology o,
            OWLReasonerConfiguration config,
            OWLReasonerFactory... delegateFactories) {
        root = o;
        for (int i = 0; i < delegateFactories.length; i++) {
            long l = bean.getCurrentThreadCpuTime();
            OWLReasoner r = config == null ? delegateFactories[i]
                    .createReasoner(o) : delegateFactories[i].createReasoner(o,
                    config);
            long elapsed = bean.getCurrentThreadCpuTime();
            elapsed = elapsed - l;
            delegates.add(r);
            timings.put(r, new ReasonerPerformanceResult(r));
            for (MethodNames s : MethodNames.values()) {
                timings.get(r).put(s, new Column(s));
            }
            timings.get(r).add(MethodNames.init, elapsed);
        }
    }

    /**
     * @param tolerant
     *        false if any difference should stop the reasoning
     * @param o
     *        the ontology to reason on
     * @param config
     *        the configuration for the reasoners; optional, can be null
     * @param delegateFactories
     *        the factories to be used to build the reasoners
     */
    public ComparisonReasoner(boolean tolerant, OWLOntology o,
            OWLReasonerConfiguration config,
            OWLReasonerFactory... delegateFactories) {
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
        for (MethodNames s : MethodNames.values()) {
            int calls = timings.get(delegates.get(0)).calls(s);
            b.append(s);
            b.append(',');
            b.append(calls);
            b.append(',');
            for (OWLReasoner r : delegates) {
                long avg = timings.get(r).average(s);
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

    @Nonnull
    private static <T> T verify(List<T> objects, String method,
            Object... arguments) {
        for (int i = 1; i < objects.size(); i++) {
            if (!delegateEquals(objects.get(0), objects.get(i))) {
                String template = "@Test public void shouldPass" + method;
                for (Object o : arguments) {
                    template += args(o);
                }
                template += "(){\n";
                template += vars(objects.get(0));
                for (int index = 0; index < arguments.length; index++) {
                    template += vars(arguments[index]);
                }
                template += "// expected " + args(objects.get(0))
                        + "\n// actual__ " + args(objects.get(1));
                template += "\nequal(reasoner." + method + "(";
                for (int index = 0; index < arguments.length; index++) {
                    if (index > 0) {
                        template += ", ";
                    }
                    template += args(arguments[index]);
                }
                template += "), " + args(objects.get(0)) + ");}";
                System.out.println(template);
                // template =
                // "ComparisonReasoner.verify() Object %s from reasoner %s:\n%s";
                // System.out.println(String.format(template, 0,
                // delegates.get(0)
                // .getReasonerName(), objects.get(0).toString()));
                // System.out.println(String.format(template, i,
                // delegates.get(i)
                // .getReasonerName(), objects.get(i).toString()));
                throw new RuntimeException("Spotted difference!");
            }
        }
        return o(objects);
    }

    private static String vars(Object o) {
        StringBuilder b = new StringBuilder();
        if (o instanceof Node) {
            for (OWLEntity e : ((Node<OWLEntity>) o).getEntities()) {
                b.append("OWL").append(e.getEntityType().getName()).append(" ")
                        .append(e.getIRI().getShortForm());
                b.append(" = C(\"").append(e.getIRI().getNamespace())
                        .append(e.getIRI().getShortForm()).append("\");\n");
            }
            return b.toString();
        }
        if (o instanceof NodeSet) {
            for (OWLEntity e : ((NodeSet<OWLEntity>) o).getFlattened()) {
                b.append("OWL").append(e.getEntityType().getName()).append(" ")
                        .append(e.getIRI().getShortForm());
                b.append(" = C(\"").append(e.getIRI().getNamespace())
                        .append(e.getIRI().getShortForm()).append("\");\n");
            }
            return b.toString();
        }
        if (o instanceof OWLEntity) {
            OWLEntity e = (OWLEntity) o;
            b.append("OWL").append(e.getEntityType().getName()).append(" ")
                    .append(e.getIRI().getShortForm());
            b.append(" = C(\"").append(e.getIRI().getNamespace())
                    .append(e.getIRI().getShortForm()).append("\");\n");
            return b.toString();
        }
        return "";
    }

    private static String args(Object o) {
        StringBuilder b = new StringBuilder();
        if (o instanceof Node) {
            for (OWLEntity e : ((Node<OWLEntity>) o).getEntities()) {
                b.append(e.getIRI().getShortForm()).append(", ");
            }
            b.delete(b.length() - 2, b.length());
            return b.toString();
        }
        if (o instanceof NodeSet) {
            for (OWLEntity e : ((NodeSet<OWLEntity>) o).getFlattened()) {
                b.append(e.getIRI().getShortForm()).append(", ");
            }
            b.delete(b.length() - 2, b.length());
            return b.toString();
        }
        if (o instanceof OWLEntity) {
            OWLEntity e = (OWLEntity) o;
            b.append(e.getIRI().getShortForm());
            return b.toString();
        }
        return o.toString();
    }

    // never mind the warnings, this needs to be generic enough to accept
    // anything
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean delegateEquals(Object o1, Object o2) {
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
                timings.get(delegates.get(i)).add(interrupt, elapsed);
            } catch (RuntimeException e) {
                if (!tolerateDifferences) {
                    throw e;
                }
            }
        }
    }

    @Override
    public void flush() {
        if (log) {
            System.out.println("r. flush("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        for (int i = 0; i < delegates.size(); i++) {
            try {
                long start = bean.getCurrentThreadCpuTime();
                delegates.get(i).flush();
                long elapsed = bean.getCurrentThreadCpuTime() - start;
                timings.get(delegates.get(i)).add(flush, elapsed);
            } catch (RuntimeException e) {
                if (!tolerateDifferences) {
                    throw e;
                }
            }
        }
    }

    @Override
    public List<OWLOntologyChange<?>> getPendingChanges() {
        if (log) {
            System.out.println("r. getPendingChanges("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        List<List<OWLOntologyChange<?>>> objects = new ArrayList<List<OWLOntologyChange<?>>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            List<OWLOntologyChange<?>> value = delegates.get(i)
                    .getPendingChanges();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getPendingChanges, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getPendingChanges");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i))
                    .add(getPendingAxiomAdditions, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getPendingAxiomAdditions");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getPendingAxiomRemovals, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getPendingAxiomRemovals");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
                timings.get(delegates.get(i))
                        .add(precomputeInferences, elapsed);
            } catch (RuntimeException e) {
                if (!tolerateDifferences) {
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
            timings.get(delegates.get(i)).add(isPrecomputed, elapsed);
            objects.add(value);
        }
        return o(objects);
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
            Set<InferenceType> value = delegates.get(i)
                    .getPrecomputableInferenceTypes();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getPrecomputableInferenceTypes,
                    elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getPrecomputableInferenceTypes");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(isConsistent, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "isConsistent");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(isSatisfiable, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "isSatisfiable", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getUnsatisfiableClasses, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getUnsatisfiableClasses");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(isEntailed, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "isEntailed", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> arg0) {
        if (log) {
            System.out.println("r. isEntailed("
                    + Arrays.toString(new Object[] { arg0 }) + ")");
        }
        List<Boolean> objects = new ArrayList<Boolean>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            boolean value = delegates.get(i).isEntailed(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(isEntailed, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "isEntailed", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            boolean value = delegates.get(i)
                    .isEntailmentCheckingSupported(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(isEntailmentCheckingSupported,
                    elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "isEntailmentCheckingSupported", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getTopClassNode, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getTopClassNode");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getBottomClassNode, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getBottomClassNode");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass>
            getSubClasses(OWLClassExpression arg0, boolean arg1) {
        if (log) {
            System.out.println("r. getSubClasses("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i)
                    .getSubClasses(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getSubClasses, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getSubClasses", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression arg0,
            boolean arg1) {
        if (log) {
            System.out.println("r. getSuperClasses("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i).getSuperClasses(arg0,
                    arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getSuperClasses, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getSuperClasses", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getEquivalentClasses, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getEquivalentClasses", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getDisjointClasses, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getDisjointClasses", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i))
                    .add(getTopObjectPropertyNode, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getTopObjectPropertyNode");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getBottomObjectPropertyNode,
                    elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getBottomObjectPropertyNode");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getSubObjectProperties, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getSubObjectProperties", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i))
                    .add(getSuperObjectProperties, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getSuperObjectProperties", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getEquivalentObjectProperties,
                    elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getEquivalentObjectProperties", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getDisjointObjectProperties,
                    elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getDisjointObjectProperties", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getInverseObjectProperties,
                    elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getInverseObjectProperties", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(
            OWLObjectPropertyExpression arg0, boolean arg1) {
        if (log) {
            System.out.println("r. getObjectPropertyDomains("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i)
                    .getObjectPropertyDomains(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i))
                    .add(getObjectPropertyDomains, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getObjectPropertyDomains", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(
            OWLObjectPropertyExpression arg0, boolean arg1) {
        if (log) {
            System.out.println("r. getObjectPropertyRanges("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i).getObjectPropertyRanges(
                    arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getObjectPropertyRanges, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getObjectPropertyRanges", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            Node<OWLDataProperty> value = delegates.get(i)
                    .getTopDataPropertyNode();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getTopDataPropertyNode, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getTopDataPropertyNode");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            Node<OWLDataProperty> value = delegates.get(i)
                    .getBottomDataPropertyNode();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getBottomDataPropertyNode,
                    elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getBottomDataPropertyNode");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            NodeSet<OWLDataProperty> value = delegates.get(i)
                    .getSubDataProperties(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getSubDataProperties, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getSubDataProperties", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(
            OWLDataProperty arg0, boolean arg1) {
        if (log) {
            System.out.println("r. getSuperDataProperties("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLDataProperty>> objects = new ArrayList<NodeSet<OWLDataProperty>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLDataProperty> value = delegates.get(i)
                    .getSuperDataProperties(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getSuperDataProperties, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getSuperDataProperties", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(
            OWLDataProperty arg0) {
        if (log) {
            System.out.println("r. getEquivalentDataProperties("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<Node<OWLDataProperty>> objects = new ArrayList<Node<OWLDataProperty>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            Node<OWLDataProperty> value = delegates.get(i)
                    .getEquivalentDataProperties(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getEquivalentDataProperties,
                    elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getEquivalentDataProperties", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            NodeSet<OWLDataProperty> value = delegates.get(i)
                    .getDisjointDataProperties(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getDisjointDataProperties,
                    elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getDisjointDataProperties", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty arg0,
            boolean arg1) {
        if (log) {
            System.out.println("r. getDataPropertyDomains("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLClass>> objects = new ArrayList<NodeSet<OWLClass>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLClass> value = delegates.get(i).getDataPropertyDomains(
                    arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getDataPropertyDomains, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getDataPropertyDomains", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getTypes, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getTypes", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression arg0,
            boolean arg1) {
        if (log) {
            System.out.println("r. getInstances("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLNamedIndividual>> objects = new ArrayList<NodeSet<OWLNamedIndividual>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLNamedIndividual> value = delegates.get(i).getInstances(
                    arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getInstances, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getInstances", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
            OWLNamedIndividual arg0, OWLObjectPropertyExpression arg1) {
        if (log) {
            System.out.println("r. getObjectPropertyValues("
                    + Arrays.toString(new Object[] { arg0, arg1 }) + ")");
        }
        List<NodeSet<OWLNamedIndividual>> objects = new ArrayList<NodeSet<OWLNamedIndividual>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLNamedIndividual> value = delegates.get(i)
                    .getObjectPropertyValues(arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getObjectPropertyValues, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getObjectPropertyValues", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            Set<OWLLiteral> value = delegates.get(i).getDataPropertyValues(
                    arg0, arg1);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getDataPropertyValues, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getDataPropertyValues", arg0, arg1);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            Node<OWLNamedIndividual> value = delegates.get(i)
                    .getSameIndividuals(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getSameIndividuals, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getSameIndividuals", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
            OWLNamedIndividual arg0) {
        if (log) {
            System.out.println("r. getDifferentIndividuals("
                    + Arrays.toString(Arrays.asList(arg0).toArray()) + ")");
        }
        List<NodeSet<OWLNamedIndividual>> objects = new ArrayList<NodeSet<OWLNamedIndividual>>();
        for (int i = 0; i < delegates.size(); i++) {
            long start = bean.getCurrentThreadCpuTime();
            NodeSet<OWLNamedIndividual> value = delegates.get(i)
                    .getDifferentIndividuals(arg0);
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getDifferentIndividuals, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getDifferentIndividuals", arg0);
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getTimeOut, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getTimeOut");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            timings.get(delegates.get(i)).add(getFreshEntityPolicy, elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getFreshEntityPolicy");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
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
            IndividualNodeSetPolicy value = delegates.get(i)
                    .getIndividualNodeSetPolicy();
            long elapsed = bean.getCurrentThreadCpuTime() - start;
            timings.get(delegates.get(i)).add(getIndividualNodeSetPolicy,
                    elapsed);
            objects.add(value);
        }
        try {
            return verify(objects, "getIndividualNodeSetPolicy");
        } catch (RuntimeException e) {
            if (tolerateDifferences) {
                return o(objects);
            } else {
                throw e;
            }
        }
    }

    @Override
    public void dispose() {
        if (log) {
            System.out.println("r. dispose("
                    + Arrays.toString(Arrays.asList().toArray()) + ")");
        }
        for (int i = 0; i < delegates.size(); i++) {
            try {
                long start = bean.getCurrentThreadCpuTime();
                delegates.get(i).dispose();
                long elapsed = bean.getCurrentThreadCpuTime() - start;
                timings.get(delegates.get(i)).add(dispose, elapsed);
            } catch (RuntimeException e) {
                if (!tolerateDifferences) {
                    throw e;
                }
            }
        }
    }
}
