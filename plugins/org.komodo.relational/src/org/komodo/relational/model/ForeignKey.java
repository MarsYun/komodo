/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.model;

import org.komodo.spi.KException;
import org.komodo.spi.repository.Repository.UnitOfWork;

/**
 * Represents a relational model foreign key.
 */
public interface ForeignKey extends TableConstraint {

    /**
     * The type identifier.
     */
    int TYPE_ID = ForeignKey.class.hashCode();

    /**
     * The constraint type for a foreign key. Value is {@value} .
     */
    ConstraintType CONSTRAINT_TYPE = ConstraintType.FOREIGN_KEY;

    /**
     * An empty collection of foreign key constraints.
     */
    ForeignKey[] NO_FOREIGN_KEYS = new ForeignKey[0];

    /**
     * @param transaction
     *        the transaction (can be <code>null</code> if update should be automatically committed)
     * @param newReferencesColumn
     *        the references table columns being added (cannot be <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    void addReferencesColumn( final UnitOfWork transaction,
                              final Column newReferencesColumn ) throws KException;

    /**
     * @param transaction
     *        the transaction (can be <code>null</code> if query should be automatically committed)
     * @return the columns referenced from the references table (never <code>null</code> but can be empty)
     * @throws KException
     *         if an error occurs
     */
    Column[] getReferencesColumns( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (can be <code>null</code> if query should be automatically committed)
     * @return the references table (can be <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    Table getReferencesTable( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (can be <code>null</code> if update should be automatically committed)
     * @param referencesColumnToRemove
     *        the references table column being removed (cannot be <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    void removeReferencesColumn( final UnitOfWork transaction,
                                 final Column referencesColumnToRemove ) throws KException;

    /**
     * @param transaction
     *        the transaction (can be <code>null</code> if update should be automatically committed)
     * @param newReferencesTable
     *        the new value for the references table (cannot be <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    void setReferencesTable( final UnitOfWork transaction,
                             final Table newReferencesTable ) throws KException;

}
