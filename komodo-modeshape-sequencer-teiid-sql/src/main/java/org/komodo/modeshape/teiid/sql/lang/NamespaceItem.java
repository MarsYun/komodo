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
package org.komodo.modeshape.teiid.sql.lang;

import org.komodo.modeshape.teiid.parser.TeiidSeqParser;
import org.komodo.spi.lexicon.TeiidSqlLexicon;

/**
 *
 */
public class NamespaceItem extends ASTNode {

    /**
     * @param p teiid parser
     * @param id node type id
     */
    public NamespaceItem(TeiidSeqParser p, int id) {
        super(p, id);
    }

    /**
     * @return uri
     */
    public String getUri() {
        Object property = getProperty(TeiidSqlLexicon.NamespaceItem.URI_PROP_NAME);
        return property == null ? null : property.toString();
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        setProperty(TeiidSqlLexicon.NamespaceItem.URI_PROP_NAME, uri);
    }

    /**
     * @return prefix
     */
    public String getPrefix() {
        Object property = getProperty(TeiidSqlLexicon.NamespaceItem.PREFIX_PROP_NAME);
        return property == null ? null : property.toString();
    }

    /**
     * @param prefix the prefix to set
    */
    public void setPrefix(String prefix) {
        setProperty(TeiidSqlLexicon.NamespaceItem.PREFIX_PROP_NAME, prefix);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getPrefix() == null) ? 0 : this.getPrefix().hashCode());
        result = prime * result + ((this.getUri() == null) ? 0 : this.getUri().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NamespaceItem other = (NamespaceItem)obj;
        if (this.getPrefix() == null) {
            if (other.getPrefix() != null)
                return false;
        } else if (!this.getPrefix().equals(other.getPrefix()))
            return false;
        if (this.getUri() == null) {
            if (other.getUri() != null)
                return false;
        } else if (!this.getUri().equals(other.getUri()))
            return false;
        return true;
    }

    @Override
    public NamespaceItem clone() {
        NamespaceItem clone = new NamespaceItem(this.getTeiidParser(), this.getId());
        clone.setUri(getUri());
        clone.setPrefix(getPrefix());
        return clone;
    }

}
