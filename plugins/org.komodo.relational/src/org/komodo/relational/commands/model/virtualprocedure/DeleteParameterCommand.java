/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.commands.model.virtualprocedure;

import static org.komodo.relational.commands.model.virtualprocedure.VirtualProcedureCommandMessages.DeleteParameterCommand.PARAMETER_DELETED;
import static org.komodo.relational.commands.model.virtualprocedure.VirtualProcedureCommandMessages.General.MISSING_PARAMETER_NAME;
import static org.komodo.shell.CompletionConstants.MESSAGE_INDENT;
import java.util.ArrayList;
import java.util.List;
import org.komodo.relational.model.Parameter;
import org.komodo.relational.model.VirtualProcedure;
import org.komodo.shell.api.Arguments;
import org.komodo.shell.api.WorkspaceStatus;
import org.komodo.spi.repository.Repository.UnitOfWork;

/**
 * A shell command to delete a Parameter from a VirtualProcedure.
 */
public final class DeleteParameterCommand extends VirtualProcedureShellCommand {

    static final String NAME = "delete-vp-parameter"; //$NON-NLS-1$

    /**
     * @param status
     *        the shell's workspace status (cannot be <code>null</code>)
     */
    public DeleteParameterCommand( final WorkspaceStatus status ) {
        super( NAME, true, status );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#doExecute()
     */
    @Override
    protected boolean doExecute() throws Exception {
        final String paramName = requiredArgument( 0, getMessage(MISSING_PARAMETER_NAME) );

        final VirtualProcedure proc = getVirtualProcedure();
        proc.removeParameter( getTransaction(), paramName );

        // Print success message
        print(MESSAGE_INDENT, getMessage(PARAMETER_DELETED,paramName));
        
        return true;
    }
    
    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#tabCompletion(java.lang.String, java.util.List)
     */
    @Override
    public int tabCompletion( final String lastArgument,
                              final List< CharSequence > candidates ) throws Exception {
        final Arguments args = getArguments();

        final UnitOfWork uow = getTransaction();
        final VirtualProcedure proc = getVirtualProcedure();
        final Parameter[] parameters = proc.getParameters( uow );
        List<String> existingParamNames = new ArrayList<String>(parameters.length);
        for(Parameter parameter : parameters) {
            existingParamNames.add(parameter.getName(uow));
        }
        
        if ( args.isEmpty() ) {
            if ( lastArgument == null ) {
                candidates.addAll( existingParamNames );
            } else {
                for ( final String item : existingParamNames ) {
                    if ( item.toUpperCase().startsWith( lastArgument.toUpperCase() ) ) {
                        candidates.add( item );
                    }
                }
            }

            return 0;
        }

        // no tab completion
        return -1;
    }
    
}
