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
package org.teiid.runtime.client.proc;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.komodo.spi.query.ProcedureService;
import org.komodo.spi.query.proc.TeiidColumnInfo;
import org.komodo.spi.query.proc.TeiidMetadataFileInfo;
import org.komodo.spi.query.proc.TeiidXmlColumnInfo;
import org.komodo.spi.query.proc.TeiidXmlFileInfo;
import org.komodo.spi.query.proc.wsdl.WsdlRequestInfo;
import org.komodo.spi.query.proc.wsdl.WsdlResponseInfo;
import org.komodo.spi.query.proc.wsdl.WsdlWrapperInfo;
import org.komodo.spi.query.sql.SQLConstants;
import org.komodo.spi.runtime.version.TeiidVersion;
import org.teiid.query.proc.wsdl.WsdlRequestProcedureHelper;
import org.teiid.query.proc.wsdl.WsdlResponseProcedureHelper;
import org.teiid.query.proc.wsdl.WsdlWrapperHelper;
import org.teiid.runtime.client.Messages;

/**
 *
 */
public class TCProcedureService implements ProcedureService, SQLConstants {

    private final TeiidVersion teiidVersion;

    /**
     * @param teiidVersion
     */
    public TCProcedureService(TeiidVersion teiidVersion) {
        this.teiidVersion = teiidVersion;
    }

    @Override
    public String getSQLStatement(TeiidMetadataFileInfo metadataFileInfo, String relationalModelName) {
        /*
         * 
         * TEXTTABLE(expression COLUMNS <COLUMN>, ... [DELIMITER char] [(QUOTE|ESCAPE) char] [HEADER [integer]] [SKIP integer]) AS name
         * 
         * DELIMITER sets the field delimiter character to use. Defaults to ','.
         * 
         * QUOTE sets the quote, or qualifier, character used to wrap field values. Defaults to '"'.
         * 
         * ESCAPE sets the escape character to use if no quoting character is in use. This is used in situations where the delimiter or new line characters are escaped with a preceding character, e.g. \
         * 
         * 
            SELECT A.lastName, A.firstName, A.middleName, A.AId FROM
        (EXEC EmployeeData.getTextFiles('EmployeeData.txt')) AS f, TEXTTABLE(file COLUMNS lastName string, firstName string, middleName string, HEADER 3) AS A
         
         *
         * SELECT {0} FROM (EXEC {1}.getTextFiles({2})) AS f, TEXTTABLE(file COLUMNS {3}  HEADER {4}) AS {5}
         */
        List<String> tokens = new ArrayList<String>();
        List<TeiidColumnInfo> columnInfoList = metadataFileInfo.getColumnInfoList();
        
        String alias = "A"; //$NON-NLS-1$
        StringBuffer sb = new StringBuffer();
        int i=0;
        int nColumns = columnInfoList.size();
        for(TeiidColumnInfo columnStr : columnInfoList) {
            sb.append(alias).append(DOT).append(columnStr.getSymbolName());
            
            if(i < (nColumns-1)) {
                sb.append(COMMA).append(SPACE);
            }
            i++;
        }
        tokens.add(sb.toString());
        tokens.add(relationalModelName);
        
        sb = new StringBuffer();
        i=0;
        for( TeiidColumnInfo columnStr : columnInfoList) {
            sb.append(columnStr.getSymbolName()).append(SPACE).append(columnStr.getDatatype());
            if( metadataFileInfo.isFixedWidthColumns()) {
                sb.append(SPACE).append(WIDTH).append(SPACE).append(Integer.toString(columnStr.getWidth()));
            }
            if(i < (nColumns-1)) {
                sb.append(COMMA).append(SPACE);
            }

            i++;
        }
        
        String proc = S_QUOTE + metadataFileInfo.getDataFile().getName() + S_QUOTE;
        if( metadataFileInfo.isUrl() ) {
            proc = S_QUOTE + GET + S_QUOTE
                    + COMMA + SPACE + NULL.toLowerCase()
                    + COMMA + SPACE + S_QUOTE + metadataFileInfo.getFileUrl() + S_QUOTE
                    + COMMA + SPACE + S_QUOTE + TRUE + S_QUOTE;
        }
        tokens.add(proc);
        
        if(metadataFileInfo.isUrl()) {
        	tokens.add(metadataFileInfo.getCharSet());
        }
        
        tokens.add(sb.toString());
        
        sb = new StringBuffer();
        
        String delimiter = metadataFileInfo.getDelimiter();
        if( metadataFileInfo.doUseDelimitedColumns() && ! DEFAULT_DELIMITER.equals(delimiter) ) {
            sb.append("DELIMITER"); //$NON-NLS-1$
            sb.append(SPACE).append('\'').append(delimiter).append('\'');
        }
        
        if( metadataFileInfo.doIncludeQuote() ) {
            String quote = metadataFileInfo.getQuote();
            if( ! DEFAULT_QUOTE.equals(quote)) {
                sb.append("QUOTE"); //$NON-NLS-1$
                sb.append(SPACE).append('\'').append(quote).append('\'');
            }
        } else if(metadataFileInfo.doIncludeEscape() ) {
            String escape = metadataFileInfo.getEscape();
            if( ! DEFAULT_ESCAPE.equals(escape)) {
                sb.append("ESCAPE"); //$NON-NLS-1$
                sb.append(SPACE).append('\'').append(escape).append('\'');
            }
        }
        
        if( metadataFileInfo.doIncludeHeader() ) {
            sb.append(SPACE).append("HEADER"); //$NON-NLS-1$
            if( metadataFileInfo.getHeaderLineNumber() > 1 ) {
                sb.append(SPACE).append(Integer.toString(metadataFileInfo.getHeaderLineNumber()));
            }
        }
        
        int firstDataRow = metadataFileInfo.getFirstDataRow();
        if( firstDataRow > 1 && (metadataFileInfo.doIncludeSkip() || metadataFileInfo.isFixedWidthColumns()) ) {
            sb.append(SPACE).append("SKIP"); //$NON-NLS-1$
            sb.append(SPACE).append(Integer.toString(firstDataRow-1));
        }
        
        if( metadataFileInfo.doIncludeNoTrim() && firstDataRow > 1 ) {
            sb.append(SPACE).append("NO TRIM"); //$NON-NLS-1$
        }
        tokens.add(sb.toString());
        tokens.add(alias);
        
        String finalSQLString = null;
        if(metadataFileInfo.isUrl()) {
        	finalSQLString = Messages.getString(Messages.ProcedureService.procedureServiceTextInvokeHttpTableSqlTemplate, tokens.toArray(new Object[0])); 
        } else {
        	finalSQLString = Messages.getString(Messages.ProcedureService.procedureServiceTextTableSqlTemplate, tokens.toArray(new Object[0])); 
        }
        return finalSQLString;
    }
    
    @Override
    public String getSQLStatement(TeiidXmlFileInfo xmlFileInfo, String relationalModelName) {
        /*
        ##  SELECT
        ##      title.pmid AS pmid, title.journal AS journal, title.title AS title
        ##  FROM
        ##      (EXEC getMeds.getTextFiles('medsamp2011.xml')) AS f, 
        ##           XMLTABLE('$d/MedlineCitationSet/MedlineCitation' PASSING 
        ##                 XMLPARSE(DOCUMENT f.file) AS d 
        ##                 COLUMNS pmid biginteger PATH 'PMID', journal string PATH 'Article/Journal/Title', title string PATH 'Article/ArticleTitle') AS title
        ##
        ## XMLTABLE([<NSP>,] xquery-expression [<PASSING>] [COLUMNS <COLUMN>, ... )] AS name
        ##
        ## COLUMN := name (FOR ORDINALITY | (datatype [DEFAULT expression] [PATH string]))
        ##
        ##  name     datatype  PATH            string
        ## journal    string   PATH   'Article/Journal/Title'
        ##
        ##
        ##
        ##  IF URL XML file, use invokeHTTP() method
        ##
        ## EXAMPLE:
        
            EXEC SampleSource.getTextFiles('sample.xml')) AS f, 
                XMLTABLE(XMLNAMESPACES('http://www.kaptest.com/schema/1.0/party' AS pty), 
                '/pty:students/student' PASSING XMLPARSE(DOCUMENT f.file) 
                COLUMNS firstName string PATH '/firstName', middleName string PATH '/middleName', lastName string PATH '/lastName') AS A
        ##
        ##
        ## XMLNAMESPACES('http://www.kaptest.com/schema/1.0/party' AS pty)
        
        */
        List<String> tokens = new ArrayList<String>();
        List<TeiidXmlColumnInfo> columnInfoList = xmlFileInfo.getColumnInfoList();
        
        String alias = "A"; //$NON-NLS-1$
        StringBuffer sb = new StringBuffer();
        int i=0;
        int nColumns = columnInfoList.size();
        for( TeiidXmlColumnInfo columnInfo : columnInfoList) {
            String name = columnInfo.getSymbolName();
            sb.append(alias).append(DOT).append(name).append(SPACE).append(AS).append(SPACE).append(name);
            
            if(i < (nColumns-1)) {
                sb.append(COMMA).append(SPACE);
            }
            i++;
        }
        tokens.add(sb.toString());
        tokens.add(relationalModelName);
        
        String proc = S_QUOTE + xmlFileInfo.getDataFile().getName() + S_QUOTE;
        if( xmlFileInfo.isUrl() ) {
            proc = S_QUOTE + GET + S_QUOTE
                    + COMMA + SPACE + NULL.toLowerCase()
                    + COMMA + SPACE + S_QUOTE + xmlFileInfo.getXmlFileUrl() + S_QUOTE
                    + COMMA + SPACE + S_QUOTE + TRUE + S_QUOTE;
        }
        tokens.add(proc);
        
        String namespaceStr = xmlFileInfo.getNamespaceString();
        sb = new StringBuffer();
        
        if( namespaceStr != null ) {
            sb.append(namespaceStr);
        }
        
        String xQueryExp = DEFAULT_XQUERY;
        String rootPath = xmlFileInfo.getRootPath();
        if( rootPath != null && rootPath.length() > 0 ) {
            xQueryExp = rootPath;
        }
        sb.append(S_QUOTE).append(xQueryExp).append(S_QUOTE);
        
        tokens.add(sb.toString());
        
        sb = new StringBuffer();
        i=0;
        for( TeiidXmlColumnInfo columnInfo : columnInfoList) {
            if( columnInfo.getOrdinality() ) {
                sb.append(columnInfo.getSymbolName()).append(SPACE).append(FOR_ORDINALITY);
            } else {
                sb.append(columnInfo.getSymbolName()).append(SPACE).append(columnInfo.getDatatype());
                
                String defValue = columnInfo.getDefaultValue();
                if( defValue != null && defValue.length() > 0) {
                    sb.append(SPACE).append(DEFAULT).append(SPACE).append(S_QUOTE).append(defValue).append(S_QUOTE);
                }
                
                String relPath = columnInfo.getRelativePath();
                if( relPath != null && relPath.length() > 1 ) {
                    sb.append(SPACE).append(PATH).append(SPACE).append(S_QUOTE).append(relPath).append(S_QUOTE);
                }
                
                
            }
            if(i < (nColumns-1)) {
                sb.append(COMMA).append(SPACE);
            }

            i++;
        }

        tokens.add(sb.toString());
        tokens.add(alias);

        String finalSQLString = null;
        
        if( xmlFileInfo.isUrl() ) {
            // SELECT {0} FROM (EXEC {1}.getTextFiles({2})) AS f, XMLTABLE('{3}' PASSING XMLPARSE(DOCUMENT f.file) AS d COLUMNS {4}) AS {5}
            finalSQLString = Messages.getString(Messages.ProcedureService.procedureServiceXmlInvokeHttpTableSqlTemplate, tokens.toArray(new Object[0])); 
        } else {
            finalSQLString = Messages.getString(Messages.ProcedureService.procedureServiceXmlGetTextFilesTableSqlTemplate, tokens.toArray(new Object[0])); 
        }
        return finalSQLString;
    }
    
    @Override
    public String getSQLStatement(WsdlWrapperInfo wrapperInfo) {
        WsdlWrapperHelper helper = new WsdlWrapperHelper(teiidVersion, wrapperInfo);
        return helper.getWrapperStatement();
    }
    
    @Override
    public String getSQLStatement(WsdlWrapperInfo wrapperInfo, Properties properties) {
        WsdlWrapperHelper helper = new WsdlWrapperHelper(teiidVersion, wrapperInfo);
        return helper.getWrapperProcedureStatement(properties);
    }
    
    @Override
    public String getSQLStatement(WsdlRequestInfo requestInfo, Properties properties) {
        WsdlRequestProcedureHelper helper = new WsdlRequestProcedureHelper(teiidVersion, requestInfo, properties);
        return helper.getSQLStatement();
    }
    
    @Override
    public String getSQLStatement(WsdlResponseInfo responseInfo, Properties properties) {
        WsdlResponseProcedureHelper helper = new WsdlResponseProcedureHelper(teiidVersion, responseInfo, properties);
        return helper.getSQLStatement();
    }
}
