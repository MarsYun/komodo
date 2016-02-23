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

import java.util.Arrays;
import java.util.List;
import org.komodo.modeshape.teiid.parser.TeiidSeqParser;
import org.komodo.modeshape.teiid.parser.SQLanguageVisitorImpl;
import org.komodo.modeshape.teiid.sql.lang.ASTNode;
import org.komodo.modeshape.teiid.sql.symbol.FunctionImpl.SQFunctionDescriptor;
import org.komodo.spi.lexicon.TeiidSqlLexicon;
import org.komodo.spi.query.sql.symbol.Function;
import org.komodo.spi.type.DataTypeManager.DataTypeName;
import org.komodo.spi.udf.FunctionDescriptor;

/**
 *
 */
public class FunctionImpl extends ASTNode implements BaseExpression, Function<SQFunctionDescriptor, SQLanguageVisitorImpl> {

    public static class SQFunctionDescriptor implements FunctionDescriptor {

        @Override
        public String getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<?> getReturnType() {
            throw new UnsupportedOperationException();
        }

        public Object getMethod() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    public FunctionImpl(TeiidSeqParser p, int id) {
        super(p, id);
    }

    @Override
    public String getName() {
        Object property = getProperty(TeiidSqlLexicon.Function.NAME_PROP_NAME);
        return property == null ? null : property.toString();
    }

    public void setName(String name) {
        setProperty(TeiidSqlLexicon.Function.NAME_PROP_NAME, name);
    }

    @Override
    public Class<?> getType() {
        return convertTypeClassPropertyToClass(TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME);
    }

    protected void setArrayType(boolean isArrayType) {
        setProperty(TeiidSqlLexicon.Function.ARRAY_TYPE_PROP_NAME, isArrayType);
    }

    protected void assignDataTypeName(DataTypeName dataTypeName, boolean isArray) {
        if (dataTypeName == null)
            return;

        setProperty(TeiidSqlLexicon.Expression.TYPE_CLASS_PROP_NAME, dataTypeName.name());
        setArrayType(isArray);
    }

    @Override
    public void setType(Class<?> type) {
        DataTypeName dataType = getDataTypeService().retrieveDataTypeName(type);
        assignDataTypeName(dataType, false);
    }

    public List<BaseExpression> getArguments() {
        return getChildrenforIdentifierAndRefType(
                                                  TeiidSqlLexicon.Function.ARGS_REF_NAME, BaseExpression.class);
    }
    @Override
    public BaseExpression[] getArgs() {
        List<BaseExpression> arguments = getArguments();
        if (arguments == null)
            return new BaseExpression[0];

        return arguments.toArray(new BaseExpression[0]);
    }

    @Override
    public BaseExpression getArg(int index) {
        List<BaseExpression> args = getArguments();
        if (index < 0 || index >= args.size())
            return null;

        return args.get(index);
    }

    public void setArgs(BaseExpression[] args) {
        removeChildren(TeiidSqlLexicon.Function.ARGS_REF_NAME);

        if (args == null) {
            setChildren(TeiidSqlLexicon.Function.ARGS_REF_NAME, null);
            return;
        }

        for (BaseExpression arg : args) {
            addLastChild(TeiidSqlLexicon.Function.ARGS_REF_NAME, arg);
        }
    }

    @Override
    public boolean isImplicit() {
        Object property = getProperty(TeiidSqlLexicon.Function.IMPLICIT_PROP_NAME);
        return property == null ? false : Boolean.parseBoolean(property.toString());
    }

    public void setImplicit(boolean implicit) {
        setProperty(TeiidSqlLexicon.Function.IMPLICIT_PROP_NAME, implicit);
    }

    @Override
    public SQFunctionDescriptor getFunctionDescriptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFunctionDescriptor(SQFunctionDescriptor fd) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.getName() == null) ? 0 : this.getName().toUpperCase().hashCode());
        if(this.getArgs() != null && this.getArgs().length > 0 && this.getArgs()[0] != null) {
            result = prime * result + Arrays.hashCode(this.getArgs());
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        FunctionImpl other = (FunctionImpl)obj;

//        if (this.getFunctionDescriptor() != null && other.getFunctionDescriptor() != null) {
//            if (!this.getFunctionDescriptor().getMethod().equals(other.getFunctionDescriptor().getMethod())) {
//                return false;
//            }
//        }

        if (this.getName() == null) {
            if (other.getName() != null) return false;
        } else if (!this.getName().equalsIgnoreCase(other.getName())) return false;

        if (this.isImplicit() != other.isImplicit()) return false;

        if (!Arrays.equals(this.getArgs(), other.getArgs())) return false;

        return true;
    }

    @Override
    public void acceptVisitor(SQLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @Override
    public FunctionImpl clone() {
        FunctionImpl clone = new FunctionImpl(this.getTeiidParser(), this.getId());

        if(getArgs() != null) {
            BaseExpression[] cloned = new BaseExpression[getArgs().length];
            for (int i = 0; i < getArgs().length; ++i) {
                cloned[i] = getArgs()[i].clone();
            }
            clone.setArgs(cloned);
        }
        if(getType() != null)
            clone.setType(getType());
        if(getName() != null)
            clone.setName(getName());

        return clone;
    }

}
