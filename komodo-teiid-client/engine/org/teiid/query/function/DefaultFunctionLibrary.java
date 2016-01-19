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

package org.teiid.query.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.komodo.spi.query.sql.symbol.AggregateSymbol;
import org.komodo.spi.query.sql.symbol.AggregateSymbol.Type;
import org.komodo.spi.runtime.version.TeiidVersion;
import org.komodo.spi.runtime.version.DefaultTeiidVersion.Version;
import org.komodo.spi.udf.FunctionLibrary;
import org.teiid.core.CoreConstants;
import org.teiid.core.types.DefaultDataTypeManager;
import org.teiid.core.types.Transform;
import org.teiid.metadata.AggregateAttributes;
import org.teiid.metadata.FunctionMethod;
import org.teiid.metadata.FunctionParameter;
import org.teiid.query.function.metadata.FunctionCategoryConstants;
import org.teiid.query.resolver.util.ResolverUtil;
import org.teiid.query.sql.symbol.ConstantImpl;
import org.teiid.query.sql.symbol.BaseExpression;
import org.teiid.query.sql.symbol.FunctionImpl;



/**
 * The function library is the primary way for the system to find out what
 * functions are available, resolve function signatures, and invoke system
 * and user-defined functions.
 */
public class DefaultFunctionLibrary implements FunctionLibrary {
	
	public static final String MVSTATUS = "mvstatus"; //$NON-NLS-1$

    // Function tree for system functions (never reloaded)
    private FunctionTree systemFunctions;

    // Function tree for user-defined functions
    private FunctionTree[] userFunctions;

    private final TeiidVersion teiidVersion;

    private DefaultDataTypeManager dataTypeManager;

	/**
	 * Construct the function library.  This should be called only once by the
	 * FunctionLibraryManager.
	 * @param teiidVersion
	 * @param systemFuncs 
	 * @param userFuncs 
	 */
	public DefaultFunctionLibrary(TeiidVersion teiidVersion, FunctionTree systemFuncs, FunctionTree... userFuncs) {
        this.teiidVersion = teiidVersion;
        this.systemFunctions = systemFuncs;
       	this.userFunctions = userFuncs;
	}

	/**
     * @return the teiidVersion
     */
    public TeiidVersion getTeiidVersion() {
        return this.teiidVersion;
    }

    public DefaultDataTypeManager getDataTypeManager() {
        if (dataTypeManager == null)
            dataTypeManager = DefaultDataTypeManager.getInstance(getTeiidVersion());

        return dataTypeManager;
    }

    public FunctionTree[] getUserFunctions() {
        return userFunctions;
    }

    /**
     * Get all function categories, sorted in alphabetical order
     * @return List of function category names, sorted in alphabetical order
     */
    @Override
	public List<String> getFunctionCategories() {
        // Remove category duplicates
        TreeSet<String> categories = new TreeSet<String>();
        categories.addAll( systemFunctions.getCategories() );
        if (this.userFunctions != null) {
	        for (FunctionTree tree: this.userFunctions) {
	        	categories.addAll(tree.getCategories());
	        }
        }

        ArrayList<String> categoryList = new ArrayList<String>(categories);
        return categoryList;
    }

    /**
     * Get all functions in a category.
     * @param category Category name
     * @return List of {@link FunctionMethod}s in a category
     */
    public List<FunctionMethod> getFunctionsInCategory(String category) {
        List<FunctionMethod> forms = new ArrayList<FunctionMethod>();
        forms.addAll(systemFunctions.getFunctionsInCategory(category));
        if (this.userFunctions != null) {
            for (FunctionTree tree: this.userFunctions) {
                forms.addAll(tree.getFunctionsInCategory(category));
            }
        }
        return forms;
    }


    @SuppressWarnings({ "deprecation", "unchecked" })
	@Override
    public List<TCFunctionForm> getFunctionForms(String category) {
        Set<FunctionMethod> fMethods = systemFunctions.getFunctionsInCategory(category);
        for (FunctionTree tree: this.userFunctions) {
            fMethods.addAll(tree.getFunctionsInCategory(category));
        }

        List<TCFunctionForm> forms = new ArrayList<TCFunctionForm>();

        if (fMethods != null) {
            for (FunctionMethod fMethod : fMethods) {
                forms.add(new TCFunctionForm(fMethod));
            }
        }

        return forms;
    }

    @SuppressWarnings("deprecation")
	@Override
    public TCFunctionForm findFunctionForm(String name, int numArgs) {
        List<FunctionMethod> functionMethods = systemFunctions.findFunctionMethods(name, numArgs);
        if (functionMethods.size() > 0) {
            return new TCFunctionForm(functionMethods.get(0));
        }
        if(functionMethods.isEmpty() && this.userFunctions != null) {
            for (FunctionTree tree: this.userFunctions) {
                functionMethods = tree.findFunctionMethods(name, numArgs);
                if (functionMethods.size() > 0) {
                    return new TCFunctionForm(functionMethods.get(0));
                }
            }
        }

        return null;
    }

    @Override
    public boolean hasFunctionMethod(String name, int numArgs) {
        List<FunctionMethod> methods = systemFunctions.findFunctionMethods(name, numArgs);
        if (!methods.isEmpty()) {
            return true;
        }
        if(this.userFunctions != null) {
            for (FunctionTree tree: this.userFunctions) {
                methods = tree.findFunctionMethods(name, numArgs);
                if (!methods.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public TCFunctionDescriptor findFunction(FunctionName name, Class<?>[] types) {
        return findFunction(name.text(), types);
    }
    
	/**
	 * Find a function descriptor given a name and the types of the arguments.
	 * This method matches based on case-insensitive function name and
     * an exact match of the number and types of parameter arguments.
     * @param name Name of the function to resolve
     * @param types Array of classes representing the types
     * @return Descriptor if found, null if not found
	 */
    @Override
	public TCFunctionDescriptor findFunction(String name, Class<?>[] types) {
        // First look in system functions
        TCFunctionDescriptor descriptor = systemFunctions.getFunction(name, types);

        // If that fails, check the user defined functions
        if(descriptor == null && this.userFunctions != null) {
        	for (FunctionTree tree: this.userFunctions) {
        		descriptor = tree.getFunction(name, types);
        		if (descriptor != null) {
        			break;
        		}
        	}
        }

        return descriptor;
	}

    /**
     * Find a function descriptor given a name and the types of the arguments.
     * This method matches based on case-insensitive function name and
     * an exact match of the number and types of parameter arguments.
     * @param name Name of the function to resolve
     * @param types Array of classes representing the types
     * @return Descriptor if found, null if not found
     */
    public List<TCFunctionDescriptor> findAllFunctions(String name, Class<?>[] types) {
        // First look in system functions
        TCFunctionDescriptor descriptor = systemFunctions.getFunction(name, types);

        // If that fails, check the user defined functions
        if(descriptor == null && this.userFunctions != null) {
            List<TCFunctionDescriptor> result = new LinkedList<TCFunctionDescriptor>();
            for (FunctionTree tree: this.userFunctions) {
                descriptor = tree.getFunction(name, types);
                if (descriptor != null) {
                    //pushdown function takes presedence 
                    //TODO: there may be multiple translators contributing functions with the same name / types
                    //need "conformed" logic so that the right pushdown can occur
                    if (descriptor.getMethod().getParent() == null || CoreConstants.SYSTEM_MODEL.equals(descriptor.getMethod().getParent().getName())) {
                        return Arrays.asList(descriptor);
                    }
                    result.add(descriptor);
                }
            }
            return result;
        }
        if (descriptor != null) {
            return Arrays.asList(descriptor);
        }
        return Collections.emptyList();
    }

	/**
	 * Get the conversions that are needed to call the named function with arguments
	 * of the given type.  In the case of an exact match, the list will contain all nulls.
	 * In other cases the list will contain one or more non-null values where the value
	 * is a conversion function that can be used to convert to the proper types for
	 * executing the function.
     * @param name Name of function
	 * @param returnType
	 * @param args 
	 * @param types Existing types passed to the function
     * @return Null if no conversion could be found, otherwise an array of conversions
     * to apply to each argument.  The list should match 1-to-1 with the parameters.
     * Parameters that do not need a conversion are null; parameters that do are
     * FunctionDescriptors.
	 * @throws Exception
	 */
	public TCFunctionDescriptor[] determineNecessaryConversions(String name, Class<?> returnType, BaseExpression[] args, Class<?>[] types, boolean hasUnknownType) throws Exception {
		// Check for no args - no conversion necessary
		if(types.length == 0) {
		    if (getTeiidVersion().isLessThan(Version.TEIID_8_0.get()))
		        return new TCFunctionDescriptor[0];

			return null;
		}

        //First find existing functions with same name and same number of parameters
        final Collection<FunctionMethod> functionMethods = new LinkedList<FunctionMethod>();
        functionMethods.addAll( this.systemFunctions.findFunctionMethods(name, types.length) );
        if (this.userFunctions != null) {
	        for (FunctionTree tree: this.userFunctions) {
	        	functionMethods.addAll( tree.findFunctionMethods(name, types.length) );
	        }
        }
        
        //Score each match, reject any where types can not be converted implicitly       
        //Score of current method (lower score means better match with less converts
        //Current best score (lower score is best.  Higher score results in more implicit conversions
        int bestScore = Integer.MAX_VALUE;
        boolean ambiguous = false;
        FunctionMethod result = null;
                
        outer: for (FunctionMethod nextMethod : functionMethods) {
            int currentScore = 0; 
            final List<FunctionParameter> methodTypes = nextMethod.getInputParameters();
            //Holder for current signature with converts where required
            
            //Iterate over the parameters adding conversions where required or failing when
            //no implicit conversion is possible
            for(int i = 0; i < types.length; i++) {
                final String tmpTypeName = methodTypes.get(Math.min(i, methodTypes.size() - 1)).getType();
                Class<?> targetType = getDataTypeManager().getDataTypeClass(tmpTypeName);

                Class<?> sourceType = types[i];
                if (sourceType == null) {
                    currentScore++;
                    continue;
                }
                if (sourceType.isArray() && targetType.isArray()
                    && sourceType.getComponentType().equals(targetType.getComponentType())) {
                    currentScore++;
                    continue;
                }
                if (sourceType.isArray()) {
                    if (isVarArgArrayParam(nextMethod, types, i, targetType)) {
                		//vararg array parameter
                		continue;
                	}
                    //treat the array as object type until proper type handling is added
                	sourceType = DefaultDataTypeManager.DefaultDataTypes.OBJECT.getTypeClass();
                }
				try {
					Transform t = getConvertFunctionDescriptor(sourceType, targetType);
					if (t != null) {
		                if (t.isExplicit()) {
		                	if (!(args[i] instanceof ConstantImpl) || ResolverUtil.convertConstant(getDataTypeManager().getDataTypeName(sourceType), tmpTypeName, (ConstantImpl)args[i]) == null) {
		                		continue outer;
		                	}
		                	currentScore++;
		                } else {
		                	currentScore++;
		                }
					}
				} catch (Exception e) {
					continue outer;
				}
            }
            
            //If the method is valid match and it is the current best score, capture those values as current best match
            if (currentScore > bestScore) {
                continue;
            }
            
            if (hasUnknownType) {
            	if (returnType != null) {
            		try {
						Transform t = getConvertFunctionDescriptor(getDataTypeManager().getDataTypeClass(nextMethod.getOutputParameter().getType()), returnType);
						if (t != null) {
							if (t.isExplicit()) {
								//there still may be a common type, but use any other valid conversion over this one
								currentScore += types.length + 1;
							} else {
								currentScore++;
							}
						}
					} catch (Exception e) {
						//there still may be a common type, but use any other valid conversion over this one
						currentScore += (types.length * types.length);
					}
            	}
            }

            boolean useNext = false;
            if (currentScore == bestScore) {
                ambiguous = true;
                boolean useCurrent = false;
                List<FunctionParameter> bestParams = result.getInputParameters();
                for (int j = 0; j < types.length; j++) {
                    String t1 = bestParams.get(Math.min(j, bestParams.size() - 1)).getType();
                    String t2 = methodTypes.get((Math.min(j, methodTypes.size() - 1))).getType();
                    
                    if (types[j] == null || t1.equals(t2)) {
                        continue;
                    }
                    
                    String commonType = ResolverUtil.getCommonType(teiidVersion, new String[] {t1, t2});
                    
                    if (commonType == null) {
                        continue outer; //still ambiguous
                    }
                    
                    if (commonType.equals(t1)) {
                        if (!useCurrent) {
                            useNext = true;
                        }
                    } else if (commonType.equals(t2)) {
                        if (!useNext) {
                            useCurrent = true;
                        }
                    } else {
                        continue outer;
                    }
                }
                if (useCurrent) {
                    ambiguous = false; //prefer narrower
                }
            }
            
            if (currentScore < bestScore || useNext) {
                ambiguous = false;
                if (currentScore == 0) {
                    //this must be an exact match
                    return null;
                }    
                
                bestScore = currentScore;
                result = nextMethod;
            }            
        }
        
        if (ambiguous || result == null) {
             throw new Exception();
        }
        
		return getConverts(result, types);
	}
	
	private TCFunctionDescriptor[] getConverts(FunctionMethod method, Class<?>[] types) {
        final List<FunctionParameter> methodTypes = method.getInputParameters();
        TCFunctionDescriptor[] result = new TCFunctionDescriptor[types.length];
        for(int i = 0; i < types.length; i++) {
        	//treat all varags as the same type
            final String tmpTypeName = methodTypes.get(Math.min(i, methodTypes.size() - 1)).getType();
            Class<?> targetType = getDataTypeManager().getDataTypeClass(tmpTypeName);

            Class<?> sourceType = types[i];
            if (sourceType == null) {
                result[i] = findTypedConversionFunction(DefaultDataTypeManager.DefaultDataTypes.NULL.getTypeClass(), targetType);
            } else if (sourceType != targetType){
            	if (isVarArgArrayParam(method, types, i, targetType)) {
            		//vararg array parameter
            		continue;
            	}
            	result[i] = findTypedConversionFunction(sourceType, targetType);
            }
        }
        return result;
	}

	public boolean isVarArgArrayParam(FunctionMethod method, Class<?>[] types,
			int i, Class<?> targetType) {
		return i == types.length - 1 && method.isVarArgs() && i == method.getInputParameterCount() - 1 
				&& types[i].isArray() && targetType.isAssignableFrom(types[i].getComponentType());
	}
	
	private Transform getConvertFunctionDescriptor(Class<?> sourceType, Class<?> targetType) throws Exception {
        //If exact match no conversion necessary
        if(sourceType.equals(targetType)) {
            return null;
        }
        Transform result = getDataTypeManager().getTransform(sourceType, targetType);
        //Else see if an implicit conversion is possible.
        if(result == null) {
             throw new Exception();
        }

        /*
         * A rather odd test which is necessary to maintain compatibility with 7.7.x
         *
         * The original code line is 7.7.x is:
         * if(!DataTypeManagerService.isImplicitConversion(sourceTypeName, targetTypeName))
         * and this version does the same thing. This may have been a fix to address a bug
         * but if someone uses teiid 7, they will encounter this exception so the designer
         * client resolver must mirror the same exception.
         */
        if(teiidVersion.isLessThan(Version.TEIID_8_0.get()) && result.isExplicit()) {
            throw new Exception();
        }

        return result;
	}

    /**
     * Find conversion function and set return type to proper type.   
     * @param sourceType The source type class
     * @param targetType The target type class
     * @return A CONVERT function descriptor or null if not possible
     */
    public TCFunctionDescriptor findTypedConversionFunction(Class<?> sourceType, Class<?> targetType) {
    	//TODO: should array to string be prohibited?    	
        TCFunctionDescriptor fd = findFunction(FunctionName.CONVERT, new Class[] {sourceType, DefaultDataTypeManager.DefaultDataTypes.STRING.getTypeClass()});
        if (fd != null) {
            return copyFunctionChangeReturnType(fd, targetType);
        }
        return null;
    }

	/**
	 * Return a copy of the given FunctionDescriptor with the sepcified return type.
	 * @param fd FunctionDescriptor to be copied.
	 * @param returnType The return type to apply to the copied FunctionDescriptor.
	 * @return The copy of FunctionDescriptor.
	 */
    public TCFunctionDescriptor copyFunctionChangeReturnType(TCFunctionDescriptor fd, Class<?> returnType) {
        if(fd != null) {
        	TCFunctionDescriptor fdImpl = fd;
            TCFunctionDescriptor copy = fdImpl.clone();
            copy.setReturnType(returnType);
            return copy;
        }
        return fd;
    }
    
    public static boolean isConvert(FunctionImpl function) {
        BaseExpression[] args = function.getArgs();
        String funcName = function.getName();
        
        return args.length == 2 && (FunctionName.CONVERT.equalsIgnoreCase(funcName) || FunctionName.CAST.equalsIgnoreCase(funcName));
    }
    
    @Override
    public String getFunctionName(FunctionName functionName) {
        if (functionName == null)
            throw new IllegalArgumentException();
        
        return functionName.text();
    }

    /**
     * Return a list of the most general forms of built-in aggregate functions.
     * <br/>count(*) - is not included
     * <br/>textagg - is not included due to its non standard syntax
     * 
     * @param includeAnalytic - true to include analytic functions that must be windowed
     * @return
     */
    public List<FunctionMethod> getBuiltInAggregateFunctions(boolean includeAnalytic) {
        ArrayList<FunctionMethod> result = new ArrayList<FunctionMethod>();
        for (Type type : AggregateSymbol.Type.values()) {
            AggregateAttributes aa = new AggregateAttributes();
            String returnType = null;
            String[] argTypes = null;
            aa.setAllowsDistinct(true);
            switch (type) {
            case USER_DEFINED:
                continue;
            case DENSE_RANK:
            case RANK:
            case ROW_NUMBER:
                if (!includeAnalytic) {
                    continue;
                }
                aa.setAllowsDistinct(false);
                aa.setAnalytic(true);
                returnType = DefaultDataTypeManager.DefaultDataTypes.INTEGER.getId();
                argTypes = new String[] {};
                break;
            case ANY:
            case SOME:
            case EVERY:
                returnType = DefaultDataTypeManager.DefaultDataTypes.BOOLEAN.getId();
                argTypes = new String[] {DefaultDataTypeManager.DefaultDataTypes.BOOLEAN.getId()};
                break;
            case COUNT:
                returnType = DefaultDataTypeManager.DefaultDataTypes.INTEGER.getId();
                argTypes = new String[] {DefaultDataTypeManager.DefaultDataTypes.OBJECT.getId()};
                break;
            case MAX:
            case MIN:
            case AVG:
            case SUM:
                returnType = DefaultDataTypeManager.DefaultDataTypes.OBJECT.getId();
                argTypes = new String[] {DefaultDataTypeManager.DefaultDataTypes.OBJECT.getId()};
                break;
            case STDDEV_POP:
            case STDDEV_SAMP:
            case VAR_POP:
            case VAR_SAMP:
                returnType = DefaultDataTypeManager.DefaultDataTypes.DOUBLE.getId();
                argTypes = new String[] {DefaultDataTypeManager.DefaultDataTypes.DOUBLE.getId()};
                break;
            case STRING_AGG:
                returnType = DefaultDataTypeManager.DefaultDataTypes.OBJECT.getId();
                argTypes = new String[] {DefaultDataTypeManager.DefaultDataTypes.OBJECT.getId()};
                aa.setAllowsOrderBy(true);
                break;
            case ARRAY_AGG:
                returnType = DefaultDataTypeManager.DefaultDataTypes.OBJECT.getId();
                argTypes = new String[] {getDataTypeManager().getDataTypeName(DefaultDataTypeManager.DefaultDataTypes.OBJECT.getTypeArrayClass())};
                aa.setAllowsOrderBy(true);
                aa.setAllowsDistinct(false);
                break;
            case JSONARRAY_AGG:
                returnType = DefaultDataTypeManager.DefaultDataTypes.CLOB.getId();
                argTypes = new String[] {DefaultDataTypeManager.DefaultDataTypes.OBJECT.getId()};
                aa.setAllowsOrderBy(true);
                aa.setAllowsDistinct(false);
                break;
            case XMLAGG:
                returnType = DefaultDataTypeManager.DefaultDataTypes.XML.getId();
                argTypes = new String[] {DefaultDataTypeManager.DefaultDataTypes.XML.getId()};
                aa.setAllowsOrderBy(true);
                aa.setAllowsDistinct(false);
                break;
            case TEXTAGG:
            	break;
            }
            FunctionMethod fm = FunctionMethod.createFunctionMethod(type.name(), type.name(), FunctionCategoryConstants.AGGREGATE, returnType, argTypes);
            fm.setAggregateAttributes(aa);
            result.add(fm);
        }
        return result;
    }
}
