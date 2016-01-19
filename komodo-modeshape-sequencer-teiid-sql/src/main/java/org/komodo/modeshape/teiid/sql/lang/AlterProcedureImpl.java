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

package org.komodo.modeshape.teiid.sql.lang;

import org.komodo.modeshape.teiid.parser.SQLanguageVisitorImpl;
import org.komodo.modeshape.teiid.parser.TeiidSeqParser;
import org.komodo.modeshape.teiid.sql.proc.CreateProcedureCommandImpl;
import org.komodo.modeshape.teiid.sql.symbol.BaseExpression;
import org.komodo.spi.query.sql.lang.AlterProcedure;

/**
 *
 */
public class AlterProcedureImpl extends AlterImpl<CreateProcedureCommandImpl> implements AlterProcedure<BaseExpression, SQLanguageVisitorImpl> {

    /**
     * @param p teiid parser
     * @param id node type id
     */
    public AlterProcedureImpl(TeiidSeqParser p, int id) {
        super(p, id);
        setType(TYPE_ALTER_PROC);
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(SQLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @Override
    public AlterProcedureImpl clone() {
        AlterProcedureImpl clone = new AlterProcedureImpl(this.getTeiidParser(), this.getId());

        if(getDefinition() != null)
            clone.setDefinition(getDefinition().clone());
        if(getTarget() != null)
            clone.setTarget(getTarget().clone());
        if(getSourceHint() != null)
            clone.setSourceHint(getSourceHint());
        if(getOption() != null)
            clone.setOption(getOption().clone());

        copyMetadataState(clone);

        return clone;
    }
}
