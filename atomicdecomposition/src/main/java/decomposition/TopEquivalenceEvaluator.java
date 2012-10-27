package decomposition;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
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
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

/** check whether class expressions are equivalent to top wrt given locality
 * class */
public class TopEquivalenceEvaluator extends SigAccessor implements OWLObjectVisitor {
    /** keep the value here */
    boolean isTopEq = false;

    public TopEquivalenceEvaluator(LocalityChecker l) {
        super(l);
    }

    /** @return true iff role expression in equivalent to const wrt locality */
    boolean isREquivalent(OWLObject expr) {
        return topRLocal() ? isTopEquivalent(expr) : localityChecker
                .isBotEquivalent(expr);
    }

    /** @return true iff an EXPRession is equivalent to top wrt defined policy */
    public boolean isTopEquivalent(OWLObject expr) {
        if (expr.isTopEntity()) {
            return true;
        }
        expr.accept(this);
        return isTopEq;
    }

    // concept expressions
    @Override
    public void visit(OWLClass expr) {
        isTopEq = getSignature().topCLocal() && !getSignature().contains(expr);
    }

    @Override
    public void visit(OWLObjectComplementOf expr) {
        isTopEq = localityChecker.isBotEquivalent(expr.getOperand());
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
        isTopEq = getSignature().topRLocal() && isTopEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectHasValue expr) {
        isTopEq = getSignature().topRLocal() && isTopEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom expr) {
        isTopEq = getSignature().topRLocal() && isTopEquivalent(expr.getProperty())
                && isTopEquivalent(expr.getFiller());
    }

    @Override
    public void visit(OWLObjectAllValuesFrom expr) {
        isTopEq = isTopEquivalent(expr.getFiller()) || !getSignature().topRLocal()
                && localityChecker.isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectMinCardinality expr) {
        isTopEq = expr.getCardinality() == 0 || getSignature().topRLocal()
                && isTopEquivalent(expr.getProperty())
                && isTopEquivalent(expr.getFiller());
    }

    @Override
    public void visit(OWLObjectMaxCardinality expr) {
        isTopEq = localityChecker.isBotEquivalent(expr.getFiller())
                || !getSignature().topRLocal()
                && localityChecker.isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectExactCardinality expr) {
        isTopEq = expr.getCardinality() == 0
                && (localityChecker.isBotEquivalent(expr.getFiller()) || !getSignature()
                        .topRLocal()
                        && localityChecker.isBotEquivalent(expr.getProperty()));
    }

    @Override
    public void visit(OWLDataHasValue expr) {
        isTopEq = getSignature().topRLocal() && isTopEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLDataSomeValuesFrom expr) {
        isTopEq = getSignature().topRLocal() && isTopEquivalent(expr.getProperty())
                && isTopOrBuiltInDataType(expr.getFiller());
    }

    @Override
    public void visit(OWLDataAllValuesFrom expr) {
        isTopEq = expr.getFiller().isTopDatatype() || !getSignature().topRLocal()
                && localityChecker.isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLDataMinCardinality expr) {
        isTopEq = expr.getCardinality() == 0;
        if (getSignature().topRLocal()) {
            isTopEq |= isTopEquivalent(expr.getProperty())
                    && (expr.getCardinality() == 1 ? isTopOrBuiltInDataType(expr
                            .getFiller()) : isTopOrBuiltInDataType(expr.getFiller()));
        }
    }

    @Override
    public void visit(OWLDataMaxCardinality expr) {
        isTopEq = !getSignature().topRLocal()
                && localityChecker.isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLDataExactCardinality expr) {
        isTopEq = !getSignature().topRLocal() && expr.getCardinality() == 0
                && localityChecker.isBotEquivalent(expr.getProperty());
    }

    @Override
    public void visit(OWLObjectProperty expr) {
        isTopEq = getSignature().topRLocal() && !getSignature().contains(expr);
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
        isTopEq = getSignature().topRLocal() && !getSignature().contains(expr);
    }
}
