/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.komodo.relational.commands.vdb;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;
import org.komodo.relational.commands.AbstractCommandTest;
import org.komodo.relational.vdb.DataRole;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.workspace.WorkspaceManager;
import org.komodo.shell.api.CommandResult;

/**
 * Test Class to test {@link DeleteDataRoleCommand}.
 */
@SuppressWarnings( {"javadoc", "nls"} )
public final class DeleteDataRoleCommandTest extends AbstractCommandTest {

    @Test
    public void testDelete1() throws Exception {
        final String[] commands = {
            "create-vdb myVdb vdbPath",
            "cd myVdb",
            "add-data-role myDataRole1",
            "add-data-role myDataRole2",
            "delete-data-role myDataRole1" };
        final CommandResult result = execute( commands );
        assertCommandResultOk(result);

        WorkspaceManager wkspMgr = WorkspaceManager.getInstance(_repo, getTransaction());
        Vdb[] vdbs = wkspMgr.findVdbs(getTransaction());

        assertEquals(1, vdbs.length);

        DataRole[] dataRoles = vdbs[0].getDataRoles(getTransaction());
        assertEquals(1, dataRoles.length);
        assertEquals("myDataRole2", dataRoles[0].getName(getTransaction())); //$NON-NLS-1$
    }

    @Test
    public void testTabCompleter()throws Exception{

    	ArrayList<CharSequence> candidates=new ArrayList<>();
    	candidates.add("myDatarole1");
    	candidates.add("myDatarole2");

    	setup("commandFiles","addDataRoles.cmd");
    	assertTabCompletion("delete-data-role myD", candidates);

    	candidates.add("MyDatarole3");
    	assertTabCompletion("delete-data-role ", candidates);
    }

}
