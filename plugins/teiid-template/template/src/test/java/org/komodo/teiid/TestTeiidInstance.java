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
package org.komodo.teiid;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;
import org.komodo.spi.runtime.TeiidInstance;
import org.komodo.spi.runtime.TeiidJdbcInfo;
import org.komodo.spi.runtime.TeiidParent;
import org.komodo.spi.runtime.version.DefaultTeiidVersion;
import org.komodo.spi.runtime.version.TeiidVersion;

@RunWith( Arquillian.class )
public class TestTeiidInstance extends AbstractTestTeiidInstance {

    @Override
    protected TeiidInstance createTeiidInstance(TeiidParent parent,
                                                                                    TeiidVersion teiidVersion,
                                                                                    TeiidJdbcInfo jdbcInfo) throws Exception {
        return new TeiidInstanceImpl(parent, teiidVersion, jdbcInfo);
    }

    @Override
    protected TeiidVersion getVersion() {
        return new DefaultTeiidVersion("%TEIID_VERSION%");
    }
}
