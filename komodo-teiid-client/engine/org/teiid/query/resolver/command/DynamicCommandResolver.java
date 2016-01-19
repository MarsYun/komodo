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

package org.teiid.query.resolver.command;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.teiid.api.exception.query.QueryResolverException;
import org.teiid.core.types.DefaultDataTypeManager;
import org.teiid.query.metadata.TempMetadataAdapter;
import org.teiid.query.metadata.TempMetadataID;
import org.teiid.query.parser.TeiidNodeFactory.ASTNodes;
import org.teiid.query.resolver.CommandResolver;
import org.teiid.query.resolver.TCQueryResolver;
import org.teiid.query.resolver.util.ResolverUtil;
import org.teiid.query.resolver.util.ResolverVisitorImpl;
import org.teiid.query.sql.ProcedureReservedWords;
import org.teiid.query.sql.lang.CommandImpl;
import org.teiid.query.sql.lang.DynamicCommandImpl;
import org.teiid.query.sql.lang.SetClauseImpl;
import org.teiid.query.sql.symbol.ElementSymbolImpl;
import org.teiid.query.sql.symbol.GroupSymbolImpl;
import org.teiid.runtime.client.Messages;


/**
 *
 */
public class DynamicCommandResolver extends CommandResolver {

    /**
     * @param queryResolver
     */
    public DynamicCommandResolver(TCQueryResolver queryResolver) {
        super(queryResolver);
    }

    /** 
     * @see org.teiid.query.resolver.CommandResolver#resolveCommand(org.teiid.query.sql.lang.CommandImpl, TempMetadataAdapter, boolean)
     */
    @Override
    public void resolveCommand(CommandImpl command, TempMetadataAdapter metadata, boolean resolveNullLiterals) 
        throws Exception {

        DynamicCommandImpl dynamicCmd = (DynamicCommandImpl)command;
        
        Iterator columns = dynamicCmd.getAsColumns().iterator();

        Set<GroupSymbolImpl> groups = new HashSet<GroupSymbolImpl>();
        
        //if there is no into group, just create temp metadata ids
        if (dynamicCmd.getIntoGroup() == null) {
            while (columns.hasNext()) {
                ElementSymbolImpl column = (ElementSymbolImpl)columns.next();
                column.setMetadataID(new TempMetadataID(column.getShortName(), column.getType()));
            }
        } else if (dynamicCmd.getIntoGroup().isTempGroupSymbol()) {
            while (columns.hasNext()) {
                ElementSymbolImpl column = (ElementSymbolImpl)columns.next();
                GroupSymbolImpl gs = getTeiidParser().createASTNode(ASTNodes.GROUP_SYMBOL);
                gs.setName(dynamicCmd.getIntoGroup().getName());
                column.setGroupSymbol(gs);
            }
        }
        
        ResolverVisitorImpl visitor = new ResolverVisitorImpl(getTeiidParser().getVersion());
        visitor.resolveLanguageObject(dynamicCmd, groups, dynamicCmd.getExternalGroupContexts(), metadata);
        String sqlType = getDataTypeManager().getDataTypeName(dynamicCmd.getSql().getType());
        String targetType = DefaultDataTypeManager.DefaultDataTypes.STRING.getId();
        
        if (!targetType.equals(sqlType) && !getDataTypeManager().isImplicitConversion(sqlType, targetType)) {
             throw new QueryResolverException(Messages.gs(Messages.TEIID.TEIID30100, sqlType));
        }
        
        if (dynamicCmd.getUsing() != null && !dynamicCmd.getUsing().isEmpty()) {
            for (SetClauseImpl clause : dynamicCmd.getUsing().getClauses()) {
                ElementSymbolImpl id = clause.getSymbol();
                GroupSymbolImpl gs = getTeiidParser().createASTNode(ASTNodes.GROUP_SYMBOL);
                gs.setName(ProcedureReservedWords.DVARS);
                id.setGroupSymbol(gs);
                id.setType(clause.getValue().getType());
                id.setMetadataID(new TempMetadataID(id.getName(), id.getType()));
            }
        }
        
        GroupSymbolImpl intoSymbol = dynamicCmd.getIntoGroup();
        if (intoSymbol != null) {
            if (!intoSymbol.isImplicitTempGroupSymbol()) {
                ResolverUtil.resolveGroup(intoSymbol, metadata);
            } else {
                List symbols = dynamicCmd.getAsColumns();
                ResolverUtil.resolveImplicitTempGroup(metadata, intoSymbol, symbols);
            }
        }
    }
}
