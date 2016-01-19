/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */

package org.komodo.modeshape.teiid.sql.lang;

import org.komodo.modeshape.teiid.cnd.TeiidSqlLexicon;
import org.komodo.modeshape.teiid.parser.SQLanguageVisitorImpl;
import org.komodo.modeshape.teiid.parser.TeiidSeqParser;
import org.komodo.modeshape.teiid.sql.symbol.BaseExpression;
import org.komodo.spi.query.sql.lang.SubquerySetCriteria;
import org.komodo.modeshape.teiid.parser.TeiidNodeFactory.ASTNodes;

/**
 *
 */
public class SubquerySetCriteriaImpl extends AbstractSetCriteria
    implements BaseSubqueryContainer<QueryCommandImpl>, SubquerySetCriteria<BaseExpression, SQLanguageVisitorImpl, QueryCommandImpl>{

    /**
     * @param p teiid parser
     * @param id node type id
     */
    public SubquerySetCriteriaImpl(TeiidSeqParser p, int id) {
        super(p, id);

        SubqueryHint subqueryHint = getTeiidParser().createASTNode(ASTNodes.SUBQUERY_HINT);
        setSubqueryHint(subqueryHint);
    }

    public SubqueryHint getSubqueryHint() {
        return getChildforIdentifierAndRefType(TeiidSqlLexicon.SubquerySetCriteria.SUBQUERY_HINT_REF_NAME, SubqueryHint.class);
    }

    public void setSubqueryHint(SubqueryHint hint) {
        setChild(TeiidSqlLexicon.SubquerySetCriteria.SUBQUERY_HINT_REF_NAME, hint);
    }

    @Override
    public QueryCommandImpl getCommand() {
        return getChildforIdentifierAndRefType(
                                               TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, QueryCommandImpl.class);
    }

    @Override
    public void setCommand(QueryCommandImpl command) {
        setChild(TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, command);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getCommand() == null) ? 0 : this.getCommand().hashCode());
        result = prime * result + ((this.getSubqueryHint() == null) ? 0 : this.getSubqueryHint().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SubquerySetCriteriaImpl other = (SubquerySetCriteriaImpl)obj;
        if (this.getCommand() == null) {
            if (other.getCommand() != null) return false;
        } else if (!this.getCommand().equals(other.getCommand())) return false;
        if (this.getSubqueryHint() == null) {
            if (other.getSubqueryHint() != null) return false;
        } else if (!this.getSubqueryHint().equals(other.getSubqueryHint())) return false;
        return true;
    }

    @Override
    public void acceptVisitor(SQLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @Override
    public SubquerySetCriteriaImpl clone() {
        SubquerySetCriteriaImpl clone = new SubquerySetCriteriaImpl(this.getTeiidParser(), this.getId());
    
        if(getCommand() != null)
            clone.setCommand(getCommand().clone());
        if(getSubqueryHint() != null)
            clone.setSubqueryHint(getSubqueryHint().clone());
        if(getExpression() != null)
            clone.setExpression(getExpression().clone());
        clone.setNegated(isNegated());
    
        return clone;
    }

}
