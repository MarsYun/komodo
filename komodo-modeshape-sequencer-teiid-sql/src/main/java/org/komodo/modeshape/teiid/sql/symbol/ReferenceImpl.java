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

package org.komodo.modeshape.teiid.sql.symbol;

import org.komodo.modeshape.teiid.parser.SQLanguageVisitorImpl;
import org.komodo.modeshape.teiid.parser.TeiidSeqParser;
import org.komodo.modeshape.teiid.sql.lang.ASTNode;
import org.komodo.spi.lexicon.TeiidSqlLexicon;
import org.komodo.spi.query.sql.symbol.Reference;
import org.komodo.spi.type.DataTypeManager.DataTypeName;

/**
 *
 */
public class ReferenceImpl extends ASTNode implements BaseExpression, Reference<SQLanguageVisitorImpl> {

    /**
     * @param p teiid parser
     * @param id node type id
     */
    public ReferenceImpl(TeiidSeqParser p, int id) {
        super(p, id);
    }

    @Override
    public Class<?> getType() {
        return convertTypeClassPropertyToClass(TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME);
    }

    public void setType(Class<?> type) {
        DataTypeName dataTypeName = getDataTypeService().retrieveDataTypeName(type);
        setProperty(TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, dataTypeName.name());
    }

    @Override
    public boolean isPositional() {
        Object property = getProperty(TeiidSqlLexicon.Reference.POSITIONAL_PROP_NAME);
        return property == null ? false : Boolean.parseBoolean(property.toString());
    }

    public void setPositional(boolean positional) {
        setProperty(TeiidSqlLexicon.Reference.POSITIONAL_PROP_NAME, positional);
    }

    @Override
    public ElementSymbolImpl getExpression() {
        return getChildforIdentifierAndRefType(
                                               TeiidSqlLexicon.Reference.EXPRESSION_REF_NAME, ElementSymbolImpl.class);
    }

    public void setExpression(ElementSymbolImpl elementSymbol) {
        setChild(TeiidSqlLexicon.Reference.EXPRESSION_REF_NAME, elementSymbol);
        setType(elementSymbol.getType());
    }

    public int getIndex() {
        Object property = getProperty(TeiidSqlLexicon.Reference.INDEX_PROP_NAME);
        return property == null ? 0 : Integer.parseInt(property.toString());
    }

    public void setIndex(int referenceIndex) {
        setProperty(TeiidSqlLexicon.Reference.INDEX_PROP_NAME, referenceIndex);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.getExpression() == null) ? 0 : this.getExpression().hashCode());
        result = prime * result + (this.isPositional() ? 1231 : 1237);
//        result = prime * result + this.getRefIndex();
        result = prime * result + ((this.getType() == null) ? 0 : this.getType().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        ReferenceImpl other = (ReferenceImpl)obj;
        if (this.getExpression() == null) {
            if (other.getExpression() != null) return false;
        } else if (!this.getExpression().equals(other.getExpression())) return false;
        if (this.isPositional() != other.isPositional()) return false;
//        if (this.getRefIndex() != other.getRefIndex()) return false;
        if (this.getType() == null) {
            if (other.getType() != null) return false;
        } else if (!this.getType().equals(other.getType())) return false;
        return true;
    }

    @Override
    public void acceptVisitor(SQLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @Override
    public ReferenceImpl clone() {
        ReferenceImpl clone = new ReferenceImpl(this.getTeiidParser(), this.getId());

        if(getExpression() != null)
            clone.setExpression(getExpression().clone());
        if(this.getType() != null)
            clone.setType(this.getType());
        clone.setPositional(isPositional());
        clone.setIndex(getIndex());
//        clone.setConstraint(clone.getConstraint());
        
        return clone;
    }

}
