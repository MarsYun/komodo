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
package org.komodo.relational.commands.column;

import java.util.List;
import org.komodo.relational.RelationalConstants;
import org.komodo.relational.commands.workspace.WorkspaceCommandsI18n;
import org.komodo.relational.model.Column;
import org.komodo.shell.CommandResultImpl;
import org.komodo.shell.api.Arguments;
import org.komodo.shell.api.CommandResult;
import org.komodo.shell.api.TabCompletionModifier;
import org.komodo.shell.api.WorkspaceStatus;
import org.komodo.shell.commands.UnsetPropertyCommand;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.utils.StringUtils;
import org.komodo.utils.i18n.I18n;

/**
 * A shell command to unset {@link Column column} properties.
 */
public final class UnsetColumnPropertyCommand extends ColumnShellCommand {

    static final String NAME = UnsetPropertyCommand.NAME;

    /**
     * @param status
     *        the shell's workspace status (cannot be <code>null</code>)
     */
    public UnsetColumnPropertyCommand( final WorkspaceStatus status ) {
        super( NAME, status );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#doExecute()
     */
    @Override
    protected CommandResult doExecute() {
        CommandResult result = null;

        try {
            final String name = requiredArgument( 0, I18n.bind( WorkspaceCommandsI18n.unsetMissingPropertyName ) );
            final Column column = getColumn();
            final UnitOfWork transaction = getTransaction();
            String errorMsg = null;

            switch ( name ) {
                case AUTO_INCREMENTED:
                    column.setAutoIncremented( transaction, Column.DEFAULT_AUTO_INCREMENTED );
                    break;
                case CASE_SENSITIVE:
                    column.setCaseSensitive( transaction, Column.DEFAULT_CASE_SENSITIVE );
                    break;
                case CHAR_OCTET_LENGTH:
                    column.setCharOctetLength(transaction, Column.DEFAULT_CHAR_OCTET_LENGTH);
                    break;
                case COLLATION_NAME:
                    column.setCollationName( transaction, null );
                    break;
                case CURRENCY:
                    column.setCurrency( transaction, Column.DEFAULT_CURRENCY );
                    break;
                case DATATYPE_NAME:
                    column.setDatatypeName( transaction, null );
                    break;
                case DEFAULT_VALUE:
                    column.setDefaultValue( transaction, null );
                    break;
                case DESCRIPTION:
                    column.setDescription( transaction, null );
                    break;
                case DISTINCT_VALUES:
                    column.setDistinctValues( transaction, Column.DEFAULT_DISTINCT_VALUES );
                    break;
                case FIXED_LENGTH:
                    column.setFixedLength( transaction, Column.DEFAULT_FIXED_LENGTH );
                    break;
                case LENGTH:
                    column.setLength( transaction, RelationalConstants.DEFAULT_LENGTH );
                    break;
                case MAX_VALUE:
                    column.setMaxValue( transaction, null );
                    break;
                case MIN_VALUE:
                    column.setMinValue( transaction, null );
                    break;
                case NAME_IN_SOURCE:
                    column.setNameInSource( transaction, null );
                    break;
                case NATIVE_TYPE:
                    column.setNativeType( transaction, null );
                    break;
                case NULLABLE:
                    column.setNullable( transaction, null );
                    break;
                case NULL_VALUE_COUNT:
                    column.setNullValueCount( transaction, Column.DEFAULT_NULL_VALUE_COUNT );
                    break;
                case PRECISION:
                    column.setPrecision( transaction, RelationalConstants.DEFAULT_PRECISION );
                    break;
                case RADIX:
                    column.setRadix( transaction, Column.DEFAULT_RADIX );
                    break;
                case SCALE:
                    column.setScale( transaction, RelationalConstants.DEFAULT_SCALE );
                    break;
                case SEARCHABLE:
                    column.setSearchable( transaction, null );
                    break;
                case SELECTABLE:
                    column.setSelectable( transaction, Column.DEFAULT_SELECTABLE );
                    break;
                case SIGNED:
                    column.setSigned( transaction, Column.DEFAULT_SIGNED );
                    break;
                case UPDATABLE:
                    column.setUpdatable( transaction, Column.DEFAULT_UPDATABLE );
                    break;
                case UUID:
                    column.setUuid( transaction, null );
                    break;
                default:
                    errorMsg = I18n.bind( WorkspaceCommandsI18n.invalidPropertyName, name, Column.class.getSimpleName() );
                    break;
            }

            if ( StringUtils.isBlank( errorMsg ) ) {
                result = new CommandResultImpl( I18n.bind( WorkspaceCommandsI18n.unsetPropertySuccess, column.getName( transaction ), name ) );
            } else {
                result = new CommandResultImpl( false, errorMsg, null );
            }
        } catch ( final Exception e ) {
            result = new CommandResultImpl( e );
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#getMaxArgCount()
     */
    @Override
    protected int getMaxArgCount() {
        return 1;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#printHelpDescription(int)
     */
    @Override
    protected void printHelpDescription( final int indent ) {
        print( indent, I18n.bind( ColumnCommandsI18n.unsetColumnPropertyHelp, getName() ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#printHelpExamples(int)
     */
    @Override
    protected void printHelpExamples( final int indent ) {
        print( indent, I18n.bind( ColumnCommandsI18n.unsetColumnPropertyExamples ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#printHelpUsage(int)
     */
    @Override
    protected void printHelpUsage( final int indent ) {
        print( indent, I18n.bind( ColumnCommandsI18n.unsetColumnPropertyUsage ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.shell.BuiltInShellCommand#tabCompletion(java.lang.String, java.util.List)
     */
    @Override
    public TabCompletionModifier tabCompletion( final String lastArgument,
                              final List< CharSequence > candidates ) throws Exception {
        final Arguments args = getArguments();

        if ( args.isEmpty() ) {
            if ( lastArgument == null ) {
                candidates.addAll( ALL_PROPS );
            } else {
                for ( final String item : ALL_PROPS ) {
                    if ( item.toUpperCase().startsWith( lastArgument.toUpperCase() ) ) {
                        candidates.add( item );
                    }
                }
            }
        }
        return TabCompletionModifier.AUTO;
    }

}
