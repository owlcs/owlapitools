package decomposition;

import java.util.Collection;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

/** syntactic locality checker for DL axioms */
public class SyntacticLocalityChecker implements OWLAxiomVisitor, LocalityChecker {
    private Signature sig = new Signature();
    /** top evaluator */
    TopEquivalenceEvaluator TopEval;
    /** bottom evaluator */
    BotEquivalenceEvaluator BotEval;
    /** remember the axiom locality value here */
    boolean isLocal;

    /** @return true iff EXPR is top equivalent */
    @Override
    public boolean isTopEquivalent(OWLObject expr) {
        final boolean topEquivalent = TopEval.isTopEquivalent(expr);
        return topEquivalent;
    }

    /** @return true iff EXPR is bottom equivalent */
    @Override
    public boolean isBotEquivalent(OWLObject expr) {
        final boolean botEquivalent = BotEval.isBotEquivalent(expr);
        return botEquivalent;
    }

    /** @return true iff role expression in equivalent to const wrt locality */
    @Override
    public boolean isREquivalent(OWLObject expr) {
        return sig.topRLocal() ? isTopEquivalent(expr) : isBotEquivalent(expr);
    }

    /** init c'tor */
    public SyntacticLocalityChecker() {
        TopEval = new TopEquivalenceEvaluator(this);
        BotEval = new BotEquivalenceEvaluator(this);
    }

    @Override
    public Signature getSignature() {
        return sig;
    }

    /** set a new value of a signature (without changing a locality parameters) */
    @Override
    public void setSignatureValue(Signature Sig) {
        sig = Sig;
    }

    // set fields
    /** @return true iff an AXIOM is local wrt defined policy */
    @Override
    public boolean local(OWLAxiom axiom) {
        axiom.accept(this);
        return isLocal;
    }

    // TODO check
    @Override
    public void visit(OWLDeclarationAxiom axiom) {
        isLocal = true;
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom) {
        // 1 element => local
        if (axiom.getClassExpressions().size() < 2) {
            isLocal = true;
            return;
        }
        // axiom is local iff all the classes are either top- or bot-local
        isLocal = false;
        List<OWLClassExpression> args = axiom.getClassExpressionsAsList();
        int[] flags = new int[args.size()];
        for (int i = 0; i < args.size(); i++) {
            if (isBotEquivalent(args.get(i))) {
                flags[i] = -1;
            } else if (isTopEquivalent(args.get(i))) {
                flags[i] = 1;
            } else {
                flags[i] = 0;
            }
        }
        int total = 0;
        for (int i : flags) {
            total += i;
        }
        isLocal = Math.abs(total) == flags.length;
    }

    @Override
    public void visit(OWLDisjointClassesAxiom axiom) {
        // local iff at most 1 concept is not bot-equiv
        boolean hasNBE = false;
        isLocal = true;
        for (OWLClassExpression p : axiom.getClassExpressions()) {
            if (!isBotEquivalent(p)) {
                if (hasNBE) {
                    isLocal = false;
                    return;
                } else {
                    hasNBE = true;
                }
            }
        }
    }

    @Override
    public void visit(OWLDisjointUnionAxiom axiom) {
        // DisjointUnion(A, C1,..., Cn) is local if
        // (1) A and all of Ci are bot-equivalent,
        // or (2) A and one Ci are top-equivalent and the remaining Cj are
        // bot-equivalent
        isLocal = false;
        boolean lhsIsTopEq;
        if (isTopEquivalent(axiom.getOWLClass())) {
            lhsIsTopEq = true;  // need to check (2)
        } else if (isBotEquivalent(axiom.getOWLClass())) {
            lhsIsTopEq = false; // need to check (1)
        } else {
            return;             // neither (1) nor (2)
        }
        boolean topEqDesc = false;
        for (OWLClassExpression p : axiom.getClassExpressions()) {
            if (!isBotEquivalent(p)) {
                if (lhsIsTopEq && isTopEquivalent(p)) {
                    if (topEqDesc) {
                        // 2nd top in there -- violate (2) -- non-local
                        return;
                    } else {
                        topEqDesc = true;
                    }
                } else {
                    // either (1) or fail to have a top-eq for (2)
                    return;
                }
            }
        }
        // check whether for (2) we found a top-eq concept
        if ( lhsIsTopEq && !topEqDesc )
            return;
        isLocal = true;
    }

    @Override
    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        isLocal = true;
        if (axiom.getProperties().size() < 2) {
            return;
        }
        for (OWLObjectPropertyExpression p : axiom.getProperties()) {
            if (!isREquivalent(p)) {
                isLocal = false;
                return;
            }
        }
    }

    @Override
    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        isLocal = true;
        if (axiom.getProperties().size() < 2) {
            return;
        }
        for (OWLDataPropertyExpression p : axiom.getProperties()) {
            if (!isREquivalent(p)) {
                isLocal = false;
                return;
            }
        }
    }

    @Override
    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        isLocal = false;
        if (sig.topRLocal()) {
            return;
        }
        boolean hasNBE = false;
        for (OWLObjectPropertyExpression p : axiom.getProperties()) {
            if (!isREquivalent(p)) {
                if (hasNBE) {
                    return; // false here
                } else {
                    hasNBE = true;
                }
            }
        }
        isLocal = true;
    }

    @Override
    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        isLocal = false;
        if (sig.topRLocal()) {
            return;
        }
        boolean hasNBE = false;
        for (OWLDataPropertyExpression p : axiom.getProperties()) {
            if (!isREquivalent(p)) {
                if (hasNBE) {
                    return; // false here
                } else {
                    hasNBE = true;
                }
            }
        }
        isLocal = true;
    }

    @Override
    public void visit(OWLSameIndividualAxiom axiom) {
        isLocal = false;
    }

    @Override
    public void visit(OWLDifferentIndividualsAxiom axiom) {
        isLocal = false;
    }

    @Override
    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        isLocal = isREquivalent(axiom.getFirstProperty())
                && isREquivalent(axiom.getSecondProperty());
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getSuperProperty())
                || isBotEquivalent(axiom.getSubProperty());
    }

    @Override
    public void visit(OWLSubDataPropertyOfAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getSuperProperty())
                || isBotEquivalent(axiom.getSubProperty());
    }

    @Override
    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getDomain())
                || isBotEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLDataPropertyDomainAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getDomain())
                || isBotEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getRange())
                || isBotEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLDataPropertyRangeAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getRange())
                || isBotEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        isLocal = isREquivalent(axiom.getProperty());
    }

    /** as BotRole is irreflexive, the only local axiom is topEquivalent(R) */
    @Override
    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        isLocal = isBotEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        isLocal = isREquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        isLocal = isBotEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        isLocal = isBotEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        isLocal = isBotEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        isLocal = isBotEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom) {
        isLocal = isBotEquivalent(axiom.getSubClass())
                || isTopEquivalent(axiom.getSuperClass());
    }

    @Override
    public void visit(OWLClassAssertionAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getClassExpression());
    }

    @Override
    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        isLocal = isBotEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getProperty());
    }

    @Override
    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        isLocal = isBotEquivalent(axiom.getProperty());
    }

    @Override
    public void preprocessOntology(Collection<AxiomWrapper> s) {
        sig = new Signature();
        for (AxiomWrapper ax : s) {
            sig.addAll(ax.getAxiom().getSignature());
        }
    }

    // TODO verify the following
    @Override
    public void visit(OWLAnnotationAssertionAxiom axiom) {
        isLocal = true;
    }

    @Override
    public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        isLocal = true;
    }

    @Override
    public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        isLocal = true;
    }

    @Override
    public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        isLocal = true;
    }

    @Override
    public void visit(OWLSubPropertyChainOfAxiom axiom) {
        isLocal = true;
        if (isTopEquivalent(axiom.getSuperProperty())) {
            return;
        }
        for (OWLObjectPropertyExpression R : axiom.getPropertyChain()) {
            if (isBotEquivalent(R)) {
                return;
            }
        }
        isLocal = false;
    }

    @Override
    public void visit(OWLHasKeyAxiom axiom) {
        isLocal = true;
    }

    @Override
    public void visit(OWLDatatypeDefinitionAxiom axiom) {
        isLocal = true;
    }

    @Override
    public void visit(SWRLRule rule) {
        isLocal = true;
    }
}
