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

package org.teiid.query.sql.visitor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.komodo.spi.query.metadata.QueryMetadataInterface;
import org.komodo.spi.runtime.version.TeiidVersion;
import org.teiid.api.exception.query.QueryResolverException;
import org.teiid.query.parser.TCLanguageVisitorImpl;
import org.teiid.query.resolver.util.ResolverVisitorImpl;
import org.teiid.query.sql.lang.CompareCriteriaImpl;
import org.teiid.query.sql.lang.CriteriaSelectorImpl;
import org.teiid.query.sql.lang.BaseLanguageObject;
import org.teiid.query.sql.lang.TranslateCriteriaImpl;
import org.teiid.query.sql.navigator.DeepPreOrderNavigator;
import org.teiid.query.sql.symbol.ElementSymbolImpl;
import org.teiid.query.sql.symbol.GroupSymbolImpl;


/**
 */
public class ResolveVirtualGroupCriteriaVisitor extends TCLanguageVisitorImpl {

    private List virtualGroup;

    private QueryMetadataInterface metadata;

    /**
     * Constructor for ResolveElementsVisitor with no specified groups.  In this
     * case every element's group will be looked up based on the group name.
     * @param teiidVersion
     * @param virtualGroup 
     * @param metadata
     */
    public ResolveVirtualGroupCriteriaVisitor(TeiidVersion teiidVersion, GroupSymbolImpl virtualGroup,  QueryMetadataInterface metadata) {
        super(teiidVersion);
        this.virtualGroup = Arrays.asList(new Object[] {virtualGroup});
        this.metadata = metadata;
    }

    @Override
    public void visit(TranslateCriteriaImpl obj) {
    	if(obj.hasTranslations()) {
    		Iterator transIter = obj.getTranslations().iterator();
    		while(transIter.hasNext()) {
				CompareCriteriaImpl ccrit = (CompareCriteriaImpl) transIter.next();
				ElementSymbolImpl element = (ElementSymbolImpl) ccrit.getLeftExpression();
				try {
                    ResolverVisitorImpl resolverVisitor = new ResolverVisitorImpl(getTeiidVersion());
                    resolverVisitor.resolveLanguageObject(element, virtualGroup, metadata);
				} catch(Exception e) {
                    throw new RuntimeException(e);
				}
    		}
    	}
    }

    @Override
    public void visit(CriteriaSelectorImpl obj) {
    	if(obj.hasElements()) {
			Iterator elmntIter = obj.getElements().iterator();
			while(elmntIter.hasNext()) {
				ElementSymbolImpl virtualElement = (ElementSymbolImpl) elmntIter.next();
                try {
                    ResolverVisitorImpl resolverVisitor = new ResolverVisitorImpl(getTeiidVersion());
                    resolverVisitor.resolveLanguageObject(virtualElement, virtualGroup, metadata);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
			}
    	}
    }

    /**
     * resolve criteria
     *
     * @param obj
     * @param virtualGroup
     * @param metadata
     * @throws Exception
     */
    public static void resolveCriteria(BaseLanguageObject obj, GroupSymbolImpl virtualGroup,  QueryMetadataInterface metadata)
        throws Exception {
        if(obj == null) {
            return;
        }

        // Resolve elements, deal with errors
        ResolveVirtualGroupCriteriaVisitor resolveVisitor = new ResolveVirtualGroupCriteriaVisitor(obj.getTeiidVersion(), virtualGroup, metadata);
        
        try {
            DeepPreOrderNavigator.doVisit(obj, resolveVisitor);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof QueryResolverException)
                throw (QueryResolverException)e.getCause();

            throw e;
        }
    }

}
