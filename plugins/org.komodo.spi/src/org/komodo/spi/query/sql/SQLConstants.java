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
package org.komodo.spi.query.sql;

import org.komodo.spi.constants.StringConstants;

/**
 * SqlConstants
 *
 *
 */
public interface SQLConstants extends StringConstants {

    String EMPTY_STR = EMPTY_STRING;
    String TAB2 = "\t\t"; //$NON-NLS-1$
    String TAB3 = "\t\t\t"; //$NON-NLS-1$
    String TAB4 = "\t\t\t\t"; //$NON-NLS-1$
    String CR = NEW_LINE;
    String CR_1 = "\\r\\n"; //$NON-NLS-1$
    String CR_2 = "\\n"; //$NON-NLS-1$
    String BLANK = EMPTY_STRING;
    String DBL_SPACE = "  "; //$NON-NLS-1$
    String TRUE = "TRUE"; //$NON-NLS-1$
    String FALSE = "FALSE"; //$NON-NLS-1$
    String RETURN = NEW_LINE;
    String SELECT = "SELECT"; //$NON-NLS-1$
    String FROM = "FROM"; //$NON-NLS-1$
    String WHERE = "WHERE"; //$NON-NLS-1$
    String HAVING = "HAVING"; //$NON-NLS-1$
    String L_PAREN = OPEN_BRACKET;
    String R_PAREN = CLOSE_BRACKET;
    String S_QUOTE = "\'"; //$NON-NLS-1$
    String D_QUOTE = "\""; //$NON-NLS-1$
    String BAR = "|"; //$NON-NLS-1$
    String AS = "AS"; //$NON-NLS-1$
    String COLUMNS = "COLUMNS"; //$NON-NLS-1$
    String BEGIN = "BEGIN"; //$NON-NLS-1$
    String END = "END"; //$NON-NLS-1$
    String XMLELEMENT = "XMLELEMENT"; //$NON-NLS-1$
    String XMLATTRIBUTES = "XMLATTRIBUTES"; //$NON-NLS-1$
    String NAME = "NAME"; //$NON-NLS-1$
    String XMLNAMESPACES = "XMLNAMESPACES"; //$NON-NLS-1$
    String DEFAULT = "DEFAULT"; //$NON-NLS-1$
    String NO_DEFAULT = "NO DEFAULT"; //$NON-NLS-1$
    String XMLTABLE = "XMLTABLE"; //$NON-NLS-1$
    String TEXTTABLE = "TEXTTABLE"; //$NON-NLS-1$
    String TABLE = "TABLE"; //$NON-NLS-1$
    String EXEC = "EXEC"; //$NON-NLS-1$
    String CONVERT = "CONVERT"; //$NON-NLS-1$
    String NULL = "NULL"; //$NON-NLS-1$
    String IN = "IN"; //$NON-NLS-1$
    
    String ENVELOPE_NS = "http://schemas.xmlsoap.org/soap/envelope/"; //$NON-NLS-1$
    String ENVELOPE_NS_ALIAS = "soap"; //$NON-NLS-1$
    String ENVELOPE_NAME = ENVELOPE_NS_ALIAS+":Envelope"; //$NON-NLS-1$
    String HEADER_NAME = ENVELOPE_NS_ALIAS+":Header"; //$NON-NLS-1$
    String BODY_NAME = ENVELOPE_NS_ALIAS+":Body"; //$NON-NLS-1$
    
    String PATH = "PATH"; //$NON-NLS-1$
    String FOR_ORDINALITY = "FOR ORDINALITY"; //$NON-NLS-1$
    String DEFAULT_XQUERY = "/"; //$NON-NLS-1$
    String GET = "GET"; //$NON-NLS-1$
    String PASSING = "PASSING"; //$NON-NLS-1$

    String SQL_TYPE_CREATE_STRING = "CREATE"; //$NON-NLS-1$
    String SQL_TYPE_SELECT_STRING = "SELECT"; //$NON-NLS-1$
    String SQL_TYPE_UPDATE_STRING = "UPDATE"; //$NON-NLS-1$
    String SQL_TYPE_INSERT_STRING = "INSERT"; //$NON-NLS-1$
    String SQL_TYPE_DELETE_STRING = "DELETE"; //$NON-NLS-1$
    String SQL_TYPE_UNKNOWN_STRING = "UNKNOWN"; //$NON-NLS-1$
    
    String FUNCTION_GET_FILES = "getFiles"; //$NON-NLS-1$
    String FUNCTION_GET_TEXT_FILES = "getTextFiles"; //$NON-NLS-1$
    String FUNCTION_SAVE_FILE = "saveFile"; //$NON-NLS-1$
    
    String FUNCTION_INVOKE = "invoke"; //$NON-NLS-1$
    String FUNCTION_INVOKE_HTTP = "invokeHttp"; //$NON-NLS-1$
    
    String ROWCOUNT = "ROWCOUNT"; //$NON-NLS-1$
    String CHANGING = "CHANGING"; //$NON-NLS-1$
    String VARIABLES = "VARIABLES"; //$NON-NLS-1$
    String DVARS = "DVARS"; //$NON-NLS-1$
    
    String DEFAULT_DELIMITER = COMMA;
    String DEFAULT_QUOTE = "\""; //$NON-NLS-1$
    String DEFAULT_ESCAPE = "\\"; //$NON-NLS-1$
    
    String HEADER = "HEADER"; //$NON-NLS-1$
    String SKIP = "SKIP"; //$NON-NLS-1$
    String WIDTH = "width"; //$NON-NLS-1$

    String LIKE = "LIKE"; //$NON-NLS-1$

    interface INSERT_OPTIONS {
    	int REPLACE_ALL = 0;
    	int INSERT_AT_BEGINNING = 1;
    	int INSERT_AT_CURSOR =  2;
    	int INSERT_AT_END = 3;
    	
    }

}
