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
import org.komodo.modeshape.teiid.parser.SQLanguageVisitorImpl;
import org.komodo.modeshape.teiid.parser.TeiidSeqParser;
import org.komodo.modeshape.teiid.sql.symbol.BaseExpression;
import org.komodo.spi.lexicon.TeiidSqlLexicon;
import org.komodo.spi.query.sql.lang.OrderBy;
import org.komodo.modeshape.teiid.parser.TeiidNodeFactory.ASTNodes;

/**
 *
 */
public class OrderByImpl extends ASTNode implements OrderBy<BaseExpression, OrderByItemImpl, SQLanguageVisitorImpl> {

    /**
     * @param p teiid parser
     * @param id node type id
     */
    public OrderByImpl(TeiidSeqParser p, int id) {
        super(p, id);
    }

    @Override
    public int getVariableCount() {
        return 0;
    }

    public BaseExpression getVariable(int index) {
        return getOrderByItems().get(index).getSymbol();
    }

    @Override
    public void addVariable(BaseExpression expression) {
        addVariable(expression, true);
    }

    /**
     * Adds a new variable to the list of order by elements with the
     * specified sort order
     *
     * @param expression Expression to add
     * @param type True for ascending, false for descending
     */
    @Override
    public void addVariable(BaseExpression expression, boolean orderType) {
        OrderByItemImpl orderByItem = getTeiidParser().createASTNode(ASTNodes.ORDER_BY_ITEM);
        orderByItem.setSymbol(expression);
        orderByItem.setAscending(orderType);
        addOrderByItem(orderByItem);
    }

    @Override
    public List<OrderByItemImpl> getOrderByItems() {
        return getChildrenforIdentifierAndRefType(
                                                  TeiidSqlLexicon.OrderBy.ORDER_BY_ITEMS_REF_NAME, OrderByItemImpl.class);
    }

    public void addOrderByItem(OrderByItemImpl item) {
        addLastChild(TeiidSqlLexicon.OrderBy.ORDER_BY_ITEMS_REF_NAME, item);
    }

    public void setOrderByItems(List<OrderByItemImpl> items) {
        setChildren(TeiidSqlLexicon.OrderBy.ORDER_BY_ITEMS_REF_NAME, items);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.getOrderByItems() == null) ? 0 : this.getOrderByItems().hashCode());
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
        OrderByImpl other = (OrderByImpl)obj;
        if (this.getOrderByItems() == null) {
            if (other.getOrderByItems() != null)
                return false;
        } else if (!this.getOrderByItems().equals(other.getOrderByItems()))
            return false;
        return true;
    }

    @Override
    public void acceptVisitor(SQLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @Override
    public OrderByImpl clone() {
        OrderByImpl clone = new OrderByImpl(this.getTeiidParser(), this.getId());
        clone.setOrderByItems(cloneList(getOrderByItems()));
        return clone;
    }

}
