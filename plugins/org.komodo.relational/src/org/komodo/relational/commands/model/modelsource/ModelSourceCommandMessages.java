/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.commands.model.modelsource;

import java.util.ResourceBundle;
import org.komodo.relational.vdb.ModelSource;
import org.komodo.spi.constants.StringConstants;

/**
 * Localized messages for {@link ModelSource}-related shell commands.
 */
public class ModelSourceCommandMessages implements StringConstants {

    private static final String BUNDLE_NAME = ( ModelSourceCommandMessages.class.getPackage().getName() + DOT + ModelSourceCommandMessages.class.getSimpleName().toLowerCase() );

    /**
     * The resource bundle for localized messages.
     */
    public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );
    
    @SuppressWarnings( "javadoc" )
    public enum General {
        INVALID_SEARCHABLE_PROPERTY_VALUE,
        INVALID_NULLABLE_PROPERTY_VALUE;

        @Override
        public String toString() {
            return getEnumName(this) + DOT + name();
        }
    }
    
    private static String getEnumName(Enum<?> enumValue) {
        String className = enumValue.getClass().getName();
        String[] components = className.split("\\$"); //$NON-NLS-1$
        return components[components.length - 1];
    }

}
