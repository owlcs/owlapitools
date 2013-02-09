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
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
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

/** check whether class expressions are equivalent to top wrt given locality
 * class */
public class TopEquivalenceEvaluator extends SigAccessor implements OWLObjectVisitor {
    /** keep the value here */
    boolean isTopEq = false;

    // non-empty Concept/Data expression
    // / @return true iff C^I is non-empty
    private boolean isBotDistinct(OWLObject C) {
        // TOP is non-empty
        if (isTopEquivalent(C)) {
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
        if (C instanceof OWLDatatype && isTopEquivalent(C)) {
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
    // / @return true iff (>= n R.C) is topEq
    private boolean isMinTopEquivalent(int n, OWLObject R, OWLObject C) {
        return n == 0 || isTopEquivalent(R) && isCardLargerThan(C, n - 1);
    }

    // /// @return true iff (<= n R.C) is topEq
    // private boolean isMaxTopEquivalent(int n, OWLObject R, OWLObject C)
    // { return isBotEquivalent(R) || isBotEquivalent(C); }
    public TopEquivalenceEvaluator(LocalityChecker l) {
        super(l);
    }

    /** @return true iff role expression in equivalent to const wrt locality */
    boolean isREquivalent(OWLObject expr) {
        return topRLocal() ? isTopEquivalent(expr) : localityChecker
                .isBotEquivalent(expr);
    }

    /** @return true iff an EXPRession is equivalent to top wrt defined policy */
    // ported from: boolean isTopEquivalent(Expression expr) {
    public boolean isTopEquivalent(OWLObject expr) {
        if (expr.isTopEntity()) {
            return true;
        }
        expr.accept(this);
        return isTopEq;
    }

    // concept expressions
    @Override
    // ported from: public void visit(ConceptName expr) {
    public void visit(OWLClass expr) {
        isTopEq = getSignature().topCLocal() && !getSignature().contains(expr);
    }

    @Override
    // ported from: public void visit(ConceptNot expr) {
    public void visit(OWLObjectComplementOf expr) {
        isTopEq = localityChecker.isBotEquivalent(expr.getOperand());
    }

    @Override
    // ported from:public void visit(ConceptAnd expr) {
    public void visit(OWLObjectIntersectionOf expr) {
        for (OWLClassExpression p : expr.getOperands()) {
            if (!isTopEquivalent(p)) {
                return;
            }
        }
        isTopEq = true;
    }

    @Override
    // ported from: public void visit(ConceptOr expr) {
    public void visit(OWLObjectUnionOf expr) {
        isTopEq = false;
        for (OWLClassExpression p : expr.getOperands()) {
            if (isTopEquivalent(p)) {
                isTopEq = true;
                return;
            }
        }
    }

    @Override
    // ported from: public void visit(ConceptOneOf expr) {
    public void visit(OWLObjectOneOf expr) {
        isTopEq = false;
    }

    @Override
    // ported from: public void visit(ConceptObjectSelf expr) {
    public void visit(OWLObjectHasSelf expr) {
        isTopEq = isTopEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ConceptObjectValue expr) {
    public void visit(OWLObjectHasValue expr) {
        isTopEq = isTopEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ConceptObjectExists expr) {
    public void visit(OWLObjectSomeValuesFrom expr) {
        isTopEq = isMinTopEquivalent(1, expr.getProperty(), expr.getFiller());
    }

    @Override
    // ported from: public void visit(ConceptObjectForall expr) {
    public void visit(OWLObjectAllValuesFrom expr) {
        isTopEq = isTopEquivalent(expr.getFiller())
                || localityChecker.isBotEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ConceptObjectMinCardinality expr) {
    public void visit(OWLObjectMinCardinality expr) {
        isTopEq = expr.getCardinality() == 0 || expr.getCardinality()==1&& getSignature().topRLocal()
                && isTopEquivalent(expr.getProperty())
                && isTopEquivalent(expr.getFiller());
    }

    @Override
    // ported from: public void visit(ConceptObjectMaxCardinality expr) {
    public void visit(OWLObjectMaxCardinality expr) {
        isTopEq = localityChecker.isBotEquivalent(expr.getFiller())
                || !getSignature().topRLocal()
                && localityChecker.isBotEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ConceptObjectExactCardinality expr) {
    public void visit(OWLObjectExactCardinality expr) {
        isTopEq = expr.getCardinality() == 0
                && (localityChecker.isBotEquivalent(expr.getFiller()) || !getSignature()
                        .topRLocal()
                        && localityChecker.isBotEquivalent(expr.getProperty()));
    }

    @Override
    // ported from: public void visit(ConceptDataValue expr) {
    public void visit(OWLDataHasValue expr) {
        isTopEq = isTopEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ConceptDataExists expr) {
    public void visit(OWLDataSomeValuesFrom expr) {
        isTopEq = isMinTopEquivalent(1, expr.getProperty(), expr.getFiller());
    }

    @Override
    // ported from: public void visit(ConceptDataForall expr) {
    public void visit(OWLDataAllValuesFrom expr) {
        isTopEq = isTopEquivalent(expr.getFiller())
                || localityChecker.isBotEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ConceptDataMinCardinality expr) {
    public void visit(OWLDataMinCardinality expr) {
        isTopEq = expr.getCardinality() == 0;
        if (getSignature().topRLocal()) {
            isTopEq |= isTopEquivalent(expr.getProperty())
                    && (expr.getCardinality() == 1 ? isTopOrBuiltInDataType(expr
                            .getFiller()) : isTopOrBuiltInDataType(expr.getFiller()));
        }
    }

    @Override
    // ported from: public void visit(ConceptDataMaxCardinality expr) {
    public void visit(OWLDataMaxCardinality expr) {
        isTopEq = !getSignature().topRLocal()
                && localityChecker.isBotEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ConceptDataExactCardinality expr) {
    public void visit(OWLDataExactCardinality expr) {
        isTopEq = !getSignature().topRLocal() && expr.getCardinality() == 0
                && localityChecker.isBotEquivalent(expr.getProperty());
    }

    @Override
    // ported from: public void visit(ObjectRoleName expr) {
    public void visit(OWLObjectProperty expr) {
        isTopEq = getSignature().topRLocal() && !getSignature().contains(expr);
    }

    @Override
    // ported from: public void visit(ObjectRoleInverse expr) {
    public void visit(OWLObjectInverseOf expr) {
        isTopEq = isTopEquivalent(expr.getInverse());
    }

    @Override
    // ported from: public void visit(ObjectRoleChain expr) {
    public void visit(OWLSubPropertyChainOfAxiom expr) {
        isTopEq = false;
        for (OWLObjectPropertyExpression p : expr.getPropertyChain()) {
            if (!isTopEquivalent(p)) {
                return;
            }
        }
        isTopEq = true;
    }

    // data role expressions
    @Override
    // ported from: public void visit(DataRoleName expr) {
    public void visit(OWLDataProperty expr) {
        // note: added check for top entity as FaCT++/JFact have a special case
        // for that
        isTopEq = expr.isTopEntity() || getSignature().topRLocal()
                && !getSignature().contains(expr);
    }

    @Override
    // ported from: public void visit(Datatype<?> arg)
            public
            void visit(OWLDatatype node) {
        isTopEq = node.isTopDatatype();
    }

    @Override
    // ported from: public void visit(Literal<?> arg) {
            public
            void visit(OWLLiteral node) {
        isTopEq = false;
    }

    @Override
    // ported from: public void visit(DataNot expr) {
            public
            void visit(OWLDataComplementOf node) {
        isTopEq = !node.isBottomEntity();
    }

    @Override
    // ported from: public void visit(DataAnd expr)
            public
            void visit(OWLDatatypeRestriction node) {
        isTopEq = node.isTopDatatype();
    }

    @Override
    // ported from: public void visit(DataOneOf arg) {
            public
            void visit(OWLDataOneOf node) {
        isTopEq = false;
    }
}
