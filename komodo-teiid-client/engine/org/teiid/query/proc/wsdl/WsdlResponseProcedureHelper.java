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
package org.teiid.query.proc.wsdl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.komodo.spi.query.proc.wsdl.WsdlColumnInfo;
import org.komodo.spi.query.proc.wsdl.WsdlConstants;
import org.komodo.spi.query.proc.wsdl.WsdlResponseInfo;
import org.komodo.spi.query.proc.wsdl.WsdlWrapperInfo;
import org.komodo.spi.query.sql.SQLConstants;
import org.komodo.spi.runtime.version.TeiidVersion;

/**
 *
 */
public class WsdlResponseProcedureHelper extends AbstractWsdlHelper implements WsdlConstants, SQLConstants {

    private final WsdlResponseInfo responseInfo;
    
    private final Properties properties;

    /**
     * @param teiidVersion 
     * @param responseInfo
     * @param properties
     */
    public WsdlResponseProcedureHelper(TeiidVersion teiidVersion, WsdlResponseInfo responseInfo, Properties properties) {
        super(teiidVersion);
        this.responseInfo = responseInfo;
        this.properties = properties;
    }
    
    private WsdlWrapperInfo getWrapperProcedure() {
        return responseInfo.getWrapperProcedure();
    }
    
    private String getXmlTableString() {
        
        // Response procedure name may have been overridden
        String responseProcedureName = properties.getProperty(KEY_RESPONSE_PROCEDURE_NAME);
        if( responseProcedureName == null ) {
            responseProcedureName = responseInfo.getProcedureName();
        }
        
        StringBuilder sb = new StringBuilder();
        String namespaceStr = getNamespaceString();

        if (namespaceStr != null) {
            sb.append(namespaceStr);
        }

        String xQueryExp = DEFAULT_XQUERY;
        String rootPath = responseInfo.getRootPath();
        if (rootPath != null && rootPath.length() > 0) {
            xQueryExp = rootPath;
        }
        sb.append(S_QUOTE).append(xQueryExp).append(S_QUOTE).append(SPACE);

        sb.append(PASSING)
                .append(SPACE)
                .append(convertSqlNameSegment(
                        getResponseProcedureParameter(responseProcedureName))).append(RETURN);

        sb.append(TAB).append(COLUMNS).append(SPACE).append(RETURN);

        int i = 0;
        WsdlColumnInfo[] bodyColumnInfoList = responseInfo.getBodyColumnInfoList();
        int nColumns = bodyColumnInfoList.length;

        for (WsdlColumnInfo columnInfo : bodyColumnInfoList) {
            if (columnInfo.getOrdinality()) {
                sb.append(columnInfo.getSymbolName()).append(SPACE)
                        .append(FOR_ORDINALITY);
            } else {
                sb.append(TAB)
                        .append(TAB)
                        .append(convertSqlNameSegment(
                                columnInfo.getSymbolName())).append(SPACE)
                        .append(columnInfo.getDatatype());

                String defValue = columnInfo.getDefaultValue();
                if (defValue != null && defValue.length() > 0) {
                    sb.append(SPACE).append(DEFAULT).append(SPACE)
                            .append(S_QUOTE).append(defValue).append(S_QUOTE);
                }

                String relPath = columnInfo.getRelativePath();
                if (relPath != null && relPath.length() > 1) {
                    //Strip out default namespace prefix
                    //relPath = relPath.replace(COLON, EMPTY_STR);
                    sb.append(SPACE).append(PATH).append(SPACE).append(S_QUOTE)
                            .append(relPath).append(S_QUOTE);
                }

            }
            if (i < (nColumns - 1)) {
                sb.append(COMMA).append(SPACE).append(RETURN);
            }

            i++;
        }

        return sb.toString();
    }

    private String getNamespaceString() {
        //
        // EXAMPLE: XMLNAMESPACES('http://www.kaptest.com/schema/1.0/party' AS
        // pty)
        //

        Map<String, String> namespaceMap = responseInfo.getNamespaceMap();
        Object nsObject = responseInfo.getOperation().getBinding().getPort().getNamespaceURI();
        if (namespaceMap.isEmpty() && nsObject==null) {
            return null;
        }
        
//        if (nsObject!=null){
//        	namespaceMap.put();
//        }
//       

        StringBuffer sb = new StringBuffer();

        sb.append(XMLNAMESPACES).append(L_PAREN);
        int i = 0;
        for (Entry<String, String> entry : namespaceMap.entrySet()) {
            if (entry.getKey()==null || entry.getKey().equalsIgnoreCase(XSI_NAMESPACE_PREFIX)) {
                continue;
            }
            
            if (i > 0) {
                sb.append(COMMA).append(SPACE);
            }
            
            sb.append(S_QUOTE).append(entry.getValue()).append(S_QUOTE).append(SPACE)
                        .append(AS).append(SPACE).append(entry.getKey());
            i++;
        }
        sb.append(R_PAREN).append(SPACE).append(COMMA).append(SPACE);

        return sb.toString();
    }

    private String getResponseProcedureParameter(String procedureName) {
        StringBuilder sb = new StringBuilder();
        sb.append(getWrapperProcedure().getViewModelName()).append(DOT)
                .append(procedureName).append(DOT)
                .append("xml_in").append(SPACE); //$NON-NLS-1$
        return sb.toString();
    }

    /**
     * Generate the SQL statement for this response info
     * 
     * @return SQL string
     */
    public String getSQLStatement() {
        // Generated SQL example for extract:
        //
        // CREATE VIRTUAL PROCEDURE
        // BEGIN
        //  SELECT t.* FROM XMLTABLE(XMLNAMESPACES(DEFAULT
        // 'http://quickstart.samples/xsd'), '/getPriceResponse' PASSING xml_in
        // COLUMNS return_ double) AS t;
        // END
        // =============================================
        // Generated SQL example for extract:
        //
        // CREATE VIRTUAL PROCEDURE
        // BEGIN
        // SELECT t.* FROM
        // XMLTABLE( XMLNAMESPACES(DEFAULT
        // 'http://www.oorsprong.org/websamples.countryinfo'),
        //  '/CapitalCityResponse'
        //  PASSING
        // COUNTRYINFOSERVICEXML.CAPITALCITY.EXTRACT_CAPITALCITYRESPONSE.xml_in
        //  COLUMNS CapitalCityResult string)
        //  AS t;
        // END
        //
        // CREATE VIRTUAL PROCEDURE
        // BEGIN
        // END
        //

        String alias = "t"; //$NON-NLS-1$
        StringBuffer sb = new StringBuffer();

        sb.append(SQL_BEGIN);
        sb.append(TAB).append(SELECT).append(SPACE).append(alias).append(DOT)
                .append(STAR).append(SPACE).append(FROM).append(RETURN);
        sb.append(TAB).append(TAB).append(XMLTABLE).append(L_PAREN);

        sb.append(getXmlTableString());

        sb.append(R_PAREN);
        sb.append(SPACE).append(AS).append(SPACE).append(alias)
                .append(SEMI_COLON);
        sb.append(SQL_END);

        return sb.toString();
    }

}
