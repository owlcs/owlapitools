package utils.threadedreasoner;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.semanticweb.owlapi.util.Version;

public class ThreadedReasoner implements OWLReasoner {
	protected OWLReasoner delegate;
	protected ExecutorService exec = null;
	protected AtomicBoolean interrupted = new AtomicBoolean();

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
		interrupted.set(true);
	}

	AtomicBoolean done = new AtomicBoolean(false);

	class Interruptor implements Runnable {
		public void run() {
			while (!done.get()) {
				if (interrupted.get()) {
					done.set(true);
					exec.shutdownNow();
				}
			}
		}
	}

	public void precomputeInferences(final InferenceType... inferenceTypes)
			throws ReasonerInterruptedException, TimeOutException,
			InconsistentOntologyException {
		Runnable thread = new Runnable() {
			public void run() {
				try {
					delegate.precomputeInferences(inferenceTypes);
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
	}

	private void threadedRun(Runnable r) {
		try {
			exec = Executors.newFixedThreadPool(3);
			done.set(false);
			exec.submit(r);
			exec.submit(new Interruptor());
			exec.shutdown();
			if(delegate.getTimeOut()>0) {
			exec.awaitTermination(delegate.getTimeOut(), TimeUnit.MILLISECONDS);
			}else {
				// wait until completed
				while(!exec.isTerminated()) {
					Thread.sleep(50);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new TimeOutException();
		} finally {
			exec = null;
		}
	}

	public boolean isConsistent() throws ReasonerInterruptedException,
			TimeOutException {
		final AtomicBoolean toReturn = new AtomicBoolean();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.isConsistent());
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public boolean isSatisfiable(final OWLClassExpression classExpression)
			throws ReasonerInterruptedException, TimeOutException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		final AtomicBoolean toReturn = new AtomicBoolean();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.isSatisfiable(classExpression));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public Node<OWLClass> getUnsatisfiableClasses()
			throws ReasonerInterruptedException, TimeOutException,
			InconsistentOntologyException {
		final AtomicReference<Node<OWLClass>> toReturn = new AtomicReference<Node<OWLClass>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getUnsatisfiableClasses());
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public boolean isEntailed(final OWLAxiom axiom)
			throws ReasonerInterruptedException,
			UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		final AtomicBoolean toReturn = new AtomicBoolean();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.isEntailed(axiom));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public boolean isEntailed(final Set<? extends OWLAxiom> axioms)
			throws ReasonerInterruptedException,
			UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		final AtomicBoolean toReturn = new AtomicBoolean();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.isEntailed(axioms));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public boolean isEntailmentCheckingSupported(final AxiomType<?> axiomType) {
		final AtomicBoolean toReturn = new AtomicBoolean();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate
							.isEntailmentCheckingSupported(axiomType));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLClass> getSubClasses(final OWLClassExpression ce, final boolean direct)
			throws ReasonerInterruptedException, TimeOutException,
			FreshEntitiesException, InconsistentOntologyException,
			ClassExpressionNotInProfileException {
		final AtomicReference<NodeSet<OWLClass>> toReturn = new AtomicReference<NodeSet<OWLClass>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getSubClasses(ce, direct));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLClass> getSuperClasses(final OWLClassExpression ce,
			final boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<NodeSet<OWLClass>> toReturn = new AtomicReference<NodeSet<OWLClass>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getSuperClasses(ce, direct));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();	}

	public Node<OWLClass> getEquivalentClasses(			final OWLClassExpression ce)
			throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<Node<OWLClass>> toReturn = new AtomicReference<Node<OWLClass>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getEquivalentClasses(ce));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();	}

	public NodeSet<OWLClass> getDisjointClasses(			final OWLClassExpression ce)
			throws ReasonerInterruptedException, TimeOutException,
			FreshEntitiesException, InconsistentOntologyException {
		final AtomicReference<NodeSet<OWLClass>> toReturn = new AtomicReference<NodeSet<OWLClass>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getDisjointClasses(ce));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();	}

	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
		return delegate.getTopObjectPropertyNode();
	}

	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
		return delegate.getBottomObjectPropertyNode();
	}

	public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
			final OWLObjectPropertyExpression pe,			final  boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<NodeSet<OWLObjectPropertyExpression>> toReturn = new AtomicReference<NodeSet<OWLObjectPropertyExpression>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getSubObjectProperties(pe, direct));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
			final OWLObjectPropertyExpression pe, 			final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<NodeSet<OWLObjectPropertyExpression>> toReturn = new AtomicReference<NodeSet<OWLObjectPropertyExpression>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getSuperObjectProperties(pe, direct));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
			final OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<Node<OWLObjectPropertyExpression>> toReturn = new AtomicReference<Node<OWLObjectPropertyExpression>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getEquivalentObjectProperties(pe));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
			final 	OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<NodeSet<OWLObjectPropertyExpression>> toReturn = new AtomicReference<NodeSet<OWLObjectPropertyExpression>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getDisjointObjectProperties(pe));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
			final OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<Node<OWLObjectPropertyExpression>> toReturn = new AtomicReference<Node<OWLObjectPropertyExpression>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getInverseObjectProperties(pe));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLClass> getObjectPropertyDomains(
			final OWLObjectPropertyExpression pe, 			final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<NodeSet<OWLClass>> toReturn = new AtomicReference<NodeSet<OWLClass>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getObjectPropertyDomains(pe, direct));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLClass> getObjectPropertyRanges(
			final OWLObjectPropertyExpression pe, 			final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<NodeSet<OWLClass>> toReturn = new AtomicReference<NodeSet<OWLClass>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getObjectPropertyRanges(pe, direct));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public Node<OWLDataProperty> getTopDataPropertyNode() {
		final AtomicReference<Node<OWLDataProperty>> toReturn = new AtomicReference<Node<OWLDataProperty>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getTopDataPropertyNode());
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		final AtomicReference<Node<OWLDataProperty>> toReturn = new AtomicReference<Node<OWLDataProperty>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getBottomDataPropertyNode());
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLDataProperty> getSubDataProperties(			final OWLDataProperty pe,
			final 	boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		final AtomicReference<NodeSet<OWLDataProperty>> toReturn = new AtomicReference<NodeSet<OWLDataProperty>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getSubDataProperties(pe, direct));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLDataProperty> getSuperDataProperties(			final OWLDataProperty pe,
			final 	boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		final AtomicReference<NodeSet<OWLDataProperty
		>> toReturn = new AtomicReference<NodeSet<OWLDataProperty>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getSuperDataProperties(pe, direct));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public Node<OWLDataProperty> getEquivalentDataProperties(			final OWLDataProperty pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<Node<OWLDataProperty>> toReturn = new AtomicReference<Node<OWLDataProperty>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getEquivalentDataProperties(pe));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLDataProperty> getDisjointDataProperties(
			final OWLDataPropertyExpression pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		final AtomicReference<NodeSet<OWLDataProperty>> toReturn = new AtomicReference<NodeSet<OWLDataProperty>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getDisjointDataProperties(pe));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLClass> getDataPropertyDomains(			final OWLDataProperty pe,
			final boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		final AtomicReference<NodeSet<OWLClass>> toReturn = new AtomicReference<NodeSet<OWLClass>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getDataPropertyDomains(pe, direct));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();	}

	public NodeSet<OWLClass> getTypes(			final OWLNamedIndividual ind, 			final boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<NodeSet<OWLClass>> toReturn = new AtomicReference<NodeSet<OWLClass>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getTypes(ind, direct));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLNamedIndividual> getInstances(			final OWLClassExpression ce,
			final boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<NodeSet<OWLNamedIndividual>> toReturn = new AtomicReference<NodeSet<OWLNamedIndividual>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getInstances(ce, direct));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
			final OWLNamedIndividual ind,			final  OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<NodeSet<OWLNamedIndividual>> toReturn = new AtomicReference<NodeSet<OWLNamedIndividual>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getObjectPropertyValues(ind, pe));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public Set<OWLLiteral> getDataPropertyValues(			final OWLNamedIndividual ind,
			final 			OWLDataProperty pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		final AtomicReference<Set<OWLLiteral>> toReturn = new AtomicReference<Set<OWLLiteral>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getDataPropertyValues(ind, pe));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public Node<OWLNamedIndividual> getSameIndividuals(			final OWLNamedIndividual ind)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		final AtomicReference<Node<OWLNamedIndividual>> toReturn = new AtomicReference<Node<OWLNamedIndividual>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getSameIndividuals(ind));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();
	}

	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(			final 
			OWLNamedIndividual ind) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		final AtomicReference<NodeSet<OWLNamedIndividual>> toReturn = new AtomicReference<NodeSet<OWLNamedIndividual>>();
		Runnable thread = new Runnable() {
			public void run() {
				try {
					toReturn.set(delegate.getDifferentIndividuals(ind));
				} finally {
					done.set(true);
				}
			}
		};
		threadedRun(thread);
		return toReturn.get();	}

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
	}
}
