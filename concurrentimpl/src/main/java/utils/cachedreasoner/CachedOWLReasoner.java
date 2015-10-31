/*
 * Date: Jan 13, 2012
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2012, Ignazio Palmisano, University of Manchester
 *
 */
package utils.cachedreasoner;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.configurables.Computable;
import org.semanticweb.owlapi.apibinding.configurables.ComputableAllThrowables;
import org.semanticweb.owlapi.apibinding.configurables.MemoizingCache;
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
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.AxiomNotInProfileException;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.ClassExpressionNotInProfileException;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.semanticweb.owlapi.util.Version;

/** @author ignazio */
@SuppressWarnings("unchecked")
public final class CachedOWLReasoner implements OWLReasoner,
        OWLOntologyChangeListener {

    protected final OWLReasoner delegate;

    private final class Entailer extends ComputableAllThrowables<Object> {
        private final OWLClassExpression classExpression;

        public Entailer(OWLClassExpression classExpression) {
            this.classExpression = classExpression;
        }

        @Override
        @SuppressWarnings("boxing")
        public Object compute() {
            try {
                return delegate.isSatisfiable(classExpression);
            } catch (Throwable e) {
                exception = e;
            }
            return null;
        }
    }

    private static final class BoolKey {
        final Object o;
        final boolean b;

        public BoolKey(Object o, boolean b) {
            this.o = o;
            this.b = b;
        }

        @Override
        public int hashCode() {
            return o.hashCode() * (b ? 1 : -1);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof BoolKey && b == ((BoolKey) obj).b
                    && o.equals(((BoolKey) obj).o);
        }
    }

    private static final class RegKey {
        final Object o1;
        final Object o2;

        public RegKey(Object o1, Object o2) {
            this.o1 = o1;
            this.o2 = o2;
        }

        @Override
        public int hashCode() {
            return o1.hashCode() * o2.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof RegKey && o1.equals(((RegKey) obj).o1)
                    && o2.equals(((RegKey) obj).o2);
        }
    }

    private final static class CachedReasoner {
        public static final Object makeKey(Object o1, boolean o2) {
            return new BoolKey(o1, o2);
        }

        public CachedReasoner() {}

        MemoizingCache<CacheKeys, MemoizingCache<Object, Object>> mainCache = new MemoizingCache<CacheKeys, MemoizingCache<Object, Object>>();

        public void clear() {
            mainCache.clear();
        }

        public <T> T get(CacheKeys cachekey, Object key, Computable<Object> c) {
            Computable<MemoizingCache<Object, Object>> cacheinit = new ComputableAllThrowables<MemoizingCache<Object, Object>>() {
                @Override
                public MemoizingCache<Object, Object> compute() {
                    try {
                        return new MemoizingCache<Object, Object>();
                    } catch (Throwable e) {
                        exception = e;
                        return null;
                    }
                }
            };
            T t = (T) mainCache.get(cacheinit, cachekey).get(c, key);
            if (cacheinit.hasThrownException()) {
                if (cacheinit.thrownException() instanceof Error) {
                    throw (Error) cacheinit.thrownException();
                }
                throw (RuntimeException) cacheinit.thrownException();
            }
            return t;
        }
    }

    private final CachedReasoner cache = new CachedReasoner();
    private final OWLOntology rootOntology;

    /**
     * @param reasoner
     *            reasoner
     * @param manager
     *        manager
     */
    public CachedOWLReasoner(OWLReasoner reasoner, OWLOntologyManager manager) {
        if (reasoner == null) {
            throw new IllegalArgumentException(
                    "The input reasoner cannot be null");
        }
        delegate = reasoner;
        manager.addOntologyChangeListener(this);
        rootOntology = delegate.getRootOntology();
    }

    private enum CacheKeys {
        isEntailed,
        subclasses,
        subclassesDirect,
        superclassesDirect,
        superclasses,
        equivclasses,
        disjointclasses,
        subobjectpropertiesDirect,
        subobjectproperties,
        superobjectpropertiesDirect,
        superobjectproperties,
        equivobjectproperties,
        disjointobjectproperties,
        inverseobjectproperties,
        objectpropertiesdomainsDirect,
        objectpropertiesdomains,
        objectpropertiesranges,
        objectpropertiesrangesDirect,
        diffindividual,
        datapropertiesdomains,
        datapropertiesdomainsDirect,
        datapropertiesvalues,
        disjointdataproperties,
        equivdataproperties,
        instances,
        instancesDirect,
        objectpropertiesvalues,
        sameindividual,
        subdataproperties,
        subdatapropertiesDirect,
        superdataproperties,
        superdatapropertiesDirect,
        types,
        typesDirect,
        issatisfiable
    }

    @Override
    public String getReasonerName() {
        return delegate.getReasonerName();
    }

    @Override
    public Version getReasonerVersion() {
        return delegate.getReasonerVersion();
    }

    @Override
    public BufferingMode getBufferingMode() {
        return delegate.getBufferingMode();
    }

    @Override
    public void flush() {
        cache.clear();
        delegate.flush();
    }

    @Override
    public List<OWLOntologyChange<?>> getPendingChanges() {
        return delegate.getPendingChanges();
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        return delegate.getPendingAxiomAdditions();
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        return delegate.getPendingAxiomRemovals();
    }

    @Override
    public OWLOntology getRootOntology() {
        return rootOntology;
    }

    @Override
    public void interrupt() {
        cache.clear();
        delegate.interrupt();
    }

    @Override
    public void precomputeInferences(InferenceType... inferenceTypes)
            throws ReasonerInterruptedException, TimeOutException,
            InconsistentOntologyException {
        cache.clear();
        delegate.precomputeInferences(inferenceTypes);
    }

    @Override
    public boolean isPrecomputed(InferenceType inferenceType) {
        return delegate.isPrecomputed(inferenceType);
    }

    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        return delegate.getPrecomputableInferenceTypes();
    }

    @Override
    public boolean isConsistent() throws ReasonerInterruptedException,
            TimeOutException {
        return delegate.isConsistent();
    }

    @Override
    public boolean isSatisfiable(final OWLClassExpression classExpression)
            throws ReasonerInterruptedException, TimeOutException,
            ClassExpressionNotInProfileException, FreshEntitiesException,
            InconsistentOntologyException {
        ComputableAllThrowables<Object> entailer = new Entailer(classExpression);
        Boolean b = cache.get(CacheKeys.issatisfiable, classExpression,
                entailer);
        if (entailer.hasThrownException()) {
            Throwable e = entailer.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof AxiomNotInProfileException) {
                throw (AxiomNotInProfileException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return b.booleanValue();
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses()
            throws ReasonerInterruptedException, TimeOutException,
            InconsistentOntologyException {
        return delegate.getUnsatisfiableClasses();
    }

    @Override
    public boolean isEntailed(final OWLAxiom axiom)
            throws ReasonerInterruptedException,
            UnsupportedEntailmentTypeException, TimeOutException,
            AxiomNotInProfileException, FreshEntitiesException,
            InconsistentOntologyException {
        ComputableAllThrowables<Object> entailer = new ComputableAllThrowables<Object>() {
            @Override
            @SuppressWarnings("boxing")
            public Object compute() {
                try {
                    return delegate.isEntailed(axiom);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Boolean b = cache.get(CacheKeys.isEntailed, axiom, entailer);
        if (entailer.hasThrownException()) {
            Throwable e = entailer.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof UnsupportedEntailmentTypeException) {
                throw (UnsupportedEntailmentTypeException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof AxiomNotInProfileException) {
                throw (AxiomNotInProfileException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return b.booleanValue();
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> axioms)
            throws ReasonerInterruptedException,
            UnsupportedEntailmentTypeException, TimeOutException,
            AxiomNotInProfileException, FreshEntitiesException,
            InconsistentOntologyException {
        for (OWLAxiom ax : axioms) {
            if (!isEntailed(ax)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return delegate.isEntailmentCheckingSupported(axiomType);
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
        return delegate.getTopClassNode();
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        return delegate.getBottomClassNode();
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(final OWLClassExpression ce,
            final boolean direct) throws ReasonerInterruptedException,
            TimeOutException, FreshEntitiesException,
            InconsistentOntologyException, ClassExpressionNotInProfileException {
        final CacheKeys key = direct ? CacheKeys.subclassesDirect
                : CacheKeys.subclasses;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getSubClasses(ce, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(ce, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof UnsupportedEntailmentTypeException) {
                throw (UnsupportedEntailmentTypeException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof AxiomNotInProfileException) {
                throw (AxiomNotInProfileException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(final OWLClassExpression ce,
            final boolean direct) throws InconsistentOntologyException,
            ClassExpressionNotInProfileException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.superclassesDirect
                : CacheKeys.superclasses;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getSuperClasses(ce, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(ce, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof AxiomNotInProfileException) {
                throw (AxiomNotInProfileException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Failure asking for superclasses of "
                    + ce + " " + direct, e);
        }
        return toReturn;
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(final OWLClassExpression ce)
            throws InconsistentOntologyException,
            ClassExpressionNotInProfileException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.equivclasses;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getEquivalentClasses(ce);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Node<OWLClass> toReturn = (Node<OWLClass>) cache.get(key, ce, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLClass> getDisjointClasses(final OWLClassExpression ce)
            throws ReasonerInterruptedException, TimeOutException,
            FreshEntitiesException, InconsistentOntologyException {
        final CacheKeys key = CacheKeys.disjointclasses;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getDisjointClasses(ce);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key, ce,
                checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        return delegate.getTopObjectPropertyNode();
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        return delegate.getBottomObjectPropertyNode();
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
            final OWLObjectPropertyExpression pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.subobjectpropertiesDirect
                : CacheKeys.subobjectproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getSubObjectProperties(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
                .get(key, CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
            final OWLObjectPropertyExpression pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.superobjectpropertiesDirect
                : CacheKeys.superobjectproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getSuperObjectProperties(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
                .get(key, CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
            final OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.equivobjectproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getEquivalentObjectProperties(pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Node<OWLObjectPropertyExpression> toReturn = (Node<OWLObjectPropertyExpression>) cache
                .get(key, pe, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
            final OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.disjointobjectproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getDisjointObjectProperties(pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
                .get(key, pe, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
            final OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.inverseobjectproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getInverseObjectProperties(pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Node<OWLObjectPropertyExpression> toReturn = (Node<OWLObjectPropertyExpression>) cache
                .get(key, pe, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(
            final OWLObjectPropertyExpression pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.objectpropertiesdomainsDirect
                : CacheKeys.objectpropertiesdomains;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getObjectPropertyDomains(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(
            final OWLObjectPropertyExpression pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.objectpropertiesrangesDirect
                : CacheKeys.objectpropertiesranges;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getObjectPropertyRanges(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        return delegate.getTopDataPropertyNode();
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        return delegate.getBottomDataPropertyNode();
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(
            final OWLDataProperty pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.subdatapropertiesDirect
                : CacheKeys.subdataproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getSubDataProperties(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
                .get(key, CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(
            final OWLDataProperty pe, final boolean direct)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.superdatapropertiesDirect
                : CacheKeys.superdataproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getSuperDataProperties(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
                .get(key, CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(
            final OWLDataProperty pe) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = CacheKeys.equivdataproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getEquivalentDataProperties(pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Node<OWLDataProperty> toReturn = (Node<OWLDataProperty>) cache.get(key,
                pe, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(
            final OWLDataPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.disjointdataproperties;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getDisjointDataProperties(pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
                .get(key, pe, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(final OWLDataProperty pe,
            final boolean direct) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = direct ? CacheKeys.datapropertiesdomainsDirect
                : CacheKeys.datapropertiesdomains;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getDataPropertyDomains(pe, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(pe, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLClass> getTypes(final OWLNamedIndividual ind,
            final boolean direct) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = direct ? CacheKeys.typesDirect : CacheKeys.types;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getTypes(ind, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
                CachedReasoner.makeKey(ind, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(
            final OWLClassExpression ce, final boolean direct)
            throws InconsistentOntologyException,
            ClassExpressionNotInProfileException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = direct ? CacheKeys.instancesDirect
                : CacheKeys.instances;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getInstances(ce, direct);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
                .get(key, CachedReasoner.makeKey(ce, direct), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
            final OWLNamedIndividual ind, final OWLObjectPropertyExpression pe)
            throws InconsistentOntologyException, FreshEntitiesException,
            ReasonerInterruptedException, TimeOutException {
        final CacheKeys key = CacheKeys.objectpropertiesvalues;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getObjectPropertyValues(ind, pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
                .get(key, new RegKey(ind, pe), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(final OWLNamedIndividual ind,
            final OWLDataProperty pe) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = CacheKeys.datapropertiesvalues;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getDataPropertyValues(ind, pe);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Set<OWLLiteral> toReturn = (Set<OWLLiteral>) cache.get(key, new RegKey(
                ind, pe), checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(
            final OWLNamedIndividual ind) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = CacheKeys.sameindividual;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getSameIndividuals(ind);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        Node<OWLNamedIndividual> toReturn = (Node<OWLNamedIndividual>) cache
                .get(key, ind, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
            final OWLNamedIndividual ind) throws InconsistentOntologyException,
            FreshEntitiesException, ReasonerInterruptedException,
            TimeOutException {
        final CacheKeys key = CacheKeys.diffindividual;
        Computable<Object> checker = new ComputableAllThrowables<Object>() {
            @Override
            public Object compute() {
                try {
                    return delegate.getDifferentIndividuals(ind);
                } catch (Throwable e) {
                    exception = e;
                }
                return null;
            }
        };
        NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
                .get(key, ind, checker);
        if (checker.hasThrownException()) {
            Throwable e = checker.thrownException();
            if (e instanceof ReasonerInterruptedException) {
                throw (ReasonerInterruptedException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof TimeOutException) {
                throw (TimeOutException) e;
            }
            if (e instanceof FreshEntitiesException) {
                throw (FreshEntitiesException) e;
            }
            if (e instanceof InconsistentOntologyException) {
                throw (InconsistentOntologyException) e;
            }
            if (e instanceof ClassExpressionNotInProfileException) {
                throw (ClassExpressionNotInProfileException) e;
            }
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    @Override
    public long getTimeOut() {
        return delegate.getTimeOut();
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        return delegate.getFreshEntityPolicy();
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        return delegate.getIndividualNodeSetPolicy();
    }

    @Override
    public void dispose() {
        cache.clear();
        delegate.dispose();
    }

    @Override
    public void ontologiesChanged(List<? extends OWLOntologyChange<?>> changes) {
        // only invalidate the caches, the changes are supposed to go to the
        // reasoner via its own listener
        cache.clear();
    }
}
