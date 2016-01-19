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

package org.teiid.query.validator;

import java.util.Iterator;

import org.komodo.spi.query.metadata.QueryMetadataInterface;
import org.komodo.spi.validator.Validator;
import org.teiid.query.metadata.TempMetadataAdapter;
import org.teiid.query.metadata.TempMetadataStore;
import org.teiid.query.sql.lang.CommandImpl;
import org.teiid.query.sql.lang.BaseLanguageObject;
import org.teiid.query.sql.navigator.PreOrderNavigator;
import org.teiid.query.sql.visitor.CommandCollectorVisitorImpl;


public class DefaultValidator implements Validator<BaseLanguageObject> {

    @Override
    public ValidatorReport validate(BaseLanguageObject object, QueryMetadataInterface metadata) throws Exception {
        ValidatorReport report1 = validate(object, metadata, new ValidationVisitorImpl(object.getTeiidVersion()));
        return report1;
    }

    public static final ValidatorReport validate(BaseLanguageObject object, QueryMetadataInterface metadata, AbstractValidationVisitor visitor)
        throws Exception {

        // Execute on this command
        executeValidation(object, metadata, visitor);

        // Construct combined runtime / query metadata if necessary
        if(object instanceof CommandImpl) {                        
            // Recursively validate subcommands
            Iterator<CommandImpl> iter = CommandCollectorVisitorImpl.getCommands((CommandImpl)object).iterator();
            while(iter.hasNext()) {
                CommandImpl subCommand = iter.next();
                validate(subCommand, metadata, visitor);
            }
        }
        
        // Otherwise, return a report
        return visitor.getReport();
    }

    private static final void executeValidation(BaseLanguageObject object, final QueryMetadataInterface metadata, final AbstractValidationVisitor visitor) 
        throws Exception {

        // Reset visitor
        visitor.reset();

		visitor.setMetadata(metadata);
        setTempMetadata(metadata, visitor, object);
        
        PreOrderNavigator nav = new PreOrderNavigator(visitor) {
        	
        	@Override
            protected void visitNode(BaseLanguageObject obj) {
        		QueryMetadataInterface previous = visitor.getMetadata();
        		setTempMetadata(metadata, visitor, obj);
        		super.visitNode(obj);
        		visitor.setMetadata(previous);
        	}
        	
        	@Override
        	protected void preVisitVisitor(BaseLanguageObject obj) {
        		super.preVisitVisitor(obj);
        		visitor.stack.add(obj);
        	}
        	
        	@Override
        	protected void postVisitVisitor(BaseLanguageObject obj) {
        		visitor.stack.pop();
        	}
        	
        };
        object.acceptVisitor(nav);        	
        
        // If an error occurred, throw an exception
        Exception e = visitor.getException();
        if(e != null) { 
            throw e;
        }                
    }
    
	private static void setTempMetadata(final QueryMetadataInterface metadata,
			final AbstractValidationVisitor visitor,
			BaseLanguageObject obj) {
		if (obj instanceof CommandImpl) {
			CommandImpl command = (CommandImpl)obj;
			visitor.currentCommand = command;
			TempMetadataStore tempMetadata = command.getTemporaryMetadata();
            if(tempMetadata != null && !tempMetadata.getData().isEmpty()) {
            	visitor.setMetadata(new TempMetadataAdapter(metadata, tempMetadata));
            }    
		}
	}
    
}    
