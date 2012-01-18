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
import org.semanticweb.owlapi.model.OWLException;
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

@SuppressWarnings("unchecked")
public final class CachedOWLReasoner implements OWLReasoner, OWLOntologyChangeListener {
	protected final OWLReasoner delegate;

	private final class Entailer extends ComputableAllThrowables<Object> {
		private final OWLClassExpression classExpression;

		public Entailer(OWLClassExpression classExpression) {
			this.classExpression = classExpression;
		}

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

	public CachedOWLReasoner(OWLReasoner reasoner, OWLOntologyManager manager) {
		if (reasoner == null) {
			throw new IllegalArgumentException("The input reasoner cannot be null");
		}
		delegate = reasoner;
		manager.addOntologyChangeListener(this);
		rootOntology = delegate.getRootOntology();
	}

	private enum CacheKeys {
		isEntailed, subclasses, subclassesDirect, superclassesDirect, superclasses, equivclasses, disjointclasses, subobjectpropertiesDirect, subobjectproperties, superobjectpropertiesDirect, superobjectproperties, equivobjectproperties, disjointobjectproperties, inverseobjectproperties, objectpropertiesdomainsDirect, objectpropertiesdomains, objectpropertiesranges, objectpropertiesrangesDirect, diffindividual, datapropertiesdomains, datapropertiesdomainsDirect, datapropertiesvalues, disjointdataproperties, equivdataproperties, instances, instancesDirect, objectpropertiesvalues, sameindividual, subdataproperties, subdatapropertiesDirect, superdataproperties, superdatapropertiesDirect, types, typesDirect, issatisfiable
	}

	public String getReasonerName() {
		return delegate.getReasonerName();
	}

	public Version getReasonerVersion() {
		return delegate.getReasonerVersion();
	}

	public BufferingMode getBufferingMode() {
		return delegate.getBufferingMode();
	}

	public void flush() {
		cache.clear();
		delegate.flush();
	}

	public List<OWLOntologyChange> getPendingChanges() {
		return delegate.getPendingChanges();
	}

	public Set<OWLAxiom> getPendingAxiomAdditions() {
		return delegate.getPendingAxiomAdditions();
	}

	public Set<OWLAxiom> getPendingAxiomRemovals() {
		return delegate.getPendingAxiomRemovals();
	}

	public OWLOntology getRootOntology() {
		return rootOntology;
	}

	public void interrupt() {
		cache.clear();
		delegate.interrupt();
	}

	public void precomputeInferences(InferenceType... inferenceTypes)
			throws ReasonerInterruptedException, TimeOutException,
			InconsistentOntologyException {
		cache.clear();
		delegate.precomputeInferences(inferenceTypes);
	}

	public boolean isPrecomputed(InferenceType inferenceType) {
		return delegate.isPrecomputed(inferenceType);
	}

	public Set<InferenceType> getPrecomputableInferenceTypes() {
		return delegate.getPrecomputableInferenceTypes();
	}

	public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
		return delegate.isConsistent();
	}

	public boolean isSatisfiable(final OWLClassExpression classExpression)
			throws ReasonerInterruptedException, TimeOutException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		ComputableAllThrowables<Object> entailer = new Entailer(classExpression);
		Boolean b = cache.get(CacheKeys.issatisfiable, classExpression, entailer);
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
		return b;
	}

	public Node<OWLClass> getUnsatisfiableClasses() throws ReasonerInterruptedException,
			TimeOutException, InconsistentOntologyException {
		return delegate.getUnsatisfiableClasses();
	}

	public boolean isEntailed(final OWLAxiom axiom) throws ReasonerInterruptedException,
			UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		ComputableAllThrowables<Object> entailer = new ComputableAllThrowables<Object>() {
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
		return b;
	}

	public boolean isEntailed(Set<? extends OWLAxiom> axioms)
			throws ReasonerInterruptedException, UnsupportedEntailmentTypeException,
			TimeOutException, AxiomNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		for (OWLAxiom ax : axioms) {
			if (!isEntailed(ax)) {
				return false;
			}
		}
		return true;
	}

	public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
		return delegate.isEntailmentCheckingSupported(axiomType);
	}

	public Node<OWLClass> getTopClassNode() {
		return delegate.getTopClassNode();
	}

	public Node<OWLClass> getBottomClassNode() {
		return delegate.getBottomClassNode();
	}

	public NodeSet<OWLClass> getSubClasses(final OWLClassExpression ce,
			final boolean direct) throws ReasonerInterruptedException, TimeOutException,
			FreshEntitiesException, InconsistentOntologyException,
			ClassExpressionNotInProfileException {
		final CacheKeys key = direct ? CacheKeys.subclassesDirect : CacheKeys.subclasses;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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

	public NodeSet<OWLClass> getSuperClasses(final OWLClassExpression ce,
			final boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = direct ? CacheKeys.superclassesDirect
				: CacheKeys.superclasses;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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
			throw new RuntimeException("Failure asking for superclasses of " + ce + " "
					+ direct, e);
		}
		return toReturn;
	}

	public Node<OWLClass> getEquivalentClasses(final OWLClassExpression ce)
			throws InconsistentOntologyException, ClassExpressionNotInProfileException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = CacheKeys.equivclasses;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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

	public NodeSet<OWLClass> getDisjointClasses(final OWLClassExpression ce)
			throws ReasonerInterruptedException, TimeOutException,
			FreshEntitiesException, InconsistentOntologyException {
		final CacheKeys key = CacheKeys.disjointclasses;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
			public Object compute() {
				try {
					return delegate.getDisjointClasses(ce);
				} catch (Throwable e) {
					exception = e;
				}
				return null;
			}
		};
		NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key, ce, checker);
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

	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
		return delegate.getTopObjectPropertyNode();
	}

	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
		return delegate.getBottomObjectPropertyNode();
	}

	public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
			final OWLObjectPropertyExpression pe, final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = direct ? CacheKeys.subobjectpropertiesDirect
				: CacheKeys.subobjectproperties;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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

	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
			final OWLObjectPropertyExpression pe, final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = direct ? CacheKeys.superobjectpropertiesDirect
				: CacheKeys.superobjectproperties;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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

	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
			final OWLObjectPropertyExpression pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = CacheKeys.equivobjectproperties;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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

	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
			final OWLObjectPropertyExpression pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = CacheKeys.disjointobjectproperties;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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

	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
			final OWLObjectPropertyExpression pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = CacheKeys.inverseobjectproperties;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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

	public NodeSet<OWLClass> getObjectPropertyDomains(
			final OWLObjectPropertyExpression pe, final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = direct ? CacheKeys.objectpropertiesdomainsDirect
				: CacheKeys.objectpropertiesdomains;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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

	public NodeSet<OWLClass> getObjectPropertyRanges(
			final OWLObjectPropertyExpression pe, final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = direct ? CacheKeys.objectpropertiesrangesDirect
				: CacheKeys.objectpropertiesranges;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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

	public Node<OWLDataProperty> getTopDataPropertyNode() {
		return delegate.getTopDataPropertyNode();
	}

	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		return delegate.getBottomDataPropertyNode();
	}

	public NodeSet<OWLDataProperty> getSubDataProperties(final OWLDataProperty pe,
			final boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = direct ? CacheKeys.subdatapropertiesDirect
				: CacheKeys.subdataproperties;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
			public Object compute() {
				try {
					return delegate.getSubDataProperties(pe, direct);
				} catch (Throwable e) {
					exception = e;
				}
				return null;
			}
		};
		NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache.get(key,
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

	public NodeSet<OWLDataProperty> getSuperDataProperties(final OWLDataProperty pe,
			final boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = direct ? CacheKeys.superdatapropertiesDirect
				: CacheKeys.superdataproperties;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
			public Object compute() {
				try {
					return delegate.getSuperDataProperties(pe, direct);
				} catch (Throwable e) {
					exception = e;
				}
				return null;
			}
		};
		NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache.get(key,
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

	public Node<OWLDataProperty> getEquivalentDataProperties(final OWLDataProperty pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = CacheKeys.equivdataproperties;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
			public Object compute() {
				try {
					return delegate.getEquivalentDataProperties(pe);
				} catch (Throwable e) {
					exception = e;
				}
				return null;
			}
		};
		Node<OWLDataProperty> toReturn = (Node<OWLDataProperty>) cache.get(key, pe,
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

	public NodeSet<OWLDataProperty> getDisjointDataProperties(
			final OWLDataPropertyExpression pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = CacheKeys.disjointdataproperties;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
			public Object compute() {
				try {
					return delegate.getDisjointDataProperties(pe);
				} catch (Throwable e) {
					exception = e;
				}
				return null;
			}
		};
		NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache.get(key, pe,
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

	public NodeSet<OWLClass> getDataPropertyDomains(final OWLDataProperty pe,
			final boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = direct ? CacheKeys.datapropertiesdomainsDirect
				: CacheKeys.datapropertiesdomains;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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

	public NodeSet<OWLClass> getTypes(final OWLNamedIndividual ind, final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = direct ? CacheKeys.typesDirect : CacheKeys.types;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
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

	public NodeSet<OWLNamedIndividual> getInstances(final OWLClassExpression ce,
			final boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = direct ? CacheKeys.instancesDirect : CacheKeys.instances;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
			public Object compute() {
				try {
					return delegate.getInstances(ce, direct);
				} catch (Throwable e) {
					exception = e;
				}
				return null;
			}
		};
		NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache.get(
				key, CachedReasoner.makeKey(ce, direct), checker);
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

	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
			final OWLNamedIndividual ind, final OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = CacheKeys.objectpropertiesvalues;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
			public Object compute() {
				try {
					return delegate.getObjectPropertyValues(ind, pe);
				} catch (Throwable e) {
					exception = e;
				}
				return null;
			}
		};
		NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache.get(
				key, new RegKey(ind, pe), checker);
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

	public Set<OWLLiteral> getDataPropertyValues(final OWLNamedIndividual ind,
			final OWLDataProperty pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = CacheKeys.datapropertiesvalues;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
			public Object compute() {
				try {
					return delegate.getDataPropertyValues(ind, pe);
				} catch (Throwable e) {
					exception = e;
				}
				return null;
			}
		};
		Set<OWLLiteral> toReturn = (Set<OWLLiteral>) cache.get(key, new RegKey(ind, pe),
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

	public Node<OWLNamedIndividual> getSameIndividuals(final OWLNamedIndividual ind)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = CacheKeys.sameindividual;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
			public Object compute() {
				try {
					return delegate.getSameIndividuals(ind);
				} catch (Throwable e) {
					exception = e;
				}
				return null;
			}
		};
		Node<OWLNamedIndividual> toReturn = (Node<OWLNamedIndividual>) cache.get(key,
				ind, checker);
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

	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
			final OWLNamedIndividual ind) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		final CacheKeys key = CacheKeys.diffindividual;
		Computable<Object> checker = new ComputableAllThrowables<Object>() {
			public Object compute() {
				try {
					return delegate.getDifferentIndividuals(ind);
				} catch (Throwable e) {
					exception = e;
				}
				return null;
			}
		};
		NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache.get(
				key, ind, checker);
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

	public long getTimeOut() {
		return delegate.getTimeOut();
	}

	public FreshEntityPolicy getFreshEntityPolicy() {
		return delegate.getFreshEntityPolicy();
	}

	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
		return delegate.getIndividualNodeSetPolicy();
	}

	public void dispose() {
		cache.clear();
		delegate.dispose();
	}

	public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
			throws OWLException {
		//only invalidate the caches, the changes are supposed to go to the reasoner via its own listener
		cache.clear();
	}
}



//
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import org.semanticweb.owlapi.apibinding.configurables.Computable;
//import org.semanticweb.owlapi.apibinding.configurables.ComputableAllThrowables;
//import org.semanticweb.owlapi.apibinding.configurables.MemoizingCache;
//import org.semanticweb.owlapi.model.AxiomType;
//import org.semanticweb.owlapi.model.OWLAxiom;
//import org.semanticweb.owlapi.model.OWLClass;
//import org.semanticweb.owlapi.model.OWLClassExpression;
//import org.semanticweb.owlapi.model.OWLDataProperty;
//import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
//import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
//import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
//import org.semanticweb.owlapi.model.OWLException;
//import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
//import org.semanticweb.owlapi.model.OWLLiteral;
//import org.semanticweb.owlapi.model.OWLLogicalAxiom;
//import org.semanticweb.owlapi.model.OWLNamedIndividual;
//import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
//import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
//import org.semanticweb.owlapi.model.OWLOntology;
//import org.semanticweb.owlapi.model.OWLOntologyChange;
//import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
//import org.semanticweb.owlapi.model.OWLOntologyManager;
//import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
//import org.semanticweb.owlapi.reasoner.AxiomNotInProfileException;
//import org.semanticweb.owlapi.reasoner.BufferingMode;
//import org.semanticweb.owlapi.reasoner.ClassExpressionNotInProfileException;
//import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
//import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
//import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
//import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
//import org.semanticweb.owlapi.reasoner.InferenceType;
//import org.semanticweb.owlapi.reasoner.Node;
//import org.semanticweb.owlapi.reasoner.NodeSet;
//import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
//import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
//import org.semanticweb.owlapi.reasoner.TimeOutException;
//import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
//import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;
//import org.semanticweb.owlapi.util.Version;
//@SuppressWarnings("unchecked")
//public final class CachedOWLReasoner implements OWLReasoner,
//		OWLOntologyChangeListener {
//	private final OWLReasoner delegate;
//
//	private final class OWLClassExpressionHarvester extends
//			OWLAxiomVisitorAdapter {
//		Set<OWLClassExpression> collector = new HashSet<OWLClassExpression>();
//
//		public void visit(OWLHasKeyAxiom axiom) {
//			collector.add(axiom.getClassExpression());
//		}
//
//		public void visit(OWLEquivalentClassesAxiom axiom) {
//			collector.addAll(axiom.getClassExpressions());
//		}
//
//		public void visit(OWLObjectPropertyDomainAxiom axiom) {
//			collector.add(axiom.getDomain());
//		}
//
//		public void visit(OWLDataPropertyDomainAxiom axiom) {
//			collector.add(axiom.getDomain());
//		}
//
//		public void visit(OWLSubClassOfAxiom axiom) {
//			collector.add(axiom.getSubClass());
//			collector.add(axiom.getSuperClass());
//		}
//	}
//
//	private static final class BoolKey {
//		Object o;
//		boolean b;
//
//		@Override
//		public int hashCode() {
//			return o.hashCode() * (b ? 1 : -1);
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			BoolKey bk = (BoolKey) obj;
//			return b == bk.b && o.equals(bk.o);
//		}
//	}
//
//	private static final class RegKey {
//		Object o1;
//		Object o2;
//
//		@Override
//		public int hashCode() {
//			return o1.hashCode() * o2.hashCode();
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			RegKey bk = (RegKey) obj;
//			return o1.equals(bk.o1) && o2.equals(bk.o2);
//		}
//	}
//
//	private final static class CachedReasoner {
//		public static Object makeKey(Object o1, Object o2) {
//			RegKey r = new RegKey();
//			r.o1 = o1;
//			r.o2 = o2;
//			return r;
//		}
//
//		public static Object makeKey(Object o1, boolean o2) {
//			BoolKey r = new BoolKey();
//			r.o = o1;
//			r.b = o2;
//			return r;
//		}
//
//		public CachedReasoner() {
//		}
//
//		MemoizingCache<CacheKeys, MemoizingCache<Object, Object>> mainCache = new MemoizingCache<CacheKeys, MemoizingCache<Object, Object>>();
//
//		public void clear() {
//			mainCache.clear();
//		}
//
//		@SuppressWarnings("unchecked")
//		public <T> T get(CacheKeys cachekey, Object key, Computable<Object> c) {
//			Computable<MemoizingCache<Object, Object>> cacheinit = new ComputableAllThrowables<MemoizingCache<Object, Object>>() {
//				public MemoizingCache<Object, Object> compute() {
//					try {
//						return new MemoizingCache<Object, Object>();
//					} catch (Throwable e) {
//						exception = e;
//						return null;
//					}
//				}
//			};
//			T t = (T) mainCache.get(cacheinit, cachekey).get(c, key);
//			if (cacheinit.hasThrownException()) {
//				if (cacheinit.thrownException() instanceof Error) {
//					throw (Error) cacheinit.thrownException();
//				}
//				throw (RuntimeException) cacheinit.thrownException();
//			}
//			return t;
//		}
//	}
//
//	protected final CachedReasoner cache = new CachedReasoner();
//	private final OWLOntology rootOntology;
//
//	public CachedOWLReasoner(OWLReasoner reasoner, OWLOntologyManager manager) {
//		if (reasoner == null) {
//			throw new IllegalArgumentException(
//					"The input reasoner cannot be null");
//		}
//		delegate = reasoner;
//		manager.addOntologyChangeListener(this);
//		rootOntology = delegate.getRootOntology();
//	}
//
//	protected final int concurrentNumber = 1;
//	protected final OWLReasoner[] concurrentDelegates = new OWLReasoner[concurrentNumber];
//
//	public CachedOWLReasoner(OWLReasoner reasoner, OWLOntologyManager manager,
//			OWLReasonerFactory factory) {
//		this(reasoner, manager);
//		for (int i = 0; i < concurrentNumber; i++) {
//			concurrentDelegates[i] = new LittleReasonerHelper(
//					factory.createReasoner(rootOntology));
//		}
//	}
//
//	protected static final void prefetch(List<? extends OWLClassExpression> l,
//			OWLReasoner r) {
//		r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
//		for (int i = 0; i < l.size(); i++) {
//			r.getEquivalentClasses(l.get(i));
//			r.getSuperClasses(l.get(i), true);
//			r.getSuperClasses(l.get(i), false);
//			r.getSubClasses(l.get(i), true);
//			r.getSubClasses(l.get(i), false);
//		}
//	}
//
//	private ExecutorService exec = Executors.newCachedThreadPool();
//
//	private enum CacheKeys {
//		isEntailed, subclasses, subclassesDirect, superclassesDirect, superclasses, equivclasses, disjointclasses, subobjectpropertiesDirect, subobjectproperties, superobjectpropertiesDirect, superobjectproperties, equivobjectproperties, disjointobjectproperties, inverseobjectproperties, objectpropertiesdomainsDirect, objectpropertiesdomains, objectpropertiesranges, objectpropertiesrangesDirect, diffindividual, datapropertiesdomains, datapropertiesdomainsDirect, datapropertiesvalues, disjointdataproperties, equivdataproperties, instances, instancesDirect, objectpropertiesvalues, sameindividual, subdataproperties, subdatapropertiesDirect, superdataproperties, superdatapropertiesDirect, types, typesDirect, issatisfiable
//	}
//
//	private class LittleReasonerHelper implements OWLReasoner {
//		final OWLReasoner littleDelegate;
//
//		LittleReasonerHelper(OWLReasoner r) {
//			littleDelegate = r;
//		}
//
//		// these methods are here only for completeness, not likely they will ever be used or needed
//		public String getReasonerName() {
//			return littleDelegate.getReasonerName();
//		}
//
//		public Version getReasonerVersion() {
//			return littleDelegate.getReasonerVersion();
//		}
//
//		public BufferingMode getBufferingMode() {
//			return littleDelegate.getBufferingMode();
//		}
//
//		public void flush() {
//			littleDelegate.flush();
//		}
//
//		public List<OWLOntologyChange> getPendingChanges() {
//			return littleDelegate.getPendingChanges();
//		}
//
//		public Set<OWLAxiom> getPendingAxiomAdditions() {
//			return littleDelegate.getPendingAxiomAdditions();
//		}
//
//		public Set<OWLAxiom> getPendingAxiomRemovals() {
//			return littleDelegate.getPendingAxiomRemovals();
//		}
//
//		public OWLOntology getRootOntology() {
//			// these helpers do not have a different root ontology
//			return rootOntology;
//		}
//
//		public void interrupt() {
//			littleDelegate.interrupt();
//		}
//
//		public void precomputeInferences(InferenceType... inferenceTypes)
//				throws ReasonerInterruptedException, TimeOutException,
//				InconsistentOntologyException {
//			// these helpers do not usuallpy precompute inferences...
//			//cache.clear();
//			littleDelegate.precomputeInferences(inferenceTypes);
//		}
//
//		public boolean isPrecomputed(InferenceType inferenceType) {
//			return littleDelegate.isPrecomputed(inferenceType);
//		}
//
//		public Set<InferenceType> getPrecomputableInferenceTypes() {
//			return littleDelegate.getPrecomputableInferenceTypes();
//		}
//
//		public boolean isConsistent() throws ReasonerInterruptedException,
//				TimeOutException {
//			return littleDelegate.isConsistent();
//		}
//
//		public boolean isSatisfiable(final OWLClassExpression classExpression)
//				throws ReasonerInterruptedException, TimeOutException,
//				ClassExpressionNotInProfileException, FreshEntitiesException,
//				InconsistentOntologyException {
//			ComputableAllThrowables<Object> entailer = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.isSatisfiable(classExpression);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			// notice: the cache is the same as the main cache
//			Boolean b = cache.get(CacheKeys.issatisfiable, classExpression,
//					entailer);
//			if (entailer.hasThrownException()) {
//				Throwable e = entailer.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof AxiomNotInProfileException) {
//					throw (AxiomNotInProfileException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return b;
//			//		return this.delegate.isSatisfiable(classExpression);
//		}
//
//		public Node<OWLClass> getUnsatisfiableClasses()
//				throws ReasonerInterruptedException, TimeOutException,
//				InconsistentOntologyException {
//			return littleDelegate.getUnsatisfiableClasses();
//		}
//
//		public boolean isEntailed(final OWLAxiom axiom)
//				throws ReasonerInterruptedException,
//				UnsupportedEntailmentTypeException, TimeOutException,
//				AxiomNotInProfileException, FreshEntitiesException,
//				InconsistentOntologyException {
//			ComputableAllThrowables<Object> entailer = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.isEntailed(axiom);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			Boolean b = cache.get(CacheKeys.isEntailed, axiom, entailer);
//			if (entailer.hasThrownException()) {
//				Throwable e = entailer.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof UnsupportedEntailmentTypeException) {
//					throw (UnsupportedEntailmentTypeException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof AxiomNotInProfileException) {
//					throw (AxiomNotInProfileException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return b;
//			//return this.delegate.isEntailed(axiom);
//		}
//
//		public boolean isEntailed(Set<? extends OWLAxiom> axioms)
//				throws ReasonerInterruptedException,
//				UnsupportedEntailmentTypeException, TimeOutException,
//				AxiomNotInProfileException, FreshEntitiesException,
//				InconsistentOntologyException {
//			for (OWLAxiom ax : axioms) {
//				if (!isEntailed(ax)) {
//					return false;
//				}
//			}
//			return true;
//			//		return this.delegate.isEntailed(axioms);
//		}
//
//		public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
//			return littleDelegate.isEntailmentCheckingSupported(axiomType);
//		}
//
//		public Node<OWLClass> getTopClassNode() {
//			return littleDelegate.getTopClassNode();
//		}
//
//		public Node<OWLClass> getBottomClassNode() {
//			return littleDelegate.getBottomClassNode();
//		}
//
//
//		public NodeSet<OWLClass> getSubClasses(final OWLClassExpression ce,
//				final boolean direct) throws ReasonerInterruptedException,
//				TimeOutException, FreshEntitiesException,
//				InconsistentOntologyException,
//				ClassExpressionNotInProfileException {
//			final CacheKeys key = direct ? CacheKeys.subclassesDirect
//					: CacheKeys.subclasses;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getSubClasses(ce, direct);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//					CachedReasoner.makeKey(ce, direct), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof UnsupportedEntailmentTypeException) {
//					throw (UnsupportedEntailmentTypeException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof AxiomNotInProfileException) {
//					throw (AxiomNotInProfileException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//		return this.delegate.getSubClasses(ce, direct);
//		}
//
//		public NodeSet<OWLClass> getSuperClasses(final OWLClassExpression ce,
//				final boolean direct) throws InconsistentOntologyException,
//				ClassExpressionNotInProfileException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = direct ? CacheKeys.superclassesDirect
//					: CacheKeys.superclasses;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getSuperClasses(ce, direct);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//					CachedReasoner.makeKey(ce, direct), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof AxiomNotInProfileException) {
//					throw (AxiomNotInProfileException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(
//						"Failure asking for superclasses of " + ce + " "
//								+ direct, e);
//			}
//			return toReturn;
//			//return this.delegate.getSuperClasses(ce, direct);
//		}
//
//		public Node<OWLClass> getEquivalentClasses(final OWLClassExpression ce)
//				throws InconsistentOntologyException,
//				ClassExpressionNotInProfileException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = CacheKeys.equivclasses;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getEquivalentClasses(ce);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			Node<OWLClass> toReturn = (Node<OWLClass>) cache.get(key, ce,
//					checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//return this.delegate.getEquivalentClasses(ce);
//		}
//
//		public NodeSet<OWLClass> getDisjointClasses(final OWLClassExpression ce)
//				throws ReasonerInterruptedException, TimeOutException,
//				FreshEntitiesException, InconsistentOntologyException {
//			final CacheKeys key = CacheKeys.disjointclasses;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getDisjointClasses(ce);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key, ce,
//					checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//		return this.delegate.getDisjointClasses(ce);
//		}
//
//		public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
//			return littleDelegate.getTopObjectPropertyNode();
//		}
//
//		public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
//			return littleDelegate.getBottomObjectPropertyNode();
//		}
//
//		public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
//				final OWLObjectPropertyExpression pe, final boolean direct)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = direct ? CacheKeys.subobjectpropertiesDirect
//					: CacheKeys.subobjectproperties;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate
//								.getSubObjectProperties(pe, direct);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
//					.get(key, CachedReasoner.makeKey(pe, direct), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//return this.delegate.getSubObjectProperties(pe, direct);
//		}
//
//		public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
//				final OWLObjectPropertyExpression pe, final boolean direct)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = direct ? CacheKeys.superobjectpropertiesDirect
//					: CacheKeys.superobjectproperties;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getSuperObjectProperties(pe,
//								direct);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
//					.get(key, CachedReasoner.makeKey(pe, direct), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//		return this.delegate.getSuperObjectProperties(pe, direct);
//		}
//
//		public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
//				final OWLObjectPropertyExpression pe)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = CacheKeys.equivobjectproperties;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getEquivalentObjectProperties(pe);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			Node<OWLObjectPropertyExpression> toReturn = (Node<OWLObjectPropertyExpression>) cache
//					.get(key, pe, checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//		return this.delegate.getEquivalentObjectProperties(pe);
//		}
//
//		public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
//				final OWLObjectPropertyExpression pe)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = CacheKeys.disjointobjectproperties;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getDisjointObjectProperties(pe);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
//					.get(key, pe, checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//return this.delegate.getDisjointObjectProperties(pe);
//		}
//
//		public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
//				final OWLObjectPropertyExpression pe)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = CacheKeys.inverseobjectproperties;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getInverseObjectProperties(pe);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			Node<OWLObjectPropertyExpression> toReturn = (Node<OWLObjectPropertyExpression>) cache
//					.get(key, pe, checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//return this.delegate.getInverseObjectProperties(pe);
//		}
//
//		public NodeSet<OWLClass> getObjectPropertyDomains(
//				final OWLObjectPropertyExpression pe, final boolean direct)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = direct ? CacheKeys.objectpropertiesdomainsDirect
//					: CacheKeys.objectpropertiesdomains;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getObjectPropertyDomains(pe,
//								direct);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//					CachedReasoner.makeKey(pe, direct), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//return this.delegate.getObjectPropertyDomains(pe, direct);
//		}
//
//		public NodeSet<OWLClass> getObjectPropertyRanges(
//				final OWLObjectPropertyExpression pe, final boolean direct)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = direct ? CacheKeys.objectpropertiesrangesDirect
//					: CacheKeys.objectpropertiesranges;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getObjectPropertyRanges(pe,
//								direct);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//					CachedReasoner.makeKey(pe, direct), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//return this.delegate.getObjectPropertyRanges(pe, direct);
//		}
//
//		public Node<OWLDataProperty> getTopDataPropertyNode() {
//			return littleDelegate.getTopDataPropertyNode();
//		}
//
//		public Node<OWLDataProperty> getBottomDataPropertyNode() {
//			return littleDelegate.getBottomDataPropertyNode();
//		}
//
//		public NodeSet<OWLDataProperty> getSubDataProperties(
//				final OWLDataProperty pe, final boolean direct)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = direct ? CacheKeys.subdatapropertiesDirect
//					: CacheKeys.subdataproperties;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getSubDataProperties(pe, direct);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
//					.get(key, CachedReasoner.makeKey(pe, direct), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//		return this.delegate.getSubDataProperties(pe, direct);
//		}
//
//		public NodeSet<OWLDataProperty> getSuperDataProperties(
//				final OWLDataProperty pe, final boolean direct)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = direct ? CacheKeys.superdatapropertiesDirect
//					: CacheKeys.superdataproperties;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate
//								.getSuperDataProperties(pe, direct);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
//					.get(key, CachedReasoner.makeKey(pe, direct), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//		return this.delegate.getSuperDataProperties(pe, direct);
//		}
//
//		public Node<OWLDataProperty> getEquivalentDataProperties(
//				final OWLDataProperty pe) throws InconsistentOntologyException,
//				FreshEntitiesException, ReasonerInterruptedException,
//				TimeOutException {
//			final CacheKeys key = CacheKeys.equivdataproperties;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getEquivalentDataProperties(pe);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			Node<OWLDataProperty> toReturn = (Node<OWLDataProperty>) cache.get(
//					key, pe, checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//		return this.delegate.getEquivalentDataProperties(pe);
//		}
//
//		public NodeSet<OWLDataProperty> getDisjointDataProperties(
//				final OWLDataPropertyExpression pe)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = CacheKeys.disjointdataproperties;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getDisjointDataProperties(pe);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
//					.get(key, pe, checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//		return this.delegate.getDisjointDataProperties(pe);
//		}
//
//		public NodeSet<OWLClass> getDataPropertyDomains(
//				final OWLDataProperty pe, final boolean direct)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = direct ? CacheKeys.datapropertiesdomainsDirect
//					: CacheKeys.datapropertiesdomains;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate
//								.getDataPropertyDomains(pe, direct);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//					CachedReasoner.makeKey(pe, direct), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//		return this.delegate.getDataPropertyDomains(pe, direct);
//		}
//
//		public NodeSet<OWLClass> getTypes(final OWLNamedIndividual ind,
//				final boolean direct) throws InconsistentOntologyException,
//				FreshEntitiesException, ReasonerInterruptedException,
//				TimeOutException {
//			final CacheKeys key = direct ? CacheKeys.typesDirect
//					: CacheKeys.types;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getTypes(ind, direct);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//					CachedReasoner.makeKey(ind, direct), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//		return this.delegate.getTypes(ind, direct);
//		}
//
//		public NodeSet<OWLNamedIndividual> getInstances(
//				final OWLClassExpression ce, final boolean direct)
//				throws InconsistentOntologyException,
//				ClassExpressionNotInProfileException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = direct ? CacheKeys.instancesDirect
//					: CacheKeys.instances;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getInstances(ce, direct);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
//					.get(key, CachedReasoner.makeKey(ce, direct), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//	return this.delegate.getInstances(ce, direct);
//		}
//
//		public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
//				final OWLNamedIndividual ind,
//				final OWLObjectPropertyExpression pe)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = CacheKeys.objectpropertiesvalues;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getObjectPropertyValues(ind, pe);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
//					.get(key, CachedReasoner.makeKey(ind, pe), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//return this.delegate.getObjectPropertyValues(ind, pe);
//		}
//
//		public Set<OWLLiteral> getDataPropertyValues(
//				final OWLNamedIndividual ind, final OWLDataProperty pe)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = CacheKeys.datapropertiesvalues;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getDataPropertyValues(ind, pe);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			Set<OWLLiteral> toReturn = (Set<OWLLiteral>) cache.get(key,
//					CachedReasoner.makeKey(ind, pe), checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//	return this.delegate.getDataPropertyValues(ind, pe);
//		}
//
//		public Node<OWLNamedIndividual> getSameIndividuals(
//				final OWLNamedIndividual ind)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = CacheKeys.sameindividual;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getSameIndividuals(ind);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			Node<OWLNamedIndividual> toReturn = (Node<OWLNamedIndividual>) cache
//					.get(key, ind, checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//		return this.delegate.getSameIndividuals(ind);
//		}
//
//		public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
//				final OWLNamedIndividual ind)
//				throws InconsistentOntologyException, FreshEntitiesException,
//				ReasonerInterruptedException, TimeOutException {
//			final CacheKeys key = CacheKeys.diffindividual;
//			Computable<Object> checker = new ComputableAllThrowables<Object>() {
//				public Object compute() {
//					try {
//						return littleDelegate.getDifferentIndividuals(ind);
//					} catch (Throwable e) {
//						exception = e;
//					}
//					return null;
//				}
//			};
//			NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
//					.get(key, ind, checker);
//			if (checker.hasThrownException()) {
//				Throwable e = checker.thrownException();
//				if (e instanceof ReasonerInterruptedException) {
//					throw (ReasonerInterruptedException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof TimeOutException) {
//					throw (TimeOutException) e;
//				}
//				if (e instanceof FreshEntitiesException) {
//					throw (FreshEntitiesException) e;
//				}
//				if (e instanceof InconsistentOntologyException) {
//					throw (InconsistentOntologyException) e;
//				}
//				if (e instanceof ClassExpressionNotInProfileException) {
//					throw (ClassExpressionNotInProfileException) e;
//				}
//				if (e instanceof Error) {
//					throw (Error) e;
//				}
//				if (e instanceof RuntimeException) {
//					throw (RuntimeException) e;
//				}
//				throw new RuntimeException(e);
//			}
//			return toReturn;
//			//	return this.delegate.getDifferentIndividuals(ind);
//		}
//
//		public long getTimeOut() {
//			return littleDelegate.getTimeOut();
//		}
//
//		public FreshEntityPolicy getFreshEntityPolicy() {
//			return littleDelegate.getFreshEntityPolicy();
//		}
//
//		public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
//			return littleDelegate.getIndividualNodeSetPolicy();
//		}
//
//		public void dispose() {
//			littleDelegate.dispose();
//		}
//	}
//
//	public String getReasonerName() {
//		return delegate.getReasonerName();
//	}
//
//	public Version getReasonerVersion() {
//		return delegate.getReasonerVersion();
//	}
//
//	public BufferingMode getBufferingMode() {
//		return delegate.getBufferingMode();
//	}
//
//	public void flush() {
//		cache.clear();
//		delegate.flush();
//		for (int i = 0; i < concurrentNumber; i++) {
//			if (concurrentDelegates[i] != null) {
//				concurrentDelegates[i].flush();
//			}
//		}
//	}
//
//	public List<OWLOntologyChange> getPendingChanges() {
//		return delegate.getPendingChanges();
//	}
//
//	public Set<OWLAxiom> getPendingAxiomAdditions() {
//		return delegate.getPendingAxiomAdditions();
//	}
//
//	public Set<OWLAxiom> getPendingAxiomRemovals() {
//		return delegate.getPendingAxiomRemovals();
//	}
//
//	public OWLOntology getRootOntology() {
//		return rootOntology;
//	}
//
//	public void interrupt() {
//		cache.clear();
//		delegate.interrupt();
//		for (int i = 0; i < concurrentNumber; i++) {
//			if (concurrentDelegates[i] != null) {
//				concurrentDelegates[i].interrupt();
//			}
//		}
//	}
//
//	public void precomputeInferences(InferenceType... inferenceTypes)
//			throws ReasonerInterruptedException, TimeOutException,
//			InconsistentOntologyException {
//		cache.clear();
//		delegate.precomputeInferences(inferenceTypes);
//		//TODO need to start threads for these reasoners
//		if (concurrentDelegates[0] != null) {
//			Runnable starter = new Runnable() {
//				public void run() {
//					final List<OWLClassExpression> list = new ArrayList<OWLClassExpression>();
//					OWLClassExpressionHarvester visitor = new OWLClassExpressionHarvester();
//					for (OWLLogicalAxiom ax : rootOntology.getLogicalAxioms()) {
//
//						ax.accept(visitor);
//					}
//					list.addAll(visitor.collector);
//					//							rootOntology.getClassesInSignature());
//					if (list.size() < concurrentNumber || concurrentNumber == 1) {
//						prefetch(list, concurrentDelegates[0]);
//					} else {
//						final int chunk = list.size() / concurrentNumber;
//						for (int index = 0; index < concurrentNumber; index++) {
//							final int i = index;
//							Runnable next = new Runnable() {
//								public void run() {
//									prefetch(
//											list.subList(i * chunk, (i + 1)
//													* chunk),
//											concurrentDelegates[i]);
//								}
//							};
//							exec.execute(next);
//						}
//					}
//				}
//			};
//			exec.execute(starter);
//		}
//	}
//
//	public boolean isPrecomputed(InferenceType inferenceType) {
//		return delegate.isPrecomputed(inferenceType);
//	}
//
//	public Set<InferenceType> getPrecomputableInferenceTypes() {
//		return delegate.getPrecomputableInferenceTypes();
//	}
//
//	public boolean isConsistent() throws ReasonerInterruptedException,
//			TimeOutException {
//		return delegate.isConsistent();
//	}
//
//	public boolean isSatisfiable(final OWLClassExpression classExpression)
//			throws ReasonerInterruptedException, TimeOutException,
//			ClassExpressionNotInProfileException, FreshEntitiesException,
//			InconsistentOntologyException {
//		ComputableAllThrowables<Object> entailer = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.isSatisfiable(classExpression);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		Boolean b = cache.get(CacheKeys.issatisfiable, classExpression,
//				entailer);
//		if (entailer.hasThrownException()) {
//			Throwable e = entailer.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof AxiomNotInProfileException) {
//				throw (AxiomNotInProfileException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return b;
//		//		return this.delegate.isSatisfiable(classExpression);
//	}
//
//	public Node<OWLClass> getUnsatisfiableClasses()
//			throws ReasonerInterruptedException, TimeOutException,
//			InconsistentOntologyException {
//		return delegate.getUnsatisfiableClasses();
//	}
//
//	public boolean isEntailed(final OWLAxiom axiom)
//			throws ReasonerInterruptedException,
//			UnsupportedEntailmentTypeException, TimeOutException,
//			AxiomNotInProfileException, FreshEntitiesException,
//			InconsistentOntologyException {
//		ComputableAllThrowables<Object> entailer = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.isEntailed(axiom);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		Boolean b = cache.get(CacheKeys.isEntailed, axiom, entailer);
//		if (entailer.hasThrownException()) {
//			Throwable e = entailer.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof UnsupportedEntailmentTypeException) {
//				throw (UnsupportedEntailmentTypeException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof AxiomNotInProfileException) {
//				throw (AxiomNotInProfileException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return b;
//		//return this.delegate.isEntailed(axiom);
//	}
//
//	public boolean isEntailed(Set<? extends OWLAxiom> axioms)
//			throws ReasonerInterruptedException,
//			UnsupportedEntailmentTypeException, TimeOutException,
//			AxiomNotInProfileException, FreshEntitiesException,
//			InconsistentOntologyException {
//		for (OWLAxiom ax : axioms) {
//			if (!isEntailed(ax)) {
//				return false;
//			}
//		}
//		return true;
//		//		return this.delegate.isEntailed(axioms);
//	}
//
//	public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
//		return delegate.isEntailmentCheckingSupported(axiomType);
//	}
//
//	public Node<OWLClass> getTopClassNode() {
//		return delegate.getTopClassNode();
//	}
//
//	public Node<OWLClass> getBottomClassNode() {
//		return delegate.getBottomClassNode();
//	}
//
//	public NodeSet<OWLClass> getSubClasses(final OWLClassExpression ce,
//			final boolean direct) throws ReasonerInterruptedException,
//			TimeOutException, FreshEntitiesException,
//			InconsistentOntologyException, ClassExpressionNotInProfileException {
//		final CacheKeys key = direct ? CacheKeys.subclassesDirect
//				: CacheKeys.subclasses;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getSubClasses(ce, direct);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//				CachedReasoner.makeKey(ce, direct), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof UnsupportedEntailmentTypeException) {
//				throw (UnsupportedEntailmentTypeException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof AxiomNotInProfileException) {
//				throw (AxiomNotInProfileException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//		return this.delegate.getSubClasses(ce, direct);
//	}
//
//	public NodeSet<OWLClass> getSuperClasses(final OWLClassExpression ce,
//			final boolean direct) throws InconsistentOntologyException,
//			ClassExpressionNotInProfileException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = direct ? CacheKeys.superclassesDirect
//				: CacheKeys.superclasses;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getSuperClasses(ce, direct);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//				CachedReasoner.makeKey(ce, direct), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof AxiomNotInProfileException) {
//				throw (AxiomNotInProfileException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException("Failure asking for superclasses of "
//					+ ce + " " + direct, e);
//		}
//		return toReturn;
//		//return this.delegate.getSuperClasses(ce, direct);
//	}
//
//	public Node<OWLClass> getEquivalentClasses(final OWLClassExpression ce)
//			throws InconsistentOntologyException,
//			ClassExpressionNotInProfileException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = CacheKeys.equivclasses;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getEquivalentClasses(ce);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		Node<OWLClass> toReturn = (Node<OWLClass>) cache.get(key, ce, checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//return this.delegate.getEquivalentClasses(ce);
//	}
//
//	public NodeSet<OWLClass> getDisjointClasses(final OWLClassExpression ce)
//			throws ReasonerInterruptedException, TimeOutException,
//			FreshEntitiesException, InconsistentOntologyException {
//		final CacheKeys key = CacheKeys.disjointclasses;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getDisjointClasses(ce);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key, ce,
//				checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//		return this.delegate.getDisjointClasses(ce);
//	}
//
//	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
//		return delegate.getTopObjectPropertyNode();
//	}
//
//	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
//		return delegate.getBottomObjectPropertyNode();
//	}
//
//	public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
//			final OWLObjectPropertyExpression pe, final boolean direct)
//			throws InconsistentOntologyException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = direct ? CacheKeys.subobjectpropertiesDirect
//				: CacheKeys.subobjectproperties;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getSubObjectProperties(pe, direct);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
//				.get(key, CachedReasoner.makeKey(pe, direct), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//return this.delegate.getSubObjectProperties(pe, direct);
//	}
//
//	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
//			final OWLObjectPropertyExpression pe, final boolean direct)
//			throws InconsistentOntologyException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = direct ? CacheKeys.superobjectpropertiesDirect
//				: CacheKeys.superobjectproperties;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getSuperObjectProperties(pe, direct);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
//				.get(key, CachedReasoner.makeKey(pe, direct), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//		return this.delegate.getSuperObjectProperties(pe, direct);
//	}
//
//	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
//			final OWLObjectPropertyExpression pe)
//			throws InconsistentOntologyException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = CacheKeys.equivobjectproperties;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getEquivalentObjectProperties(pe);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		Node<OWLObjectPropertyExpression> toReturn = (Node<OWLObjectPropertyExpression>) cache
//				.get(key, pe, checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//		return this.delegate.getEquivalentObjectProperties(pe);
//	}
//
//	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
//			final OWLObjectPropertyExpression pe)
//			throws InconsistentOntologyException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = CacheKeys.disjointobjectproperties;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getDisjointObjectProperties(pe);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLObjectPropertyExpression> toReturn = (NodeSet<OWLObjectPropertyExpression>) cache
//				.get(key, pe, checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//return this.delegate.getDisjointObjectProperties(pe);
//	}
//
//	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
//			final OWLObjectPropertyExpression pe)
//			throws InconsistentOntologyException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = CacheKeys.inverseobjectproperties;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getInverseObjectProperties(pe);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		Node<OWLObjectPropertyExpression> toReturn = (Node<OWLObjectPropertyExpression>) cache
//				.get(key, pe, checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//return this.delegate.getInverseObjectProperties(pe);
//	}
//
//	public NodeSet<OWLClass> getObjectPropertyDomains(
//			final OWLObjectPropertyExpression pe, final boolean direct)
//			throws InconsistentOntologyException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = direct ? CacheKeys.objectpropertiesdomainsDirect
//				: CacheKeys.objectpropertiesdomains;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getObjectPropertyDomains(pe, direct);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//				CachedReasoner.makeKey(pe, direct), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//return this.delegate.getObjectPropertyDomains(pe, direct);
//	}
//
//	public NodeSet<OWLClass> getObjectPropertyRanges(
//			final OWLObjectPropertyExpression pe, final boolean direct)
//			throws InconsistentOntologyException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = direct ? CacheKeys.objectpropertiesrangesDirect
//				: CacheKeys.objectpropertiesranges;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getObjectPropertyRanges(pe, direct);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//				CachedReasoner.makeKey(pe, direct), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//return this.delegate.getObjectPropertyRanges(pe, direct);
//	}
//
//	public Node<OWLDataProperty> getTopDataPropertyNode() {
//		return delegate.getTopDataPropertyNode();
//	}
//
//	public Node<OWLDataProperty> getBottomDataPropertyNode() {
//		return delegate.getBottomDataPropertyNode();
//	}
//
//	public NodeSet<OWLDataProperty> getSubDataProperties(
//			final OWLDataProperty pe, final boolean direct)
//			throws InconsistentOntologyException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = direct ? CacheKeys.subdatapropertiesDirect
//				: CacheKeys.subdataproperties;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getSubDataProperties(pe, direct);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
//				.get(key, CachedReasoner.makeKey(pe, direct), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//		return this.delegate.getSubDataProperties(pe, direct);
//	}
//
//	public NodeSet<OWLDataProperty> getSuperDataProperties(
//			final OWLDataProperty pe, final boolean direct)
//			throws InconsistentOntologyException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = direct ? CacheKeys.superdatapropertiesDirect
//				: CacheKeys.superdataproperties;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getSuperDataProperties(pe, direct);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
//				.get(key, CachedReasoner.makeKey(pe, direct), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//		return this.delegate.getSuperDataProperties(pe, direct);
//	}
//
//	public Node<OWLDataProperty> getEquivalentDataProperties(
//			final OWLDataProperty pe) throws InconsistentOntologyException,
//			FreshEntitiesException, ReasonerInterruptedException,
//			TimeOutException {
//		final CacheKeys key = CacheKeys.equivdataproperties;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getEquivalentDataProperties(pe);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		Node<OWLDataProperty> toReturn = (Node<OWLDataProperty>) cache.get(key,
//				pe, checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//		return this.delegate.getEquivalentDataProperties(pe);
//	}
//
//	public NodeSet<OWLDataProperty> getDisjointDataProperties(
//			final OWLDataPropertyExpression pe)
//			throws InconsistentOntologyException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = CacheKeys.disjointdataproperties;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getDisjointDataProperties(pe);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLDataProperty> toReturn = (NodeSet<OWLDataProperty>) cache
//				.get(key, pe, checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//		return this.delegate.getDisjointDataProperties(pe);
//	}
//
//	public NodeSet<OWLClass> getDataPropertyDomains(final OWLDataProperty pe,
//			final boolean direct) throws InconsistentOntologyException,
//			FreshEntitiesException, ReasonerInterruptedException,
//			TimeOutException {
//		final CacheKeys key = direct ? CacheKeys.datapropertiesdomainsDirect
//				: CacheKeys.datapropertiesdomains;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getDataPropertyDomains(pe, direct);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//				CachedReasoner.makeKey(pe, direct), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//		return this.delegate.getDataPropertyDomains(pe, direct);
//	}
//
//	public NodeSet<OWLClass> getTypes(final OWLNamedIndividual ind,
//			final boolean direct) throws InconsistentOntologyException,
//			FreshEntitiesException, ReasonerInterruptedException,
//			TimeOutException {
//		final CacheKeys key = direct ? CacheKeys.typesDirect : CacheKeys.types;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getTypes(ind, direct);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLClass> toReturn = (NodeSet<OWLClass>) cache.get(key,
//				CachedReasoner.makeKey(ind, direct), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//		return this.delegate.getTypes(ind, direct);
//	}
//
//	public NodeSet<OWLNamedIndividual> getInstances(
//			final OWLClassExpression ce, final boolean direct)
//			throws InconsistentOntologyException,
//			ClassExpressionNotInProfileException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = direct ? CacheKeys.instancesDirect
//				: CacheKeys.instances;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getInstances(ce, direct);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
//				.get(key, CachedReasoner.makeKey(ce, direct), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//	return this.delegate.getInstances(ce, direct);
//	}
//
//	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
//			final OWLNamedIndividual ind, final OWLObjectPropertyExpression pe)
//			throws InconsistentOntologyException, FreshEntitiesException,
//			ReasonerInterruptedException, TimeOutException {
//		final CacheKeys key = CacheKeys.objectpropertiesvalues;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getObjectPropertyValues(ind, pe);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
//				.get(key, CachedReasoner.makeKey(ind, pe), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//return this.delegate.getObjectPropertyValues(ind, pe);
//	}
//
//	public Set<OWLLiteral> getDataPropertyValues(final OWLNamedIndividual ind,
//			final OWLDataProperty pe) throws InconsistentOntologyException,
//			FreshEntitiesException, ReasonerInterruptedException,
//			TimeOutException {
//		final CacheKeys key = CacheKeys.datapropertiesvalues;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getDataPropertyValues(ind, pe);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		Set<OWLLiteral> toReturn = (Set<OWLLiteral>) cache.get(key,
//				CachedReasoner.makeKey(ind, pe), checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//	return this.delegate.getDataPropertyValues(ind, pe);
//	}
//
//	public Node<OWLNamedIndividual> getSameIndividuals(
//			final OWLNamedIndividual ind) throws InconsistentOntologyException,
//			FreshEntitiesException, ReasonerInterruptedException,
//			TimeOutException {
//		final CacheKeys key = CacheKeys.sameindividual;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getSameIndividuals(ind);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		Node<OWLNamedIndividual> toReturn = (Node<OWLNamedIndividual>) cache
//				.get(key, ind, checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//		return this.delegate.getSameIndividuals(ind);
//	}
//
//	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
//			final OWLNamedIndividual ind) throws InconsistentOntologyException,
//			FreshEntitiesException, ReasonerInterruptedException,
//			TimeOutException {
//		final CacheKeys key = CacheKeys.diffindividual;
//		Computable<Object> checker = new ComputableAllThrowables<Object>() {
//			public Object compute() {
//				try {
//					return delegate.getDifferentIndividuals(ind);
//				} catch (Throwable e) {
//					exception = e;
//				}
//				return null;
//			}
//		};
//		NodeSet<OWLNamedIndividual> toReturn = (NodeSet<OWLNamedIndividual>) cache
//				.get(key, ind, checker);
//		if (checker.hasThrownException()) {
//			Throwable e = checker.thrownException();
//			if (e instanceof ReasonerInterruptedException) {
//				throw (ReasonerInterruptedException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof TimeOutException) {
//				throw (TimeOutException) e;
//			}
//			if (e instanceof FreshEntitiesException) {
//				throw (FreshEntitiesException) e;
//			}
//			if (e instanceof InconsistentOntologyException) {
//				throw (InconsistentOntologyException) e;
//			}
//			if (e instanceof ClassExpressionNotInProfileException) {
//				throw (ClassExpressionNotInProfileException) e;
//			}
//			if (e instanceof Error) {
//				throw (Error) e;
//			}
//			if (e instanceof RuntimeException) {
//				throw (RuntimeException) e;
//			}
//			throw new RuntimeException(e);
//		}
//		return toReturn;
//		//	return this.delegate.getDifferentIndividuals(ind);
//	}
//
//	public long getTimeOut() {
//		return delegate.getTimeOut();
//	}
//
//	public FreshEntityPolicy getFreshEntityPolicy() {
//		return delegate.getFreshEntityPolicy();
//	}
//
//	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
//		return delegate.getIndividualNodeSetPolicy();
//	}
//
//	public void dispose() {
//		cache.clear();
//		delegate.dispose();
//		for (int i = 0; i < concurrentNumber; i++) {
//			if (concurrentDelegates[i] != null) {
//				concurrentDelegates[i].dispose();
//			}
//		}
//	}
//
//	public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
//			throws OWLException {
//		//only invalidate the caches, the changes are supposed to go to the reasoner via its own listener
//		cache.clear();
//	}
//}
