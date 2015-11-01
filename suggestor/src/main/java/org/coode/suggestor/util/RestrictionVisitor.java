package org.coode.suggestor.util;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.add;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author Nick Drummond, The University Of Manchester, Bio Health Informatics
 *         Group, Date: Jul 12, 2011
 */
class RestrictionVisitor implements OWLClassExpressionVisitor {

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
            add(props, r.getSubObjectProperties(prop.asOWLObjectProperty()).entities());
        } else if (prop instanceof OWLDataProperty) {
            add(props, r.getSubDataProperties(prop.asOWLDataProperty()).entities());
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
        and.operands().forEach(d -> d.accept(this));
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
