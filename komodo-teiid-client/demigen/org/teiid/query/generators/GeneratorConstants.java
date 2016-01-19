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
package org.teiid.query.generators;

import java.io.File;
import org.komodo.spi.constants.StringConstants;

/**
 *
 */
@SuppressWarnings( "nls" )
public interface GeneratorConstants extends StringConstants {

    /**
     * License text for inclusion at header to generated classes
     */
    String LICENSE = EMPTY_STRING +
    "/*" + NEW_LINE +
    " * JBoss, Home of Professional Open Source." + NEW_LINE +
    " * See the COPYRIGHT.txt file distributed with this work for information" + NEW_LINE +
    " * regarding copyright ownership.  Some portions may be licensed" + NEW_LINE +
    " * to Red Hat, Inc. under one or more contributor license agreements." + NEW_LINE +
    " * " + NEW_LINE +
    " * This library is free software; you can redistribute it and/or" + NEW_LINE +
    " * modify it under the terms of the GNU Lesser General Public" + NEW_LINE +
    " * License as published by the Free Software Foundation; either" + NEW_LINE +
    " * version 2.1 of the License, or (at your option) any later version." + NEW_LINE +
    " * " + NEW_LINE +
    " * This library is distributed in the hope that it will be useful," + NEW_LINE +
    " * but WITHOUT ANY WARRANTY; without even the implied warranty of" + NEW_LINE +
    " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU" + NEW_LINE +
    " * Lesser General Public License for more details." + NEW_LINE +
    " * " + NEW_LINE +
    " * You should have received a copy of the GNU Lesser General Public" + NEW_LINE +
    " * License along with this library; if not, write to the Free Software" + NEW_LINE +
    " * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA" + NEW_LINE +
    " * 02110-1301 USA." + NEW_LINE +
    " */" + NEW_LINE;

    /**
     * Directory of execution
     */
    String EXEC_HOME = DOT;

    /**
     * Source Directory
     */
    String SRC_DIR = EXEC_HOME + File.separator + SRC;

    /**
     * Generated Root Directory
     */
    String GEN_DIR = EXEC_HOME + File.separator + "gen";

    /**
     * Demi-Generated Root Directory
     */
    String DEMI_GEN_DIR = EXEC_HOME + File.separator + "demigen";

    /**
     * Language object interface prefix
     */
    String LANG_OBJECT_PREFIX = "Base";

    /**
     * Language object post fix
     */
    String LANG_OBJECT_POSTFIX = "Impl";

    /**
     * Utilities for source generation
     */
    class Utilities {

        public static String convertPackageToDirPath(Package pkg) {
            return pkg.getName().replaceAll(DOUBLE_BACK_SLASH + DOT, File.separator);
        }

    }
    
    
    
    
    
    
    
    
    
    
    
    
}
