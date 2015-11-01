package org.coode.suggestor.util;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

/**
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics
 *         Group, Date: Jul 12, 2011
 */
class RestrictionVisitor extends OWLClassExpressionVisitorAdapter {

    protected final OWLReasoner r;
    protected final @Nullable OWLPropertyExpression prop;
    protected final Set<OWLPropertyExpression> props;
    private final @Nullable Class<? extends OWLRestriction> type;
    final Set<OWLRestriction> restrs = new HashSet<>();

    RestrictionVisitor(OWLReasoner r, @Nullable OWLPropertyExpression prop,
        @Nullable Class<? extends OWLRestriction> type) {
        this.r = r;
        this.prop = prop;
        this.type = type;
        props = new HashSet<>();
        props.add(prop);
        if (prop instanceof OWLObjectProperty) {
            props.addAll(r.getSubObjectProperties((OWLObjectProperty) prop,
                false).getFlattened());
        } else if (prop instanceof OWLDataProperty) {
            props.addAll(r.getSubDataProperties((OWLDataProperty) prop, false)
                .getFlattened());
        }
    }

    private void handleRestriction(OWLDataRestriction restr) {
        if (type == null || type.isAssignableFrom(restr.getClass())) {
            if (prop == null || restr.getProperty().equals(prop)) {
                restrs.add(restr);
            }
        }
    }

    private void handleRestriction(OWLObjectRestriction restr) {
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
