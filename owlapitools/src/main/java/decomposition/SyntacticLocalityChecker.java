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
@SuppressWarnings("unused")
public class SyntacticLocalityChecker extends SigAccessor implements OWLAxiomVisitor,
        LocalityChecker {
    /** top evaluator */
    TopEquivalenceEvaluator TopEval;
    /** bottom evaluator */
    BotEquivalenceEvaluator BotEval;
    /** remember the axiom locality value here */
    boolean isLocal;

    /** @return true iff EXPR is top equivalent */
    boolean isTopEquivalent(OWLObject expr) {
        final boolean topEquivalent = TopEval.isTopEquivalent(expr);
        return topEquivalent;
    }

    /** @return true iff EXPR is bottom equivalent */
    boolean isBotEquivalent(OWLObject expr) {
        return BotEval.isBotEquivalent(expr);
    }

    /** @return true iff role expression in equivalent to const wrt locality */
    boolean isREquivalent(OWLObject expr) {
        return sig.topRLocal() ? isTopEquivalent(expr) : isBotEquivalent(expr);
    }

    /** init c'tor */
    public SyntacticLocalityChecker() {
        TopEval = new TopEquivalenceEvaluator();
        BotEval = new BotEquivalenceEvaluator();
        TopEval.setBotEval(BotEval);
        BotEval.setTopEval(TopEval);
    }

    public Signature getSignature() {
        return sig;
    }

    /** set a new value of a signature (without changing a locality parameters) */
    public void setSignatureValue(Signature Sig) {
        sig = Sig;
        TopEval.sig = sig;
        BotEval.sig = sig;
    }

    // set fields
    /** @return true iff an AXIOM is local wrt defined policy */
    public boolean local(OWLAxiom axiom) {
        axiom.accept(this);
        return isLocal;
    }

    // TODO check
    public void visit(OWLDeclarationAxiom axiom) {
        isLocal = !axiom.getEntity().isOWLAnnotationProperty();
    }

    public void visit(OWLEquivalentClassesAxiom axiom) {
        // 1 element => local
        if (axiom.getClassExpressions().size() == 1) {
            isLocal = true;
            return;
        }
        // axiom is local iff all the classes are either top- or bot-local
        isLocal = false;
        List<OWLClassExpression> args = axiom.getClassExpressionsAsList();
        if (args.size() > 0) {
            if (isBotEquivalent(args.get(0))) {
                for (int i = 1; i < args.size(); i++) {
                    if (!isBotEquivalent(args.get(i))) {
                        return;
                    }
                }
            } else {
                if (!isTopEquivalent(args.get(0))) {
                    return;
                }
                for (int i = 1; i < args.size(); i++) {
                    if (!isTopEquivalent(args.get(i))) {
                        return;
                    }
                }
            }
        }
        isLocal = true;
    }

    public void visit(OWLDisjointClassesAxiom axiom) {
        // local iff at most 1 concept is not bot-equiv
        boolean hasNBE = false;
        isLocal = true;
        for (OWLClassExpression p : axiom.getClassExpressions()) {
            if (!isBotEquivalent(p)) {
                if (hasNBE) {
                    isLocal = false;
                    break;
                } else {
                    hasNBE = true;
                }
            }
        }
    }

    public void visit(OWLDisjointUnionAxiom axiom) {
        isLocal = false;
        boolean topLoc = sig.topCLocal();
        if (!(topLoc ? isTopEquivalent(axiom.getOWLClass()) : isBotEquivalent(axiom
                .getOWLClass()))) {
            return;
        }
        boolean topEqDesc = false;
        for (OWLClassExpression p : axiom.getClassExpressions()) {
            if (!isBotEquivalent(p)) {
                if (!topLoc) {
                    return; // non-local straight away
                }
                if (isTopEquivalent(p)) {
                    if (topEqDesc) {
                        return; // 2nd top in there -- non-local
                    } else {
                        topEqDesc = true;
                    }
                } else {
                    return; // non-local
                }
            }
        }
        isLocal = true;
    }

    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        isLocal = true;
        if (axiom.getProperties().size() <= 1) {
            return;
        }
        for (OWLObjectPropertyExpression p : axiom.getProperties()) {
            if (!isREquivalent(p)) {
                isLocal = false;
                break;
            }
        }
    }

    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        isLocal = true;
        if (axiom.getProperties().size() <= 1) {
            return;
        }
        for (OWLDataPropertyExpression p : axiom.getProperties()) {
            if (!isREquivalent(p)) {
                isLocal = false;
                break;
            }
        }
    }

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

    public void visit(OWLSameIndividualAxiom axiom) {
        isLocal = false;
    }

    public void visit(OWLDifferentIndividualsAxiom axiom) {
        isLocal = false;
    }

    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        isLocal = isREquivalent(axiom.getFirstProperty())
                && isREquivalent(axiom.getSecondProperty());
    }

    public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        isLocal = isREquivalent(sig.topRLocal() ? axiom.getSuperProperty() : axiom
                .getSubProperty());
    }

    public void visit(OWLSubDataPropertyOfAxiom axiom) {
        isLocal = isREquivalent(sig.topRLocal() ? axiom.getSuperProperty() : axiom
                .getSubProperty());
    }

    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getDomain());
        if (!sig.topRLocal()) {
            isLocal |= isBotEquivalent(axiom.getProperty());
        }
    }

    public void visit(OWLDataPropertyDomainAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getDomain());
        if (!sig.topRLocal()) {
            isLocal |= isBotEquivalent(axiom.getProperty());
        }
    }

    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getRange());
        if (!sig.topRLocal()) {
            isLocal |= isBotEquivalent(axiom.getProperty());
        }
    }

    public void visit(OWLDataPropertyRangeAxiom axiom) {
        isLocal = axiom.getRange().isTopDatatype();
        if (!sig.topRLocal()) {
            isLocal |= isBotEquivalent(axiom.getProperty());
        }
    }

    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        isLocal = isREquivalent(axiom.getProperty());
    }

    /** as BotRole is irreflexive, the only local axiom is topEquivalent(R) */
    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getProperty());
    }

    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        isLocal = !sig.topRLocal();
    }

    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        isLocal = isREquivalent(axiom.getProperty());
    }

    public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        isLocal = !sig.topRLocal();
    }

    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        isLocal = !sig.topRLocal() && isBotEquivalent(axiom.getProperty());
    }

    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        isLocal = !sig.topRLocal() && isBotEquivalent(axiom.getProperty());
    }

    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        isLocal = !sig.topRLocal() && isBotEquivalent(axiom.getProperty());
    }

    public void visit(OWLSubClassOfAxiom axiom) {
        isLocal = isBotEquivalent(axiom.getSubClass())
                || isTopEquivalent(axiom.getSuperClass());
    }

    public void visit(OWLClassAssertionAxiom axiom) {
        isLocal = isTopEquivalent(axiom.getClassExpression());
    }

    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        isLocal = sig.topRLocal() && isTopEquivalent(axiom.getProperty());
    }

    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        isLocal = !sig.topRLocal() && isBotEquivalent(axiom.getProperty());
    }

    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        isLocal = sig.topRLocal() && isTopEquivalent(axiom.getProperty());
    }

    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        isLocal = !sig.topRLocal() && isBotEquivalent(axiom.getProperty());
    }

    public void preprocessOntology(Collection<AxiomWrapper> s) {
        sig = new Signature();
        for (AxiomWrapper ax : s) {
            sig.addAll(ax.getAxiom().getSignature());
        }
    }

    // TODO verify the following
    @Override
    public void visit(OWLAnnotationAssertionAxiom axiom) {
        isLocal = false;
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
        isLocal = false;
    }

    @Override
    public void visit(OWLHasKeyAxiom axiom) {
        isLocal = false;
    }

    @Override
    public void visit(OWLDatatypeDefinitionAxiom axiom) {
        isLocal = false;
    }

    @Override
    public void visit(SWRLRule rule) {
        isLocal = true;
    }
}
