package org.coode.suggestor.util;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

/** Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jul 12, 2011<br>
 * <br> */
class RestrictionVisitor extends OWLClassExpressionVisitorAdapter {
    protected final OWLReasoner r;
    protected final OWLPropertyExpression<?, ?> prop;
    protected final Set<OWLPropertyExpression<?, ?>> props;
    private final Class<? extends OWLRestriction<?, ?, ?>> type;
    final Set<OWLRestriction<?, ?, ?>> restrs = new HashSet<OWLRestriction<?, ?, ?>>();

    RestrictionVisitor(OWLReasoner r, OWLPropertyExpression<?, ?> prop,
            Class<? extends OWLRestriction<?, ?, ?>> type) {
        this.r = r;
        this.prop = prop;
        this.type = type;
        props = new HashSet<OWLPropertyExpression<?, ?>>();
        props.add(prop);
        if (prop instanceof OWLObjectProperty) {
            props.addAll(r.getSubObjectProperties((OWLObjectProperty) prop, false)
                    .getFlattened());
        } else if (prop instanceof OWLDataProperty) {
            props.addAll(r.getSubDataProperties((OWLDataProperty) prop, false)
                    .getFlattened());
        }
    }

    private void handleRestriction(OWLRestriction<?, ?, ?> restr) {
        if (type == null || type.isAssignableFrom(restr.getClass())) {
            if (prop == null || restr.getProperty().equals(prop)) {
                restrs.add(restr);
            }
        }
    }

    // flattening the description should be enough
    @Override
    public void visit(OWLObjectIntersectionOf and) {
        for (OWLClassExpression desc : and.getOperands()) {
            desc.accept(this);
        }
    }

    @Override
    public void visit(OWLDataAllValuesFrom restr) {
        handleRestriction(restr);
    }

    @Override
    public void visit(OWLDataSomeValuesFrom restr) {
        handleRestriction(restr);
    }

    @Override
    public void visit(OWLDataHasValue restr) {
        handleRestriction(restr);
    }

    @Override
    public void visit(OWLDataMinCardinality restr) {
        handleRestriction(restr);
    }

    @Override
    public void visit(OWLDataExactCardinality restr) {
        handleRestriction(restr);
    }

    @Override
    public void visit(OWLDataMaxCardinality restr) {
        handleRestriction(restr);
    }

    @Override
    public void visit(OWLObjectAllValuesFrom restr) {
        handleRestriction(restr);
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom restr) {
        handleRestriction(restr);
    }

    @Override
    public void visit(OWLObjectHasValue desc) {
        handleRestriction(desc);
    }

    @Override
    public void visit(OWLObjectMinCardinality desc) {
        handleRestriction(desc);
    }

    @Override
    public void visit(OWLObjectExactCardinality desc) {
        handleRestriction(desc);
    }

    @Override
    public void visit(OWLObjectMaxCardinality desc) {
        handleRestriction(desc);
    }

    @Override
    public void visit(OWLObjectHasSelf desc) {
        handleRestriction(desc);
    }
}
