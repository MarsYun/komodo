/*************************************************************************************
 * JBoss, Home of Professional Open Source.
* See the COPYRIGHT.txt file distributed with this work for information
* regarding copyright ownership. Some portions may be licensed
* to Red Hat, Inc. under one or more contributor license agreements.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
* 02110-1301 USA.
 ************************************************************************************/
package org.teiid.runtime.client.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.komodo.spi.query.QueryFactory;
import org.komodo.spi.query.QueryParser;
import org.komodo.spi.query.QueryResolver;
import org.komodo.spi.query.QueryService;
import org.komodo.spi.query.metadata.QueryMetadataInterface;
import org.komodo.spi.query.sql.CommandCollectorVisitor;
import org.komodo.spi.query.sql.ElementCollectorVisitor;
import org.komodo.spi.query.sql.FunctionCollectorVisitor;
import org.komodo.spi.query.sql.GroupCollectorVisitor;
import org.komodo.spi.query.sql.GroupsUsedByElementsVisitor;
import org.komodo.spi.query.sql.PredicateCollectorVisitor;
import org.komodo.spi.query.sql.ReferenceCollectorVisitor;
import org.komodo.spi.query.sql.ResolverVisitor;
import org.komodo.spi.query.sql.SQLStringVisitor;
import org.komodo.spi.query.sql.SQLStringVisitorCallback;
import org.komodo.spi.query.sql.ValueIteratorProviderCollectorVisitor;
import org.komodo.spi.query.sql.lang.Command;
import org.komodo.spi.query.sql.lang.Expression;
import org.komodo.spi.query.sql.symbol.GroupSymbol;
import org.komodo.spi.query.sql.symbol.Symbol;
import org.komodo.spi.runtime.version.TeiidVersion;
import org.komodo.spi.udf.FunctionLibrary;
import org.komodo.spi.udf.FunctionMethodDescriptor;
import org.komodo.spi.udf.FunctionParameterDescriptor;
import org.komodo.spi.validator.UpdateValidator;
import org.komodo.spi.validator.UpdateValidator.TransformUpdateType;
import org.komodo.spi.validator.Validator;
import org.komodo.spi.xml.MappingDocumentFactory;
import org.teiid.core.types.JDBCSQLTypeInfo;
import org.teiid.language.SQLConstants;
import org.teiid.metadata.FunctionMethod;
import org.teiid.metadata.FunctionMethod.Determinism;
import org.teiid.metadata.FunctionParameter;
import org.teiid.query.function.DefaultFunctionLibrary;
import org.teiid.query.function.FunctionTree;
import org.teiid.query.function.SystemFunctionManager;
import org.teiid.query.function.TCFunctionDescriptor;
import org.teiid.query.function.UDFSource;
import org.teiid.query.parser.TCQueryParser;
import org.teiid.query.resolver.TCQueryResolver;
import org.teiid.query.resolver.util.ResolverUtil;
import org.teiid.query.resolver.util.ResolverVisitorImpl;
import org.teiid.query.sql.ProcedureReservedWords;
import org.teiid.query.sql.lang.CommandImpl;
import org.teiid.query.sql.symbol.GroupSymbolImpl;
import org.teiid.query.sql.visitor.CallbackSQLStringVisitor;
import org.teiid.query.sql.visitor.CommandCollectorVisitorImpl;
import org.teiid.query.sql.visitor.ElementCollectorVisitorImpl;
import org.teiid.query.sql.visitor.FunctionCollectorVisitorImpl;
import org.teiid.query.sql.visitor.GroupCollectorVisitorImpl;
import org.teiid.query.sql.visitor.GroupsUsedByElementsVisitorImpl;
import org.teiid.query.sql.visitor.SQLStringVisitorImpl;
import org.teiid.query.sql.visitor.ValueIteratorProviderCollectorVisitorImpl;
import org.teiid.query.validator.DefaultUpdateValidator;
import org.teiid.query.validator.DefaultUpdateValidator.UpdateType;
import org.teiid.query.validator.DefaultValidator;
import org.teiid.query.validator.PredicateCollectorVisitorImpl;
import org.teiid.query.validator.ReferenceCollectorVisitorImpl;
import org.teiid.runtime.client.proc.TCProcedureService;
import org.teiid.runtime.client.xml.MappingDocumentFactoryImpl;

/**
 *
 */
public class TCQueryService implements QueryService {

    private final TeiidVersion teiidVersion;

    private TCQueryParser queryParser;

    private final SystemFunctionManager systemFunctionManager;

    private SyntaxFactory factory;

    /**
     * @param teiidVersion teiid version
     */
    public TCQueryService(TeiidVersion teiidVersion) {
        this.teiidVersion = teiidVersion;
        systemFunctionManager = new SystemFunctionManager(teiidVersion, getClass().getClassLoader());
    }

    /**
     * @return a query parser applicable to the given teiid instance version
     */
    @Override
    public QueryParser getQueryParser() {
        if (queryParser == null) {
            queryParser = new TCQueryParser(teiidVersion);
        }

        return queryParser;
    }

    @Override
    public boolean isReservedWord(String word) {
        return SQLConstants.isReservedWord(teiidVersion, word);
    }

    @Override
    public boolean isProcedureReservedWord(String word) {
        return ProcedureReservedWords.isProcedureReservedWord(teiidVersion, word);
    }

    @Override
    public Set<String> getReservedWords() {
        return SQLConstants.getReservedWords(teiidVersion);
    }

    @Override
    public Set<String> getNonReservedWords() {
        return SQLConstants.getNonReservedWords(teiidVersion);
    }

    @Override
    public String getJDBCSQLTypeName(int jdbcType) {
        return JDBCSQLTypeInfo.getTypeName(jdbcType);
    }

    @Override
    public FunctionLibrary createFunctionLibrary() {
        return new DefaultFunctionLibrary(teiidVersion, systemFunctionManager.getSystemFunctions(), new FunctionTree[0]);
    }

    @Override
    public FunctionLibrary createFunctionLibrary(List<FunctionMethodDescriptor> functionMethodDescriptors) {

        // Dynamically return a function library for each call rather than cache it here.
        Map<String, FunctionTree> functionTrees = new HashMap<String, FunctionTree>();

        for (FunctionMethodDescriptor descriptor : functionMethodDescriptors) {

            List<FunctionParameter> inputParameters = new ArrayList<FunctionParameter>();
            for (FunctionParameterDescriptor paramDescriptor : descriptor.getInputParameters()) {
                inputParameters.add(new FunctionParameter(paramDescriptor.getName(), paramDescriptor.getType()));
            }

            FunctionParameter outputParameter = new FunctionParameter(descriptor.getOutputParameter().getName(),
                                                                      descriptor.getOutputParameter().getType());

            FunctionMethod fMethod = new FunctionMethod(descriptor.getName(), descriptor.getDescription(),
                                                        descriptor.getCategory(), descriptor.getInvocationClass(),
                                                        descriptor.getInvocationMethod(),
                                                        inputParameters.toArray(new FunctionParameter[0]), outputParameter);

            fMethod.setPushDown(descriptor.getPushDownLiteral());
            fMethod.setVarArgs(descriptor.isVariableArgs());
            if (descriptor.isDeterministic()) {
                fMethod.setDeterminism(Determinism.DETERMINISTIC);
            } else {
                fMethod.setDeterminism(Determinism.NONDETERMINISTIC);
            }

            FunctionTree tree = functionTrees.get(descriptor.getSchema());
            if (tree == null) {
                tree = new FunctionTree(teiidVersion, descriptor.getSchema(), new UDFSource(Collections.EMPTY_LIST, getClass().getClassLoader()), false);
                functionTrees.put(descriptor.getSchema(), tree);
            }

            TCFunctionDescriptor fd = tree.addFunction(descriptor.getSchema(), null, fMethod, false);
            fd.setMetadataID(descriptor.getMetadataID());
        }

        return new DefaultFunctionLibrary(teiidVersion, systemFunctionManager.getSystemFunctions(),
                                   functionTrees.values().toArray(new FunctionTree[0]));
    }

    @Override
    public QueryFactory createQueryFactory() {
        if (factory == null)
            factory = new SyntaxFactory(((TCQueryParser)getQueryParser()).getTeiidParser());

        return factory;
    }

    @Override
    public MappingDocumentFactory getMappingDocumentFactory() {
        getQueryParser();
        return new MappingDocumentFactoryImpl(queryParser.getTeiidParser());
    }

    @Override
    public String getSymbolName(Expression expression) {
        if (expression instanceof Symbol) {
            return ((Symbol)expression).getName();
        }

        return "expr"; //$NON-NLS-1$
    }

    @Override
    public String getSymbolShortName(String name) {
        int index = name.lastIndexOf(Symbol.SEPARATOR);
        if (index >= 0) {
            return name.substring(index + 1);
        }
        return name;
    }

    @Override
    public String getSymbolShortName(Expression expression) {
        if (expression instanceof Symbol) {
            return ((Symbol)expression).getShortName();
        }
        return "expr"; //$NON-NLS-1$
    }

    @Override
    public SQLStringVisitorImpl getSQLStringVisitor() {
        return new SQLStringVisitorImpl(teiidVersion);
    }

    @Override
    public SQLStringVisitor getCallbackSQLStringVisitor(SQLStringVisitorCallback visitorCallback) {
        return new CallbackSQLStringVisitor(teiidVersion, visitorCallback);
    }

    @Override
    public GroupCollectorVisitor getGroupCollectorVisitor(boolean removeDuplicates) {
        return new GroupCollectorVisitorImpl(teiidVersion, removeDuplicates);
    }

    @Override
    public GroupsUsedByElementsVisitor getGroupsUsedByElementsVisitor() {
        return new GroupsUsedByElementsVisitorImpl();
    }

    @Override
    public ElementCollectorVisitor getElementCollectorVisitor(boolean removeDuplicates) {
        return new ElementCollectorVisitorImpl(teiidVersion, removeDuplicates);
    }

    @Override
    public CommandCollectorVisitor getCommandCollectorVisitor() {
        return new CommandCollectorVisitorImpl(teiidVersion);
    }

    @Override
    public FunctionCollectorVisitor getFunctionCollectorVisitor(boolean removeDuplicates) {
        return new FunctionCollectorVisitorImpl(teiidVersion, removeDuplicates);
    }

    @Override
    public PredicateCollectorVisitor getPredicateCollectorVisitor() {
        return new PredicateCollectorVisitorImpl(teiidVersion);
    }

    @Override
    public ReferenceCollectorVisitor getReferenceCollectorVisitor() {
        return new ReferenceCollectorVisitorImpl(teiidVersion);
    }

    @Override
    public ValueIteratorProviderCollectorVisitor getValueIteratorProviderCollectorVisitor() {
        return new ValueIteratorProviderCollectorVisitorImpl(teiidVersion);
    }

    @Override
    public ResolverVisitor getResolverVisitor() {
        return new ResolverVisitorImpl(teiidVersion);
    }

    @Override
    public Validator getValidator() {
        return new DefaultValidator();
    }

    @Override
    public UpdateValidator getUpdateValidator(QueryMetadataInterface metadata, TransformUpdateType tInsertType, TransformUpdateType tUpdateType, TransformUpdateType tDeleteType) {

        UpdateType insertType = UpdateType.valueOf(tInsertType.name());
        UpdateType updateType = UpdateType.valueOf(tUpdateType.name());
        UpdateType deleteType = UpdateType.valueOf(tDeleteType.name());

        return new DefaultUpdateValidator(metadata, insertType, updateType, deleteType);
    }

    @Override
    public void resolveGroup(GroupSymbol groupSymbol, QueryMetadataInterface metadata) throws Exception {
        ResolverUtil.resolveGroup((GroupSymbolImpl)groupSymbol, metadata);
    }

    @Override
    public void fullyQualifyElements(Command command) {
        ResolverUtil.fullyQualifyElements((CommandImpl)command);
    }

    @Override
    public QueryResolver getQueryResolver() {
        getQueryParser();
        return new TCQueryResolver((TCQueryParser)getQueryParser());
    }

    @Override
    public TCProcedureService getProcedureService() {
        return new TCProcedureService(teiidVersion);
    }
}
