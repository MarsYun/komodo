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

import java.util.List;
import org.komodo.modeshape.teiid.cnd.TeiidSqlLexicon;
import org.komodo.modeshape.teiid.parser.SQLanguageVisitorImpl;
import org.komodo.modeshape.teiid.parser.TeiidSeqParser;
import org.komodo.modeshape.teiid.sql.lang.ASTNode;
import org.komodo.spi.query.sql.symbol.JSONObject;
import org.komodo.spi.type.DataTypeManager.DataTypeName;

/**
 *
 */
public class JSONObjectImpl extends ASTNode implements BaseExpression, JSONObject<SQLanguageVisitorImpl> {

    /**
     * @param p teiid parser
     * @param id node type id
     */
    public JSONObjectImpl(TeiidSeqParser p, int id) {
        super(p, id);
        setProperty(TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, DataTypeName.CLOB.name());
    }

    @Override
    public Class<?> getType() {
        return convertTypeClassPropertyToClass(TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME);
    }

    public List<DerivedColumnImpl> getArgs() {
        return getChildrenforIdentifierAndRefType(
                                                  TeiidSqlLexicon.JSONObject.ARGS_REF_NAME, DerivedColumnImpl.class);
    }

    public void setArgs(List<DerivedColumnImpl> args) {
        setChildren(TeiidSqlLexicon.JSONObject.ARGS_REF_NAME, args);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.getArgs() == null) ? 0 : this.getArgs().hashCode());
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
        JSONObjectImpl other = (JSONObjectImpl)obj;
        if (this.getArgs() == null) {
            if (other.getArgs() != null)
                return false;
        } else if (!this.getArgs().equals(other.getArgs()))
            return false;
        return true;
    }

    @Override
    public void acceptVisitor(SQLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @Override
    public JSONObjectImpl clone() {
        JSONObjectImpl clone = new JSONObjectImpl(this.getTeiidParser(), this.getId());

        if (getArgs() != null)
            clone.setArgs(cloneList(getArgs()));

        return clone;
    }

}
