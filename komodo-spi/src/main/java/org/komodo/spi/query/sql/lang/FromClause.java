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
package org.komodo.spi.query.sql.lang;

import org.komodo.spi.query.sql.LanguageVisitor;




/**
 *
 */
public interface FromClause<LV extends LanguageVisitor> extends LanguageObject<LV> {

    /**
     * Is the clause optional
     * 
     * @return true if optional
     */
    boolean isOptional();
    
    /**
     * Set whether the clause is optional
     * 
     * @param optional
     */
    void setOptional(boolean optional);
    
    /**
     * Is make dependent
     * 
     * @return true if make dependent
     */
    boolean isMakeDep();

    /**
     * Set make dependent
     * 
     * @param makeDep
     */
    void setMakeDep(boolean makeDep);
    
    /**
     * Is make not dependent
     * 
     * @return true if make not dependent
     */
    boolean isMakeNotDep();
    
    /**
     * Set make not dependent
     * 
     * @param makeNotDep
     */
    void setMakeNotDep(boolean makeNotDep);
}
