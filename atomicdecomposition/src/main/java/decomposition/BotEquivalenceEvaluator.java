package decomposition;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
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

/** check whether class expressions are equivalent to bottom wrt given locality
 * class */
// XXX verify unused parameters
public class BotEquivalenceEvaluator extends SigAccessor implements OWLObjectVisitor {
    /** keep the value here */
    boolean isBotEq = false;

    public BotEquivalenceEvaluator(LocalityChecker l) {
        super(l);
    }

    /** @return true iff role expression in equivalent to const wrt locality */
    boolean isREquivalent(OWLObject expr) {
        return getSignature().topRLocal() ? localityChecker.isTopEquivalent(expr)
                : isBotEquivalent(expr);
    }

    // non-empty Concept/Data expression
    // / @return true iff C^I is non-empty
    private boolean isBotDistinct(OWLObject C) {
        // TOP is non-empty
        if (isBotEquivalent(C)) {
            return true;
        }
        // built-in DT are non-empty
        // FIXME!! that's it for now
        return C instanceof OWLDatatype;
    }

    // cardinality of a concept/data expression interpretation
    // / @return true if #C^I > n
    private boolean isCardLargerThan(OWLObject C, int n) {
        if (n == 0) {
            return isBotDistinct(C);
        }
        if (C instanceof OWLDatatype && isBotEquivalent(C)) {
            return true;
        }
        if (C instanceof OWLDatatype) {
            return ((OWLDatatype) C).isBuiltIn()
                    && !((OWLDatatype) C).getBuiltInDatatype().isFinite();
        }
        // FIXME!! try to be more precise
        return false;
    }

    // QCRs
    // / @return true iff (>= n R.C) is botEq
    private boolean isMinBotEquivalent(int n, OWLObject R, OWLObject C) {
        return n > 0 && (isBotEquivalent(R) || isBotEquivalent(C));
    }

    // / @return true iff (<= n R.C) is botEq
    private boolean isMaxBotEquivalent(int n, OWLObject R, OWLObject C) {
        return isBotEquivalent(R) && isCardLargerThan(C, n);
    }

    /** @return true iff an EXPRession is equivalent to bottom wrt defined */
    // policy
    boolean isBotEquivalent(OWLObject expr) {
        if (expr.isBottomEntity()) {
            return true;
        }
        expr.accept(this);
        return isBotEq;
    }

    // concept expressions
    @Override
    // ported from: public void visit(ConceptName expr) {
    public void visit(OWLClass expr) {
        isBotEq = !getSignature().topCLocal() && !getSignature().contains(expr);
    }

    @Override
    // ported from: public void visit(ConceptNot expr) {
    public void visit(OWLObjectComplementOf expr) {
        isBotEq = localityChecker.isTopEquivalent(expr.getOperand());
    }

    @Override
    // ported from:public void visit(ConceptAnd expr) {
    public void visit(OWLObjectIntersectionOf expr) {
        for (OWLClassExpression p : expr.getOperands()) {
            if (isBotEquivalent(p)) {
                return;
            }
        }
        isBotEq = false;
    }

    @Override
    // ported from: public void visit(ConceptOr expr) {
    public void visit(OWLObjectUnionOf expr) {
        isBotEq = true;
        for (OWLClassExpression p : expr.getOperands()) {
            if (!isBotEquivalent(p)) {
                isBotEq=false;
                return;
            }
        }
    }

    @Override
    // ported from: public void visit(ConceptOneOf expr) {
    public void visit(OWLObjectOneOf expr) {
        isBotEq = expr.getIndividuals().isEmpty();
    }

    @Override
    // ported from: public void visit(ConceptObjectSelf expr) {
    public void visit(OWLObjectHasSelf expr) {
        isBotEq = isBotEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ConceptObjectValue expr) {
    public void visit(OWLObjectHasValue expr) {
        isBotEq = isBotEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ConceptObjectExists expr) {
    public void visit(OWLObjectSomeValuesFrom expr) {
        isBotEq = isMinBotEquivalent(1, expr.getProperty(), expr.getFiller());
    }

    @Override
    // ported from: public void visit(ConceptObjectForall expr) {
    public void visit(OWLObjectAllValuesFrom expr) {
        isBotEq = localityChecker.isTopEquivalent(expr.getProperty())
                && isBotEquivalent(expr.getFiller());
    }

    @Override
    // ported from: public void visit(ConceptObjectMinCardinality expr) {
    public void visit(OWLObjectMinCardinality expr) {
        isBotEq = expr.getCardinality() > 0
                && (isBotEquivalent(expr.getFiller()) || !getSignature().topRLocal()
                        && isBotEquivalent(expr.getProperty()));
    }

    @Override
    // ported from: public void visit(ConceptObjectMaxCardinality expr) {
    public void visit(OWLObjectMaxCardinality expr) {
        isBotEq = getSignature().topRLocal() && expr.getCardinality() > 0
                && localityChecker.isTopEquivalent(expr.getProperty())
                && localityChecker.isTopEquivalent(expr.getFiller());
    }

    @Override
    // ported from: public void visit(ConceptObjectExactCardinality expr) {
    public void visit(OWLObjectExactCardinality expr) {
        int n = expr.getCardinality();
        isBotEq = isMinBotEquivalent(n, expr.getProperty(), expr.getFiller())
                || isMaxBotEquivalent(n, expr.getProperty(), expr.getFiller());
    }

    @Override
    // ported from: public void visit(ConceptDataValue expr) {
    public void visit(OWLDataHasValue expr) {
        isBotEq = isBotEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ConceptDataExists expr) {
    public void visit(OWLDataSomeValuesFrom expr) {
        isBotEq = isMinBotEquivalent(1, expr.getProperty(), expr.getFiller());
    }

    @Override
    // ported from: public void visit(ConceptDataForall expr) {
    public void visit(OWLDataAllValuesFrom expr) {
        isBotEq = localityChecker.isTopEquivalent(expr.getProperty())
                && !expr.getFiller().isTopDatatype();
    }

    @Override
    // ported from: public void visit(ConceptDataMinCardinality expr) {
    public void visit(OWLDataMinCardinality expr) {
        isBotEq = !getSignature().topRLocal() && expr.getCardinality() > 0
                && isBotEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ConceptDataMaxCardinality expr) {
    public void visit(OWLDataMaxCardinality expr) {
        isBotEq = getSignature().topRLocal()
                && localityChecker.isTopEquivalent(expr.getProperty())
                && (expr.getCardinality() <= 1 ? isTopOrBuiltInDataType(expr.getFiller())
                        : isTopOrBuiltInDataType(expr.getFiller()));
    }

    @Override
    // ported from: public void visit(ConceptDataExactCardinality expr) {
    public void visit(OWLDataExactCardinality expr) {
        isBotEq = isREquivalent(expr.getProperty())
                && (getSignature().topRLocal() ? expr.getCardinality() == 0 ? isTopOrBuiltInDataType(expr
                        .getFiller()) : isTopOrBuiltInDataType(expr.getFiller())
                        : expr.getCardinality() > 0);
    }

    // object role expressions
    @Override
    // ported from: public void visit(ObjectRoleName expr) {
    public void visit(OWLObjectProperty expr) {
        isBotEq = !getSignature().topRLocal() && !getSignature().contains(expr);
    }

    @Override
    // ported from: public void visit(ObjectRoleInverse expr) {
    public void visit(OWLObjectInverseOf expr) {
        isBotEq = isBotEquivalent(expr.getInverse());
    }

    @Override
    // ported from: public void visit(ObjectRoleChain expr) {
    public void visit(OWLSubPropertyChainOfAxiom expr) {
        isBotEq=true;
        for (OWLObjectPropertyExpression p : expr.getPropertyChain()) {
            if (isBotEquivalent(p)) {
                return;
            }
        }
        isBotEq = false;
    }

    // data role expressions
    @Override
    // ported from: public void visit(DataRoleName expr) {
    public void visit(OWLDataProperty expr) {
        isBotEq = !getSignature().topRLocal() && !getSignature().contains(expr);
    }

    @Override
    // ported from: public void visit(Datatype<?> arg) 
    public void visit(OWLDatatype node) {
        isBotEq=node.isBottomEntity();
    }

    @Override
    // ported from: public void visit(Literal<?> arg) {
    public void visit(OWLLiteral node) {
        isBotEq=false;
    }

    @Override
    // ported from: public void visit(DataNot expr) {
    public void visit(OWLDataComplementOf node) {
        isBotEq=node.getDataRange().isTopEntity();
    }

    @Override
    // ported from: public void visit(DataOneOf arg) {
    public void visit(OWLDataOneOf node) {
        isBotEq=false;
    }


}
