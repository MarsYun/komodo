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

package org.teiid.query.mapping.xml;

import java.util.HashMap;
import java.util.Map;

import org.komodo.spi.query.QueryFactory;
import org.teiid.core.util.ArgCheck;
import org.teiid.query.parser.TeiidNodeFactory.ASTNodes;
import org.teiid.query.parser.TeiidClientParser;
import org.teiid.query.sql.symbol.ElementSymbolImpl;
import org.teiid.query.sql.symbol.GroupSymbolImpl;


/** 
 * This represents a source node. A source node is which produces results from
 * executing a relational query.
 */
public class MappingSourceNodeImpl extends MappingBaseNodeImpl {
    
    private transient ResultSetInfo resultSetInfo;
    private Map symbolMap = new HashMap();
    
    protected MappingSourceNodeImpl(TeiidClientParser teiidParser) {
        super(teiidParser);
    }
    
    public MappingSourceNodeImpl(TeiidClientParser teiidParser, String source) {
        this(teiidParser);
        setProperty(MappingNodeConstants.Properties.NODE_TYPE, MappingNodeConstants.SOURCE);
        setSource(source);
    }
        
    public void acceptVisitor(MappingVisitor visitor) {
        visitor.visit(this);
    }

    public String getResultName() {
        return (String) getProperty(MappingNodeConstants.Properties.RESULT_SET_NAME);
    }
    
    public String getAliasResultName() {
        return (String) getProperty(MappingNodeConstants.Properties.ALIAS_RESULT_SET_NAME);
    }    
    
    /**
     * in the case of recursive node we need to know the original source node name; this represents
     * that name.
     * @param alias
     */
    public void setAliasResultName(String alias) {
        setProperty(MappingNodeConstants.Properties.ALIAS_RESULT_SET_NAME, alias);
    }    
    
    public void setSource(String source) {
        if (source != null) {
            setProperty(MappingNodeConstants.Properties.RESULT_SET_NAME, source);
        }
    }        
    
    public MappingSourceNodeImpl getSourceNode() {
        return this;
    }
    
    public boolean isRootSourceNode() {
        return getParentSourceNode() == null;
    }
    
    public MappingSourceNodeImpl getParentSourceNode() {
        MappingBaseNodeImpl parent = getParentNode();
        while (parent != null) {
            if (parent instanceof MappingSourceNodeImpl) {
                return (MappingSourceNodeImpl)parent;
            }
            parent = parent.getParentNode();
        }
        return null;
    }
    
    public ResultSetInfo getResultSetInfo() {
        if (this.resultSetInfo == null) {
            this.resultSetInfo = new ResultSetInfo(getActualResultSetName());
            setProperty(MappingNodeConstants.Properties.RESULT_SET_INFO, this.resultSetInfo);
        }
        return this.resultSetInfo;
    }

    public void setResultSetInfo(ResultSetInfo resultSetInfo) {
        this.resultSetInfo = resultSetInfo;        
    }
    
    public String toString() {
        return "[" + getProperty(MappingNodeConstants.Properties.NODE_TYPE) + "]" //$NON-NLS-1$ //$NON-NLS-2$ 
                + getProperty(MappingNodeConstants.Properties.RESULT_SET_NAME);
    }
    
    public Map getSymbolMap() {
        return this.symbolMap;
    }

    public void setSymbolMap(Map symbolMap) {
        this.symbolMap = symbolMap;
        
        updateSymbolMapDependentValues();        
    }

    public void updateSymbolMapDependentValues() {
        // based on the symbol map modify the getalias name
        if (getAliasResultName() != null) {
            GroupSymbolImpl groupSymbol = getTeiidParser().createASTNode(ASTNodes.GROUP_SYMBOL);
            groupSymbol.setName(getActualResultSetName());

            GroupSymbolImpl newGroup = getMappedSymbol(groupSymbol);
            setAliasResultName(newGroup.getName());
        }
        
        ElementSymbolImpl mappingClassSymbol = this.getResultSetInfo().getMappingClassSymbol();
        
        if (mappingClassSymbol != null) {
            this.getResultSetInfo().setMappingClassSymbol(getMappedSymbol(mappingClassSymbol));
        }
    }
    
    public String getActualResultSetName() {
        GroupSymbolImpl groupSymbol = getTeiidParser().createASTNode(ASTNodes.GROUP_SYMBOL);
        groupSymbol.setName(getResultName());

        GroupSymbolImpl group = getMappedSymbol(groupSymbol);
        return group.getName();
    }
    
    public Map buildFullSymbolMap() {
        HashMap map = new HashMap();
        MappingSourceNodeImpl sourceNode = this;
        
        while(sourceNode != null) {
            map.putAll(sourceNode.getSymbolMap());
            sourceNode = sourceNode.getParentSourceNode();
        }
        return map;
    }
    
    public ElementSymbolImpl getMappedSymbol(ElementSymbolImpl symbol) {
        ElementSymbolImpl mappedSymbol = (ElementSymbolImpl)symbolMap.get(symbol);
        if (mappedSymbol == null) {
            ArgCheck.isTrue(symbol.getGroupSymbol() == null || !symbolMap.containsKey(symbol.getGroupSymbol()), "invalid symbol " + symbol); //$NON-NLS-1$

            MappingSourceNodeImpl parentSourceNode = getParentSourceNode();
            if (parentSourceNode != null) {
                return parentSourceNode.getMappedSymbol(symbol);
            }
        }
        
        if (mappedSymbol == null) {
            return symbol;
        }
        
        return mappedSymbol;
    }

    public GroupSymbolImpl getMappedSymbol(GroupSymbolImpl symbol) {
        GroupSymbolImpl mappedSymbol = (GroupSymbolImpl)symbolMap.get(symbol);
        if (mappedSymbol == null) {
            MappingSourceNodeImpl parentSourceNode = getParentSourceNode();
            if (parentSourceNode != null) {
                return parentSourceNode.getMappedSymbol(symbol);
            }
        }
        
        if (mappedSymbol == null) {
            return symbol;
        }
        
        return mappedSymbol;
    }    
}
