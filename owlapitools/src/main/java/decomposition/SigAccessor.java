package decomposition;

import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.util.OWLObjectVisitorAdapter;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

/** helper class to set signature and locality class */
public class SigAccessor extends OWLObjectVisitorAdapter {
    /** signature of a module */
    Signature sig;

    public void setSignature(Signature s) {
        sig = s;
    }

    /** @return true iff EXPR is a top datatype */
    static boolean isTopDT(OWLDataRange expr) {
        return expr.isTopDatatype();
    }

    /** // @return true iff EXPR is a top datatype or a built-in datatype; FIXME */
    // for now -- just top
    // static boolean isTopOrBuiltInDT( Expression expr) {
    // return isTopDT(expr);
    // }
    //
    /** // @return true iff EXPR is a top datatype or an infinite built-in */
    // datatype; FIXME for now -- just top
    // static boolean isTopOrBuiltInInfDT( Expression expr) {
    // return isTopDT(expr);
    // }
    /** @return true iff EXPR is a top datatype or a built-in datatype; */
    public boolean isTopOrBuiltInDataType(OWLDataRange expr) {
        return isTopDT(expr) || expr.isDatatype() && expr.asOWLDatatype().isBuiltIn();
    }

    /** @return true iff EXPR is a top datatype or an infinite built-in */
    // datatype; FIXME add real/fraction later
    public boolean isTopOrBuiltInInfDataType(OWLDataRange expr) {
        if (isTopDT(expr)) {
            return true;
        }
        if (expr.isDatatype()) {
            final OWLDatatype dt = expr.asOWLDatatype();
            return dt.isBuiltIn() && OWL2Datatype.getDatatype(dt.getIRI()).isFinite();
        }
        return false;
    }

    /** @return true iff concepts are treated as TOPs */
    public boolean topCLocal() {
        return sig.topCLocal();
    }

    /** @return true iff roles are treated as TOPs */
    public boolean topRLocal() {
        return sig.topRLocal();
    }
}
