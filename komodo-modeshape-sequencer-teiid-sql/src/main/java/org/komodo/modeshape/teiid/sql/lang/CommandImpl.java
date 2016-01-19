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

import java.util.List;
import org.komodo.modeshape.teiid.cnd.TeiidSqlLexicon;
import org.komodo.modeshape.teiid.parser.TeiidSeqParser;
import org.komodo.modeshape.teiid.parser.SQLanguageVisitorImpl;
import org.komodo.modeshape.teiid.sql.symbol.BaseExpression;
import org.komodo.spi.query.sql.lang.Command;

/**
 *
 */
public abstract class CommandImpl extends ASTNode implements Command<BaseExpression, SQLanguageVisitorImpl> {

    /**
     * @param p teiid parser
     * @param id node type id
     */
    public CommandImpl(TeiidSeqParser p, int id) {
        super(p, id);
    }

    @Override
    public int getType() {
        Object property = getProperty(TeiidSqlLexicon.Command.TYPE_PROP_NAME);
        return property == null ? -1 : Integer.parseInt(property.toString());
    }

    protected void setType(int typeId) {
        setProperty(TeiidSqlLexicon.Command.TYPE_PROP_NAME, typeId);
    }

    /**
     * @return true if command has been resolved
     */
    @Override
    public boolean isResolved() {
        Object property = getProperty(TeiidSqlLexicon.Command.IS_RESOLVED_PROP_NAME);
        return property == null ? false : Boolean.parseBoolean(property.toString());
    }

    /**
     * This command is intended to only be used by the QueryResolver.
     * @param isResolved whether this command is resolved or not
     */
    public void setIsResolved(boolean isResolved) {
        setProperty(TeiidSqlLexicon.Command.IS_RESOLVED_PROP_NAME, isResolved);
    }

    /**
     * @return null if unknown, empty if results are not returned, or the resultset columns
     */
    @Override
    public List<? extends BaseExpression> getResultSetColumns() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the option clause for the query.
     * @return option clause
     */
    @Override
    public OptionImpl getOption() {
        return getChildforIdentifierAndRefType(TeiidSqlLexicon.Command.OPTION_REF_NAME, OptionImpl.class);
    }

    /**
     * Set the option clause for the query.
     * @param option New option clause
     */
    public void setOption(OptionImpl option) {
        setChild(TeiidSqlLexicon.Command.OPTION_REF_NAME, option);
    }

    /**
     * @return the sourceHint
     */
    public SourceHintImpl getSourceHint() {
        return getChildforIdentifierAndRefType(TeiidSqlLexicon.Command.SOURCE_HINT_REF_NAME, SourceHintImpl.class);
    }

    /**
     * @param sourceHint the sourceHint to set
     */
    public void setSourceHint(SourceHintImpl sourceHint) {
        setChild(TeiidSqlLexicon.Command.SOURCE_HINT_REF_NAME, sourceHint);
    }

    /**
     * @return whether this object returns a result set
     */
    public boolean returnsResultSet() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return singleton update symbol which is lazily created
     */
    public List<BaseExpression> getUpdateCommandSymbol() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (this.isResolved() ? 1231 : 1237);
        result = prime * result + ((this.getOption() == null) ? 0 : this.getOption().hashCode());
        result = prime * result + ((this.getSourceHint() == null) ? 0 : this.getSourceHint().hashCode());
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
        CommandImpl other = (CommandImpl)obj;
        if (this.isResolved() != other.isResolved())
            return false;
        if (this.getOption() == null) {
            if (other.getOption() != null)
                return false;
        } else if (!this.getOption().equals(other.getOption()))
            return false;
        if (this.getSourceHint() == null) {
            if (other.getSourceHint() != null)
                return false;
        } else if (!this.getSourceHint().equals(other.getSourceHint()))
            return false;
        return true;
    }

    /**
     * @param clone
     */
    protected void copyMetadataState(CommandImpl clone) {
        //TODO To be implemented if required
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(SQLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @Override
    public abstract CommandImpl clone();
}
