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

package org.teiid.core.types;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.komodo.spi.runtime.version.TeiidVersion;
import org.teiid.core.types.DefaultDataTypeManager.DefaultDataTypes;

/**
 * <p> This is a helper class used to obtain SQL type information for java types.
 * The SQL type information is obtained from java.sql.Types class. The integers and
 * strings returned by methods in this class are based on constants in java.sql.Types.
 */

public final class JDBCSQLTypeInfo {
	
	public static class TypeInfo {
		String name;
		int maxDisplaySize;
		int defaultPrecision;
		String javaClassName;
		int[] jdbcTypes;
		
		public TypeInfo(int maxDisplaySize, int precision, String name,
				String javaClassName, int[] jdbcTypes) {
			super();
			this.maxDisplaySize = maxDisplaySize;
			this.defaultPrecision = precision;
			this.name = name;
			this.javaClassName = javaClassName;
			this.jdbcTypes = jdbcTypes;
		}
		
	}
	
    // Prevent instantiation
    private JDBCSQLTypeInfo() {}

    public static final Integer DEFAULT_RADIX = 10;
    public static final Integer DEFAULT_SCALE = 0;

    // XML column constants
    public final static Integer XML_COLUMN_LENGTH = Integer.MAX_VALUE;

    private static Map<String, TypeInfo> NAME_TO_TYPEINFO = new LinkedHashMap<String, TypeInfo>();
    private static Map<Integer, TypeInfo> TYPE_TO_TYPEINFO = new HashMap<Integer, TypeInfo>();
    private static Map<String, TypeInfo> CLASSNAME_TO_TYPEINFO = new HashMap<String, TypeInfo>();

    static {
    	//note the order in which these are added matters.  if there are multiple sql type mappings (e.g. biginteger and bigdecimal to numeric), the latter will be the primary
    	addType(DefaultDataTypeManager.DefaultDataTypes.BIG_INTEGER, 20, 19, DefaultDataTypeManager.DefaultDataTypes.BIG_INTEGER.getId(), Types.NUMERIC);
    	addType(new String[] {DefaultDataTypeManager.DefaultDataTypes.BIG_DECIMAL.getId(), "decimal"}, 22, 20, DefaultDataTypeManager.DefaultDataTypes.BIG_DECIMAL.getId(), Types.NUMERIC, Types.DECIMAL); //$NON-NLS-1$
    	addType(DefaultDataTypeManager.DefaultDataTypes.BLOB, Integer.MAX_VALUE, Integer.MAX_VALUE, Blob.class.getName(), Types.BLOB, Types.LONGVARBINARY);
    	addType(DefaultDataTypeManager.DefaultDataTypes.BOOLEAN, 5, 1, DefaultDataTypeManager.DefaultDataTypes.BOOLEAN.getId(), Types.BIT, Types.BOOLEAN);
    	addType(new String[] {DefaultDataTypeManager.DefaultDataTypes.BYTE.getId(), "tinyint"}, 4, 3, DefaultDataTypeManager.DefaultDataTypes.BYTE.getId(), Types.TINYINT); //$NON-NLS-1$
    	addType(DefaultDataTypeManager.DefaultDataTypes.CHAR, 1, 1, DefaultDataTypeManager.DefaultDataTypes.CHAR.getId(), Types.CHAR);
    	addType(DefaultDataTypeManager.DefaultDataTypes.CLOB, Integer.MAX_VALUE, Integer.MAX_VALUE, Clob.class.getName(), Types.CLOB, Types.NCLOB, Types.LONGNVARCHAR, Types.LONGVARCHAR);
    	addType(DefaultDataTypeManager.DefaultDataTypes.DATE, 10, 10, DefaultDataTypeManager.DefaultDataTypes.DATE.getId(), Types.DATE);
    	addType(DefaultDataTypeManager.DefaultDataTypes.DOUBLE, 22, 20, DefaultDataTypeManager.DefaultDataTypes.DOUBLE.getId(), Types.DOUBLE, Types.FLOAT);
    	addType(new String[] {DefaultDataTypeManager.DefaultDataTypes.FLOAT.getId(), "real"}, 22, 20, DefaultDataTypeManager.DefaultDataTypes.FLOAT.getId(), Types.REAL); //$NON-NLS-1$
    	addType(DefaultDataTypeManager.DefaultDataTypes.INTEGER, 11, 10, DefaultDataTypeManager.DefaultDataTypes.INTEGER.getId(), Types.INTEGER);
    	addType(new String[] {DefaultDataTypeManager.DefaultDataTypes.LONG.getId(), "bigint"}, 20, 19, DefaultDataTypeManager.DefaultDataTypes.LONG.getId(), Types.BIGINT); //$NON-NLS-1$
    	addType(DefaultDataTypeManager.DefaultDataTypes.OBJECT, Integer.MAX_VALUE, Integer.MAX_VALUE, DefaultDataTypeManager.DefaultDataTypes.OBJECT.getId(), Types.JAVA_OBJECT);
    	addType(new String[] {DefaultDataTypeManager.DefaultDataTypes.SHORT.getId(), "smallint"}, 6, 5, DefaultDataTypeManager.DefaultDataTypes.SHORT.getId(), Types.SMALLINT); //$NON-NLS-1$
    	addType(new String[] {DefaultDataTypeManager.DefaultDataTypes.STRING.getId(), "varchar"}, DefaultDataTypeManager.MAX_STRING_LENGTH, DefaultDataTypeManager.MAX_STRING_LENGTH, DefaultDataTypeManager.DefaultDataTypes.STRING.getId(), Types.VARCHAR, Types.NVARCHAR, Types.CHAR, Types.NCHAR); //$NON-NLS-1$
    	addType(DefaultDataTypeManager.DefaultDataTypes.TIME, 8, 8, DefaultDataTypeManager.DefaultDataTypes.TIME.getId(), Types.TIME);
    	addType(DefaultDataTypeManager.DefaultDataTypes.TIMESTAMP, 29, 29, DefaultDataTypeManager.DefaultDataTypes.TIMESTAMP.getId(), Types.TIMESTAMP);
    	addType(DefaultDataTypeManager.DefaultDataTypes.XML, Integer.MAX_VALUE, Integer.MAX_VALUE, SQLXML.class.getName(), Types.SQLXML);
    	addType(DefaultDataTypeManager.DefaultDataTypes.NULL, 4, 1, null, Types.NULL);
    	addType(DefaultDataTypeManager.DefaultDataTypes.VARBINARY, DefaultDataTypeManager.MAX_LOB_MEMORY_BYTES, DefaultDataTypeManager.MAX_LOB_MEMORY_BYTES, byte[].class.getName(), Types.VARBINARY, Types.BINARY);
    	addType(DefaultDataTypeManager.DefaultDataTypes.VARBINARY, DefaultDataTypeManager.MAX_LOB_MEMORY_BYTES, DefaultDataTypeManager.MAX_LOB_MEMORY_BYTES, byte[].class.getName(), Types.VARBINARY, Types.BINARY);
    	
    	TypeInfo typeInfo = new TypeInfo(Integer.MAX_VALUE, 0, "ARRAY", Array.class.getName(), new int[Types.ARRAY]); //$NON-NLS-1$
		CLASSNAME_TO_TYPEINFO.put(Array.class.getName(), typeInfo); 
    	TYPE_TO_TYPEINFO.put(Types.ARRAY, typeInfo);
    }

    private static TypeInfo addType(DefaultDataTypes type, int maxDisplaySize, int precision, String javaClassName, int... sqlTypes) {
        return addType(type.getId(), maxDisplaySize, precision, javaClassName, sqlTypes);
    }
    
	private static TypeInfo addType(String typeName, int maxDisplaySize, int precision, String javaClassName, int... sqlTypes) {
		TypeInfo ti = new TypeInfo(maxDisplaySize, precision, typeName, javaClassName, sqlTypes);
		NAME_TO_TYPEINFO.put(typeName, ti);
		if (javaClassName != null) {
			CLASSNAME_TO_TYPEINFO.put(javaClassName, ti);
		}
		for (int i : sqlTypes) {
			TYPE_TO_TYPEINFO.put(i, ti);
		}
		return ti;
	}
	
	private static void addType(String[] typeNames, int maxDisplaySize, int precision, String javaClassName, int... sqlTypes) {
		TypeInfo ti = addType(typeNames[0], maxDisplaySize, precision, javaClassName, sqlTypes);
		for (int i = 1; i < typeNames.length; i++) {
			NAME_TO_TYPEINFO.put(typeNames[i], ti);
		}
	}

    /**
     * This method is used to obtain a short indicating JDBC SQL type for any object.
     * The short values that give the type info are from java.sql.Types.
     * @param Name of the teiid type.
     * @return A short value representing SQL Type for the given java type.
     */
    public static final int getSQLType(String typeName) {

        if (typeName == null) {
            return Types.NULL;
        }
        
        TypeInfo sqlType = NAME_TO_TYPEINFO.get(typeName);
        
        if (sqlType == null) {
            if (DefaultDataTypeManager.isArrayType(typeName)) {
        		return Types.ARRAY;
        	}
            return Types.JAVA_OBJECT;
        }
        
        return sqlType.jdbcTypes[0];
    }    

    /**
     * Get sql Type from java class type name.  This should not be called with runtime types
     * as Clob and Blob are represented by ClobType and BlobType respectively.
     * @param typeName
     * @return int
     */
    public static final int getSQLTypeFromClass(String className) {

        if (className == null) {
            return Types.NULL;
        }
        
        TypeInfo sqlType = CLASSNAME_TO_TYPEINFO.get(className);
        
        if (sqlType == null) {
            return Types.JAVA_OBJECT;
        }
        
        return sqlType.jdbcTypes[0];
    }
    
    /**
     * This method is used to obtain a the java class name given an int value
     * indicating JDBC SQL type. The int values that give the type info are from
     * java.sql.Types.
     * @param int value giving the SQL type code.
     * @return A String representing the java class name for the given SQL Type.
     */
    public static final String getJavaClassName(int jdbcSQLType) {
    	TypeInfo typeInfo = TYPE_TO_TYPEINFO.get(jdbcSQLType);
    	
    	if (typeInfo == null) {
    		return DefaultDataTypeManager.DefaultDataTypes.OBJECT.getId();
    	}
    	
    	return typeInfo.javaClassName;
    }
    
    public static final String getTypeName(int sqlType) {
    	TypeInfo typeInfo = TYPE_TO_TYPEINFO.get(sqlType);
    	
    	if (typeInfo == null) {
    		return DefaultDataTypeManager.DefaultDataTypes.OBJECT.getId();
    	}
    	
    	return typeInfo.name;
    }

    public static Set<String> getMMTypeNames() {
    	return NAME_TO_TYPEINFO.keySet();
    }

	public static Integer getMaxDisplaySize(TeiidVersion teiidVersion, Class<?> dataTypeClass) {
	    return getMaxDisplaySize(DefaultDataTypeManager.getInstance(teiidVersion).getDataTypeName(dataTypeClass));
	}

	public static Integer getMaxDisplaySize(String typeName) {
		TypeInfo ti = NAME_TO_TYPEINFO.get(typeName);
		if (ti == null) {
			return null;
		}
	    return ti.maxDisplaySize;
	}

	public static Integer getDefaultPrecision(TeiidVersion teiidVersion, Class<?> dataTypeClass) {
	    return getDefaultPrecision(DefaultDataTypeManager.getInstance(teiidVersion).getDataTypeName(dataTypeClass));
	}

	public static Integer getDefaultPrecision(String typeName) {
		TypeInfo ti = NAME_TO_TYPEINFO.get(typeName);
		if (ti == null) {
			return null;
		}
	    return ti.defaultPrecision;
	}

}
