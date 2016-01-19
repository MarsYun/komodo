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
import java.util.Set;

import org.komodo.spi.query.metadata.QueryMetadataInterface;
import org.teiid.query.metadata.TempMetadataAdapter;
import org.teiid.query.resolver.ProcedureContainerResolver;
import org.teiid.query.resolver.TCQueryResolver;
import org.teiid.query.resolver.util.ResolverVisitorImpl;
import org.teiid.query.sql.lang.CommandImpl;
import org.teiid.query.sql.lang.DeleteImpl;
import org.teiid.query.sql.symbol.GroupSymbolImpl;


/**
 * This class knows how to expand and resolve DELETE commands.
 */
public class DeleteResolver extends ProcedureContainerResolver {

    /**
     * @param queryResolver
     */
    public DeleteResolver(TCQueryResolver queryResolver) {
        super(queryResolver);
    }

    /** 
     * @see org.teiid.query.resolver.ProcedureContainerResolver#resolveProceduralCommand(org.teiid.query.sql.lang.CommandImpl, org.teiid.query.metadata.TempMetadataAdapter)
     */
    @Override
    public void resolveProceduralCommand(CommandImpl command, TempMetadataAdapter metadata) 
        throws Exception {

        //Cast to known type
        DeleteImpl delete = (DeleteImpl) command;

        Set<GroupSymbolImpl> groups = new HashSet<GroupSymbolImpl>();
        groups.add(delete.getGroup());
        getQueryResolver().resolveSubqueries(command, metadata, groups);
        ResolverVisitorImpl visitor = new ResolverVisitorImpl(getTeiidParser().getVersion());
        visitor.resolveLanguageObject(delete, groups, delete.getExternalGroupContexts(), metadata);
    }
    
    /** 
     * @param metadata
     * @param group
     * @return
     * @throws Exception
     * @throws Exception
     */
    @Override
    protected String getPlan(QueryMetadataInterface metadata,
                           GroupSymbolImpl group) throws Exception {
        return metadata.getDeletePlan(group.getMetadataID());
    }
    
}
