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

package org.teiid.query.resolver;

import org.komodo.spi.runtime.version.TeiidVersion;
import org.teiid.core.types.DefaultDataTypeManager;
import org.teiid.query.metadata.TempMetadataAdapter;
import org.teiid.query.parser.TeiidNodeFactory.ASTNodes;
import org.teiid.query.parser.TeiidClientParser;
import org.teiid.query.sql.lang.CommandImpl;
import org.teiid.query.sql.lang.BaseLanguageObject;


/**
 * This is the interface that is implemented for each type of command, telling how 
 * to resolve that command.  
 */
public abstract class CommandResolver {

    private final TCQueryResolver queryResolver;

    private DefaultDataTypeManager dataTypeManager;

    /**
     * @param queryResolver
     */
    public CommandResolver(TCQueryResolver queryResolver) {
        this.queryResolver = queryResolver;
    }

    protected TCQueryResolver getQueryResolver() {
        return this.queryResolver;
    }

    protected TeiidVersion getTeiidVersion() {
        return getQueryResolver().getTeiidVersion();
    }

    protected TeiidClientParser getTeiidParser() {
        return this.queryResolver.getQueryParser().getTeiidParser();
    }

    protected <T extends BaseLanguageObject> T create(ASTNodes type) {
        return getTeiidParser().createASTNode(type);
    }

    protected DefaultDataTypeManager getDataTypeManager() {
        if (dataTypeManager == null)
            dataTypeManager = DefaultDataTypeManager.getInstance(getTeiidVersion());

        return dataTypeManager;
    }

    /**
     * Resolve the command using the metadata.  
     * @param command The command to resolve
     * @param metadata Metadata
     * @param resolveNullLiterals true if the resolver should consider replacing null literals with more appropriate types 
     * @return the TempMetadataStore containing the metadata defined by this command
     * @throws Exception If there is a metadata problem
     * @throws Exception If the query cannot be resolved
     * @throws Exception If there is an internal error     
     */        
    public abstract void resolveCommand(CommandImpl command, TempMetadataAdapter metadata, boolean resolveNullLiterals)
    throws Exception;
    
}
