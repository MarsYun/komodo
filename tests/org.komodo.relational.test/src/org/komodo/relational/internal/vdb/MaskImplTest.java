/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.internal.vdb;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.komodo.relational.RelationalModelTest;
import org.komodo.relational.internal.RelationalModelFactory;
import org.komodo.relational.vdb.DataRole;
import org.komodo.relational.vdb.Mask;
import org.komodo.relational.vdb.Permission;
import org.komodo.relational.vdb.Vdb;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.modeshape.sequencer.teiid.lexicon.VdbLexicon;

@SuppressWarnings( {"javadoc", "nls"} )
public final class MaskImplTest extends RelationalModelTest {

    private static int _counter = 1;

    private DataRole dataRole;
    private Mask mask;
    private Permission permission;
    private Vdb vdb;

    private void create() throws Exception {
        final UnitOfWork transaction = _repo.createTransaction(this.name.getMethodName(), false, null); // won't work inside an @Before

        final int suffix = _counter++;
        this.vdb = RelationalModelFactory.createVdb(transaction, _repo, null, ("vdb" + suffix), "/Users/sledge/hammer/MyVdb.vdb");
        this.dataRole = RelationalModelFactory.createDataRole(transaction, _repo, this.vdb, ("dataRole" + suffix));
        this.permission = RelationalModelFactory.createPermission(transaction, _repo, this.dataRole, ("permission" + suffix));
        this.mask = RelationalModelFactory.createMask(transaction, _repo, this.permission, ("mask" + suffix));

        transaction.commit();

        assertThat(this.vdb.getPrimaryType(null).getName(), is(VdbLexicon.Vdb.VIRTUAL_DATABASE));
        assertThat(this.dataRole.getPrimaryType(null).getName(), is(VdbLexicon.DataRole.DATA_ROLE));
        assertThat(this.permission.getPrimaryType(null).getName(), is(VdbLexicon.DataRole.Permission.PERMISSION));
        assertThat(this.mask.getPrimaryType(null).getName(), is(VdbLexicon.DataRole.Permission.Mask.MASK));
    }

    @Test
    public void shouldNotHaveOrderAfterConstruction() throws Exception {
        create();
        assertThat(this.mask.getOrder(null), is(nullValue()));
    }

    @Test
    public void shouldSetOrder() throws Exception {
        create();
        final String newValue = "newOrder";
        this.mask.setOrder(null, newValue);
        assertThat(this.mask.getOrder(null), is(newValue));
    }

}