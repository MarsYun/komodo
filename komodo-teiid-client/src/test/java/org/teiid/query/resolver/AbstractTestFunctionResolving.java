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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.teiid.api.exception.query.QueryResolverException;
import org.teiid.core.types.DefaultDataTypeManager;
import org.komodo.spi.runtime.version.TeiidVersion;
import org.teiid.query.resolver.util.ResolverVisitorImpl;
import org.teiid.query.sql.symbol.ElementSymbolImpl;
import org.teiid.query.sql.symbol.BaseExpression;
import org.teiid.query.sql.symbol.FunctionImpl;
import org.teiid.query.sql.symbol.XMLSerializeImpl;

@SuppressWarnings( {"javadoc"})
public abstract class AbstractTestFunctionResolving extends AbstractTest {

    /**
     * @param teiidVersion
     */
    public AbstractTestFunctionResolving(TeiidVersion teiidVersion) {
        super(teiidVersion);
    }

    @Test
    public void testResolvesClosestType() throws Exception {
        ElementSymbolImpl e1 = getFactory().newElementSymbol("pm1.g1.e1"); //$NON-NLS-1$
        //dummy resolve to a byte
        e1.setType(DefaultDataTypeManager.DefaultDataTypes.BYTE.getTypeClass());
        e1.setMetadataID(new Object());
        FunctionImpl function = getFactory().newFunction("abs", new BaseExpression[] {e1}); //$NON-NLS-1$

        ResolverVisitorImpl visitor = new ResolverVisitorImpl(getTeiidVersion());
        visitor.resolveLanguageObject(function, getMetadataFactory().example1Cached());

        assertEquals(DefaultDataTypeManager.DefaultDataTypes.INTEGER.getTypeClass(), function.getType());
    }

    @Test
    public void testResolveConvertReference() throws Exception {
        FunctionImpl function = getFactory().newFunction(
                                         "convert", new BaseExpression[] { //$NON-NLS-1$
                                             getFactory().newReference(0), getFactory().newConstant(DefaultDataTypeManager.DefaultDataTypes.BOOLEAN.getId())});

        ResolverVisitorImpl visitor = new ResolverVisitorImpl(getTeiidVersion());
        visitor.resolveLanguageObject(function, getMetadataFactory().example1Cached());

        assertEquals(DefaultDataTypeManager.DefaultDataTypes.BOOLEAN.getTypeClass(), function.getType());
        assertEquals(DefaultDataTypeManager.DefaultDataTypes.BOOLEAN.getTypeClass(), function.getArgs()[0].getType());
    }

    @Test
    public void testResolveCoalesce() throws Exception {
        String sql = "coalesce('', '')"; //$NON-NLS-1$
        helpResolveFunction(sql);
    }

    @Test
    public void testResolveCoalesce1() throws Exception {
        String sql = "coalesce('', '', '')"; //$NON-NLS-1$
        helpResolveFunction(sql);
    }

    /**
     * Should resolve using varags logic
     */
    @Test
    public void testResolveCoalesce1a() throws Exception {
        String sql = "coalesce('', '', '', '')"; //$NON-NLS-1$
        helpResolveFunction(sql);
    }

    /**
     * Should resolve as 1 is implicitly convertable to string
     */
    @Test
    public void testResolveCoalesce2() throws Exception {
        String sql = "coalesce('', 1, '', '')"; //$NON-NLS-1$
        helpResolveFunction(sql);
    }

    @Test
    public void testResolveCoalesce3() throws Exception {
        String sql = "coalesce('', 1, null, '')"; //$NON-NLS-1$
        helpResolveFunction(sql);
    }

    @Test
    public void testResolveCoalesce4() throws Exception {
        String sql = "coalesce({d'2009-03-11'}, 1)"; //$NON-NLS-1$
        helpResolveFunction(sql);
    }

    private FunctionImpl helpResolveFunction(String sql) throws Exception {
        FunctionImpl func = (FunctionImpl)getExpression(sql);
        assertEquals(DefaultDataTypeManager.DefaultDataTypes.STRING.getTypeClass(), func.getType());
        return func;
    }

    public BaseExpression getExpression(String sql)
        throws Exception {
        BaseExpression func = getQueryParser().parseExpression(sql);
        ResolverVisitorImpl visitor = new ResolverVisitorImpl(getTeiidVersion());
        visitor.resolveLanguageObject(func, getMetadataFactory().example1Cached());
        return func;
    }

    /**
     * e1 is of type string, so 1 should be converted to string
     * @throws Exception
     */
    @Test
    public void testLookupTypeConversion() throws Exception {
        String sql = "lookup('pm1.g1', 'e2', 'e1', 1)"; //$NON-NLS-1$
        FunctionImpl f = (FunctionImpl)getExpression(sql);
        assertEquals(DefaultDataTypeManager.DefaultDataTypes.STRING.getTypeClass(), f.getArg(3).getType());
    }

    @Test
    public void testXMLSerialize() throws Exception {
        String sql = "xmlserialize(DOCUMENT '<a/>' as clob)"; //$NON-NLS-1$
        XMLSerializeImpl xs = (XMLSerializeImpl)getExpression(sql);
        assertEquals(DefaultDataTypeManager.DefaultDataTypes.CLOB.getTypeClass(), xs.getType());
    }

    @Test( expected = QueryResolverException.class )
    public void testXMLSerialize_1() throws Exception {
        String sql = "xmlserialize(DOCUMENT 1 as clob)"; //$NON-NLS-1$
        XMLSerializeImpl xs = (XMLSerializeImpl)getExpression(sql);
        assertEquals(DefaultDataTypeManager.DefaultDataTypes.CLOB.getTypeClass(), xs.getType());
    }
}
