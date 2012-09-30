package decomposition;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

/** check whether class expressions are equivalent to top wrt given locality
 * class */
@SuppressWarnings("javadoc")
public class TopEquivalenceEvaluator extends SigAccessor implements OWLObjectVisitor {
    /** corresponding bottom evaluator */
    BotEquivalenceEvaluator BotEval = null;
    /** keep the value here */
    boolean isTopEq = false;

    /** check whether the expression is top-equivalent */
    boolean isBotEquivalent(OWLObject expr) {
        return BotEval.isBotEquivalent(expr);
    }

    /** @return true iff role expression in equivalent to const wrt locality */
    boolean isREquivalent(OWLObject expr) {
        return topRLocal() ? isTopEquivalent(expr) : isBotEquivalent(expr);
    }

    // set fields
    /** set the corresponding bottom evaluator */
    void setBotEval(BotEquivalenceEvaluator eval) {
        BotEval = eval;
    }

    /** @return true iff an EXPRession is equivalent to top wrt defined policy */
    boolean isTopEquivalent(OWLObject expr) {
        if (expr.isTopEntity()) {
            return true;
        }
        expr.accept(this);
        return isTopEq;
    }

    // concept expressions
    @Override
    public void visit(OWLClass expr) {
        isTopEq = sig.topCLocal() && !sig.contains(expr);
    }

    @Override
    public void visit(OWLObjectComplementOf expr) {
        isTopEq = isBotEquivalent(expr.getOperand());
    }

    @Override
    public void visit(OWLObjectIntersectionOf expr) {
        for (OWLClassExpression p : expr.getOperands()) {
            if (!isTopEquivalent(p)) {
                return;
            }
        }
        isTopEq = true;
    }

    @Override
    public void visit(OWLObjectUnionOf expr) {
        for (OWLClassExpression p : expr.getOperands()) {
            if (isTopEquivalent(p)) {
                isTopEq = true;
                return;
            }
        }
        isTopEq = false;
    }

    @Override
    public void visit(OWLObjectOneOf expr) {
        isTopEq = false;
    }

    @Override
    public void visit(OWLObjectHasSelf expr) {
        isTopEq = sig.topRLocal() && isTopEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectHasValue expr) {
        isTopEq = sig.topRLocal() && isTopEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom expr) {
        isTopEq = sig.topRLocal() && isTopEquivalent(expr.getProperty())
                && isTopEquivalent(expr.getFiller());
    }

    @Override
    public void visit(OWLObjectAllValuesFrom expr) {
        isTopEq = isTopEquivalent(expr.getFiller()) || !sig.topRLocal()
                && isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectMinCardinality expr) {
        isTopEq = expr.getCardinality() == 0 || sig.topRLocal()
                && isTopEquivalent(expr.getProperty())
                && isTopEquivalent(expr.getFiller());
    }

    @Override
    public void visit(OWLObjectMaxCardinality expr) {
        isTopEq = isBotEquivalent(expr.getFiller()) || !sig.topRLocal()
                && isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectExactCardinality expr) {
        isTopEq = expr.getCardinality() == 0
                && (isBotEquivalent(expr.getFiller()) || !sig.topRLocal()
                        && isBotEquivalent(expr.getProperty()));
    }

    @Override
    public void visit(OWLDataHasValue expr) {
        isTopEq = sig.topRLocal() && isTopEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLDataSomeValuesFrom expr) {
        isTopEq = sig.topRLocal() && isTopEquivalent(expr.getProperty())
                && isTopOrBuiltInDataType(expr.getFiller());
    }

    @Override
    public void visit(OWLDataAllValuesFrom expr) {
        isTopEq = isTopDT(expr.getFiller()) || !sig.topRLocal()
                && isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLDataMinCardinality expr) {
        isTopEq = expr.getCardinality() == 0;
        if (sig.topRLocal()) {
            isTopEq |= isTopEquivalent(expr.getProperty())
                    && (expr.getCardinality() == 1 ? isTopOrBuiltInDataType(expr
                            .getFiller()) : isTopOrBuiltInDataType(expr.getFiller()));
        }
    }

    @Override
    public void visit(OWLDataMaxCardinality expr) {
        isTopEq = !sig.topRLocal() && isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLDataExactCardinality expr) {
        isTopEq = !sig.topRLocal() && expr.getCardinality() == 0
                && isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectProperty expr) {
        isTopEq = sig.topRLocal() && !sig.contains(expr);
    }

    @Override
    public void visit(OWLObjectInverseOf expr) {
        isTopEq = isTopEquivalent(expr.getInverse());
    }

    @Override
    public void visit(OWLSubPropertyChainOfAxiom expr) {
        for (OWLObjectPropertyExpression p : expr.getPropertyChain()) {
            if (!isTopEquivalent(p)) {
                return;
            }
        }
        isTopEq = true;
    }

    // data role expressions
    @Override
    public void visit(OWLDataProperty expr) {
        isTopEq = sig.topRLocal() && !sig.contains(expr);
    }

    @Override
    public void visit(OWLLiteral node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLFacetRestriction node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDatatype node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDataOneOf node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDataComplementOf node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDataIntersectionOf node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDataUnionOf node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDatatypeRestriction node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLNamedIndividual individual) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLAnnotationProperty property) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDeclarationAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDisjointClassesAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDataPropertyDomainAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDifferentIndividualsAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDisjointUnionAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDataPropertyRangeAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLClassAssertionAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLSubDataPropertyOfAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLSameIndividualAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLHasKeyAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLDatatypeDefinitionAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLRule rule) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLAnnotationAssertionAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLAnonymousIndividual individual) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(IRI iri) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLAnnotation node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLClassAtom node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLDataRangeAtom node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLObjectPropertyAtom node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLDataPropertyAtom node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLBuiltInAtom node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLVariable node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLIndividualArgument node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLLiteralArgument node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLSameIndividualAtom node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(SWRLDifferentIndividualsAtom node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(OWLOntology ontology) {
        // TODO Auto-generated method stub
    }
}
