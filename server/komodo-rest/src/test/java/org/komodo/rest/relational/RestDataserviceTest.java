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
package org.komodo.rest.relational;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.junit.Before;
import org.junit.Test;
import org.komodo.relational.dataservice.Dataservice;
import org.komodo.rest.relational.dataservice.RestDataservice;
import org.komodo.spi.repository.Descriptor;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.PropertyDescriptor;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.mockito.Mockito;
import org.teiid.modeshape.sequencer.vdb.lexicon.VdbLexicon;

@SuppressWarnings( {"javadoc", "nls"} )
public final class RestDataserviceTest {

    private static final URI BASE_URI = UriBuilder.fromUri("http://localhost:8081/v1/").build();
    private static final String WORKSPACE_DATA_PATH = "/workspace";
    private static final String DATASERVICE_NAME = "MyDataservice";
    private static final String DATASERVICE_DATA_PATH = "/workspace/dataservices/dataservice1";
    private static final KomodoType kType = KomodoType.DATASERVICE;
    private static final String DESCRIPTION = "my description";
    private static final String ORIGINAL_FILE = "/Users/ElvisIsKing/MyVdb.xml";
    private static final String CONNECTION_TYPE = "BY_VERSION";
    private static final int VERSION = 1;

    private RestDataservice dataservice;

    private RestDataservice copy() {
        final RestDataservice copy = new RestDataservice();

        copy.setBaseUri(dataservice.getBaseUri());
        copy.setId(dataservice.getName());
        copy.setDataPath(dataservice.getDataPath());
        copy.setkType(dataservice.getkType());
        copy.setHasChildren(dataservice.hasChildren());
        copy.setName(this.dataservice.getName());
        copy.setDescription(this.dataservice.getDescription());
        copy.setOriginalFilePath(this.dataservice.getOriginalFilePath());
        copy.setConnectionType(this.dataservice.getConnectionType());
        copy.setPreview(this.dataservice.isPreview());
        copy.setVersion(this.dataservice.getVersion());
        copy.setLinks(this.dataservice.getLinks());
        copy.setProperties(this.dataservice.getProperties());

        return copy;
    }

    @Before
    public void init() throws Exception {
        UnitOfWork transaction = Mockito.mock(UnitOfWork.class);

        KomodoObject workspace = Mockito.mock(KomodoObject.class);
        Mockito.when(workspace.getAbsolutePath()).thenReturn(WORKSPACE_DATA_PATH);

        Descriptor dataserviceType = Mockito.mock(Descriptor.class);
        when(dataserviceType.getName()).thenReturn(VdbLexicon.Vdb.VIRTUAL_DATABASE);

        Dataservice theDataservice = Mockito.mock(Dataservice.class);
        Mockito.when(theDataservice.getPrimaryType(transaction)).thenReturn(dataserviceType);
        Mockito.when(theDataservice.getName(transaction)).thenReturn(DATASERVICE_NAME);
        Mockito.when(theDataservice.getAbsolutePath()).thenReturn(DATASERVICE_DATA_PATH);
        Mockito.when(theDataservice.getTypeIdentifier(transaction)).thenReturn(kType);
        Mockito.when(theDataservice.hasChildren(transaction)).thenReturn(true);
        Mockito.when(theDataservice.getPropertyNames(transaction)).thenReturn(new String[0]);
        Mockito.when(theDataservice.getPropertyDescriptors(transaction)).thenReturn(new PropertyDescriptor[0]);
        Mockito.when(theDataservice.getParent(transaction)).thenReturn(workspace);

        this.dataservice = new RestDataservice(BASE_URI, theDataservice, false, transaction);
        this.dataservice.setName(DATASERVICE_NAME);
        this.dataservice.setDescription(DESCRIPTION);
        this.dataservice.setOriginalFilePath(ORIGINAL_FILE);
        this.dataservice.setConnectionType(CONNECTION_TYPE);
        this.dataservice.setPreview(false);
        this.dataservice.setVersion(VERSION);
    }

    @Test
    public void shouldHaveBaseUri() {
        assertEquals(BASE_URI, this.dataservice.getBaseUri());
    }

    @Test
    public void shouldBeEqual() {
        final RestDataservice thatDataservice = copy();
        assertEquals(this.dataservice, thatDataservice);
    }

    @Test
    public void shouldBeEqualWhenComparingEmptyDataservices() {
        final RestDataservice empty1 = new RestDataservice();
        final RestDataservice empty2 = new RestDataservice();
        assertEquals(empty1, empty2);
    }

    @Test
    public void shouldConstructEmptyDataservice() {
        final RestDataservice empty = new RestDataservice();
        assertNull(empty.getBaseUri());
        assertNull(empty.getName());
        assertNull(empty.getDescription());
        assertNull(empty.getOriginalFilePath());
        assertEquals(empty.getProperties().isEmpty(), true);
        assertEquals(empty.getLinks().size(), 0);
    }

    @Test
    public void shouldHaveSameHashCode() {
        final RestDataservice thatDataservice = copy();
        assertEquals(this.dataservice.hashCode(), thatDataservice.hashCode());
    }

    @Test
    public void shouldNotBeEqualWhenDescriptionIsDifferent() {
        final RestDataservice thatDataservice = copy();
        thatDataservice.setDescription(this.dataservice.getDescription() + "blah");
        assertNotEquals(this.dataservice, not(thatDataservice));
    }

    @Test
    public void shouldNotBeEqualWhenNameIsDifferent() {
        final RestDataservice thatDataservice = copy();
        thatDataservice.setName(this.dataservice.getName() + "blah");
        assertNotEquals(this.dataservice, thatDataservice);
    }

    @Test
    public void shouldNotBeEqualWhenOriginalFileIsDifferent() {
        final RestDataservice thatDataservice = copy();
        thatDataservice.setOriginalFilePath(this.dataservice.getOriginalFilePath() + "blah");
        assertNotEquals(this.dataservice, thatDataservice);
    }

    @Test
    public void shouldSetDescription() {
        final String newDescription = "blah";
        this.dataservice.setDescription(newDescription);
        assertEquals(this.dataservice.getDescription(), newDescription);
    }

    @Test
    public void shouldSetName() {
        final String newName = "blah";
        this.dataservice.setName(newName);
        assertEquals(this.dataservice.getName(), newName);
    }

    @Test
    public void shouldSetOriginalFilePath() {
        final String newPath = "blah";
        this.dataservice.setOriginalFilePath(newPath);
        assertEquals(this.dataservice.getOriginalFilePath(), newPath);
    }

}