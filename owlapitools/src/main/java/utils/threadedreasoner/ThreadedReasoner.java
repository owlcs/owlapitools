/*
 * Date: Jan 13, 2012
 *
 * code made available under Mozilla Public License (http://www.mozilla.org/MPL/MPL-1.1.html)
 *
 * copyright 2012, Ignazio Palmisano, University of Manchester
 *
 */
package utils.threadedreasoner;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.semanticweb.owlapi.util.Version;

/**
 *
 * @author ignazio
 *
 *         Reasoner wrapper that will spin each call to the underlying reasoner
 *         on an Executor service and try hard to respect timeouts
 *
 */
public class ThreadedReasoner implements OWLReasoner {
	protected final OWLReasoner delegate;
	private final ExecutorService exec = Executors.newFixedThreadPool(1);

	public ThreadedReasoner(OWLReasoner r) {
		delegate = r;
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
		return delegate.getRootOntology();
	}

	public boolean isPrecomputed(InferenceType inferenceType) {
		return delegate.isPrecomputed(inferenceType);
	}

	public Set<InferenceType> getPrecomputableInferenceTypes() {
		return delegate.getPrecomputableInferenceTypes();
	}

	public Node<OWLClass> getTopClassNode() {
		return delegate.getTopClassNode();
	}

	public Node<OWLClass> getBottomClassNode() {
		return delegate.getBottomClassNode();
	}

	public void interrupt() {
		delegate.interrupt();
		exec.shutdownNow();
	}

	public void precomputeInferences(final InferenceType... inferenceTypes)
			throws ReasonerInterruptedException, TimeOutException,
			InconsistentOntologyException {
		Callable<Boolean> thread = new Callable<Boolean>() {
			public Boolean call() throws Exception {
				delegate.precomputeInferences(inferenceTypes);
				return Boolean.TRUE;
			}
		};
		threadedRun(thread);
	}

	private <T> T threadedRun(Callable<T> r) {
		Future<T> toReturn = exec.submit(r);
		try {
			if (delegate.getTimeOut() > 0) {
				return toReturn.get(delegate.getTimeOut(), TimeUnit.MILLISECONDS);
			} else {
				return toReturn.get();
			}
		} catch (InterruptedException e) {
			//e.printStackTrace();
			interrupt();
			exec.shutdownNow();
			throw new ReasonerInterruptedException(
					"Reasoning was interrupted; future reasoning tasks might be affected",
					e);
		} catch (ExecutionException e) {
			//e.printStackTrace();
			interrupt();
			exec.shutdownNow();
			throw new ReasonerInternalException(
					"Execution problem; future reasoning tasks might be affected", e);
		} catch (TimeoutException e) {
			//e.printStackTrace();
			interrupt();
			exec.shutdownNow();
			throw new TimeOutException(
					"Timeout occurred; future reasoning tasks might be affected", e);
		}
		finally {
			toReturn.cancel(true);
		}
	}

	public boolean isConsistent() throws ReasonerInterruptedException, TimeOutException {
		return threadedRun(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return delegate.isConsistent();
			}
		});
	}

	public boolean isSatisfiable(final OWLClassExpression classExpression)
			throws ReasonerInterruptedException, TimeOutException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		return threadedRun(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return delegate.isSatisfiable(classExpression);
			}
		});
	}

	public Node<OWLClass> getUnsatisfiableClasses() throws ReasonerInterruptedException,
			TimeOutException, InconsistentOntologyException {
		Callable<Node<OWLClass>> thread = new Callable<Node<OWLClass>>() {
			public Node<OWLClass> call() throws Exception {
				return delegate.getUnsatisfiableClasses();
			}
		};
		return threadedRun(thread);
	}

	public boolean isEntailed(final OWLAxiom axiom) throws ReasonerInterruptedException,
			UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		Callable<Boolean> thread = new Callable<Boolean>() {
			public Boolean call() {
				return delegate.isEntailed(axiom);
			}
		};
		return threadedRun(thread);
	}

	public boolean isEntailed(final Set<? extends OWLAxiom> axioms)
			throws ReasonerInterruptedException, UnsupportedEntailmentTypeException,
			TimeOutException, AxiomNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		Callable<Boolean> thread = new Callable<Boolean>() {
			public Boolean call() {
				return delegate.isEntailed(axioms);
			}
		};
		return threadedRun(thread);
	}

	public boolean isEntailmentCheckingSupported(final AxiomType<?> axiomType) {
		Callable<Boolean> thread = new Callable<Boolean>() {
			public Boolean call() {
				return delegate.isEntailmentCheckingSupported(axiomType);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLClass> getSubClasses(final OWLClassExpression ce,
			final boolean direct) throws ReasonerInterruptedException, TimeOutException,
			FreshEntitiesException, InconsistentOntologyException,
			ClassExpressionNotInProfileException {
		Callable<NodeSet<OWLClass>> thread = new Callable<NodeSet<OWLClass>>() {
			public NodeSet<OWLClass> call() {
				return delegate.getSubClasses(ce, direct);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLClass> getSuperClasses(final OWLClassExpression ce,
			final boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLClass>> thread = new Callable<NodeSet<OWLClass>>() {
			public NodeSet<OWLClass> call() {
				return delegate.getSuperClasses(ce, direct);
			}
		};
		return threadedRun(thread);
	}

	public Node<OWLClass> getEquivalentClasses(final OWLClassExpression ce)
			throws InconsistentOntologyException, ClassExpressionNotInProfileException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		Callable<Node<OWLClass>> thread = new Callable<Node<OWLClass>>() {
			public Node<OWLClass> call() {
				return delegate.getEquivalentClasses(ce);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLClass> getDisjointClasses(final OWLClassExpression ce)
			throws ReasonerInterruptedException, TimeOutException,
			FreshEntitiesException, InconsistentOntologyException {
		Callable<NodeSet<OWLClass>> thread = new Callable<NodeSet<OWLClass>>() {
			public NodeSet<OWLClass> call() {
				return delegate.getDisjointClasses(ce);
			}
		};
		return threadedRun(thread);
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
		Callable<NodeSet<OWLObjectPropertyExpression>> thread = new Callable<NodeSet<OWLObjectPropertyExpression>>() {
			public NodeSet<OWLObjectPropertyExpression> call() {
				return delegate.getSubObjectProperties(pe, direct);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
			final OWLObjectPropertyExpression pe, final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLObjectPropertyExpression>> thread = new Callable<NodeSet<OWLObjectPropertyExpression>>() {
			public NodeSet<OWLObjectPropertyExpression> call() {
				return delegate.getSuperObjectProperties(pe, direct);
			}
		};
		return threadedRun(thread);
	}

	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
			final OWLObjectPropertyExpression pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		Callable<Node<OWLObjectPropertyExpression>> thread = new Callable<Node<OWLObjectPropertyExpression>>() {
			public Node<OWLObjectPropertyExpression> call() {
				return delegate.getEquivalentObjectProperties(pe);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
			final OWLObjectPropertyExpression pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLObjectPropertyExpression>> thread = new Callable<NodeSet<OWLObjectPropertyExpression>>() {
			public NodeSet<OWLObjectPropertyExpression> call() {
				return delegate.getDisjointObjectProperties(pe);
			}
		};
		return threadedRun(thread);
	}

	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
			final OWLObjectPropertyExpression pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		Callable<Node<OWLObjectPropertyExpression>> thread = new Callable<Node<OWLObjectPropertyExpression>>() {
			public Node<OWLObjectPropertyExpression> call() {
				return delegate.getInverseObjectProperties(pe);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLClass> getObjectPropertyDomains(
			final OWLObjectPropertyExpression pe, final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLClass>> thread = new Callable<NodeSet<OWLClass>>() {
			public NodeSet<OWLClass> call() {
				return delegate.getObjectPropertyDomains(pe, direct);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLClass> getObjectPropertyRanges(
			final OWLObjectPropertyExpression pe, final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLClass>> thread = new Callable<NodeSet<OWLClass>>() {
			public NodeSet<OWLClass> call() {
				return delegate.getObjectPropertyRanges(pe, direct);
			}
		};
		return threadedRun(thread);
	}

	public Node<OWLDataProperty> getTopDataPropertyNode() {
		Callable<Node<OWLDataProperty>> thread = new Callable<Node<OWLDataProperty>>() {
			public Node<OWLDataProperty> call() {
				return delegate.getTopDataPropertyNode();
			}
		};
		return threadedRun(thread);
	}

	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		Callable<Node<OWLDataProperty>> thread = new Callable<Node<OWLDataProperty>>() {
			public Node<OWLDataProperty> call() {
				return delegate.getBottomDataPropertyNode();
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLDataProperty> getSubDataProperties(final OWLDataProperty pe,
			final boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLDataProperty>> thread = new Callable<NodeSet<OWLDataProperty>>() {
			public NodeSet<OWLDataProperty> call() {
				return delegate.getSubDataProperties(pe, direct);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLDataProperty> getSuperDataProperties(final OWLDataProperty pe,
			final boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLDataProperty>> thread = new Callable<NodeSet<OWLDataProperty>>() {
			public NodeSet<OWLDataProperty> call() {
				return delegate.getSuperDataProperties(pe, direct);
			}
		};
		return threadedRun(thread);
	}

	public Node<OWLDataProperty> getEquivalentDataProperties(final OWLDataProperty pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		Callable<Node<OWLDataProperty>> thread = new Callable<Node<OWLDataProperty>>() {
			public Node<OWLDataProperty> call() {
				return delegate.getEquivalentDataProperties(pe);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLDataProperty> getDisjointDataProperties(
			final OWLDataPropertyExpression pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLDataProperty>> thread = new Callable<NodeSet<OWLDataProperty>>() {
			public NodeSet<OWLDataProperty> call() {
				return delegate.getDisjointDataProperties(pe);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLClass> getDataPropertyDomains(final OWLDataProperty pe,
			final boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLClass>> thread = new Callable<NodeSet<OWLClass>>() {
			public NodeSet<OWLClass> call() {
				return delegate.getDataPropertyDomains(pe, direct);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLClass> getTypes(final OWLNamedIndividual ind, final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLClass>> thread = new Callable<NodeSet<OWLClass>>() {
			public NodeSet<OWLClass> call() {
				return delegate.getTypes(ind, direct);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLNamedIndividual> getInstances(final OWLClassExpression ce,
			final boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLNamedIndividual>> thread = new Callable<NodeSet<OWLNamedIndividual>>() {
			public NodeSet<OWLNamedIndividual> call() {
				return delegate.getInstances(ce, direct);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
			final OWLNamedIndividual ind, final OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLNamedIndividual>> thread = new Callable<NodeSet<OWLNamedIndividual>>() {
			public NodeSet<OWLNamedIndividual> call() {
				return delegate.getObjectPropertyValues(ind, pe);
			}
		};
		return threadedRun(thread);
	}

	public Set<OWLLiteral> getDataPropertyValues(final OWLNamedIndividual ind,
			final OWLDataProperty pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		Callable<Set<OWLLiteral>> thread = new Callable<Set<OWLLiteral>>() {
			public Set<OWLLiteral> call() {
				return delegate.getDataPropertyValues(ind, pe);
			}
		};
		return threadedRun(thread);
	}

	public Node<OWLNamedIndividual> getSameIndividuals(final OWLNamedIndividual ind)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		Callable<Node<OWLNamedIndividual>> thread = new Callable<Node<OWLNamedIndividual>>() {
			public Node<OWLNamedIndividual> call() {
				return delegate.getSameIndividuals(ind);
			}
		};
		return threadedRun(thread);
	}

	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
			final OWLNamedIndividual ind) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException, TimeOutException {
		Callable<NodeSet<OWLNamedIndividual>> thread = new Callable<NodeSet<OWLNamedIndividual>>() {
			public NodeSet<OWLNamedIndividual> call() {
				return delegate.getDifferentIndividuals(ind);
			}
		};
		return threadedRun(thread);
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
		delegate.dispose();
		exec.shutdownNow();
	}
}
