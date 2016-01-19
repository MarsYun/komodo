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
import org.komodo.modeshape.teiid.parser.TeiidSeqParser;
import org.komodo.modeshape.teiid.parser.SQLanguageVisitorImpl;
import org.komodo.modeshape.teiid.parser.TeiidNodeFactory.ASTNodes;
import org.komodo.spi.query.sql.lang.ExistsCriteria;

/**
 * Criteria node for exists keyword
 */
public class ExistsCriteriaImpl extends CriteriaImpl implements PredicateCriteria, BaseSubqueryContainer<QueryCommandImpl>, ExistsCriteria<SQLanguageVisitorImpl, QueryCommandImpl> {

    /**
     * @param p parent parser
     * @param id type id of node
     */
    public ExistsCriteriaImpl(TeiidSeqParser p, int id) {
        super(p, id);

        SubqueryHint subqueryHint = getTeiidParser().createASTNode(ASTNodes.SUBQUERY_HINT);
        setSubqueryHint(subqueryHint);
    }

    @Override
    public boolean isNegated() {
        Object property = getProperty(TeiidSqlLexicon.ExistsCriteria.NEGATED_PROP_NAME);
        return property == null ? false : Boolean.parseBoolean(property.toString());
    }

    @Override
    public void setNegated(boolean value) {
        setProperty(TeiidSqlLexicon.ExistsCriteria.NEGATED_PROP_NAME, value);
    }

    @Override
    public QueryCommandImpl getCommand() {
        return getChildforIdentifierAndRefType(TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, QueryCommandImpl.class);
    }

    @Override
    public void setCommand(QueryCommandImpl command) {
        setChild(TeiidSqlLexicon.SubqueryContainer.COMMAND_REF_NAME, command);
    }

    /**
     * @return subquery hint
     */
    public SubqueryHint getSubqueryHint() {
        return getChildforIdentifierAndRefType(TeiidSqlLexicon.ExistsCriteria.SUBQUERY_HINT_REF_NAME, SubqueryHint.class);
    }

    /**
     * @param hint value
     */
    public void setSubqueryHint(SubqueryHint hint) {
        setChild(TeiidSqlLexicon.ExistsCriteria.SUBQUERY_HINT_REF_NAME, hint);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.getCommand() == null) ? 0 : this.getCommand().hashCode());
        result = prime * result + (this.isNegated() ? 1231 : 1237);
        result = prime * result + ((this.getSubqueryHint() == null) ? 0 : this.getSubqueryHint().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExistsCriteriaImpl other = (ExistsCriteriaImpl)obj;
        if (this.getCommand() == null) {
            if (other.getCommand() != null)
                return false;
        } else if (!this.getCommand().equals(other.getCommand()))
            return false;
        if (this.isNegated() != other.isNegated())
            return false;
        if (this.getSubqueryHint() == null) {
            if (other.getSubqueryHint() != null)
                return false;
        } else if (!this.getSubqueryHint().equals(other.getSubqueryHint()))
            return false;
        return true;
    }

    @Override
    public void acceptVisitor(SQLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @Override
    public ExistsCriteriaImpl clone() {
        ExistsCriteriaImpl clone = new ExistsCriteriaImpl(this.getTeiidParser(), this.getId());

        if (getCommand() != null)
            clone.setCommand(getCommand().clone());
        clone.setNegated(isNegated());
        if (getSubqueryHint() != null)
            clone.setSubqueryHint(getSubqueryHint().clone());

        return clone;
    }

}
