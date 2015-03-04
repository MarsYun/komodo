/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.model.internal;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import org.junit.Before;
import org.junit.Test;
import org.komodo.relational.RelationalModelTest;
import org.komodo.relational.internal.RelationalModelFactory;
import org.komodo.relational.internal.RelationalObjectImpl;
import org.komodo.relational.model.AccessPattern;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.Table;
import org.komodo.relational.model.TableConstraint;
import org.komodo.spi.KException;
import org.modeshape.sequencer.ddl.dialect.teiid.TeiidDdlLexicon;

@SuppressWarnings( {"javadoc", "nls"} )
public class AccessPatternImplTest extends RelationalModelTest {

    private static final String NAME = "accesspattern";

    private AccessPattern accessPattern;
    private Table table;

    @Before
    public void init() throws Exception {
        this.table = RelationalModelFactory.createTable(null, _repo, mock(Model.class), "table");
        this.accessPattern = RelationalModelFactory.createAccessPattern(null, _repo, this.table, NAME);
    }

    @Test
    public void shouldFailConstructionIfNotAccessPattern() {
        if (RelationalObjectImpl.VALIDATE_INITIAL_STATE) {
            try {
                new AccessPatternImpl(null, _repo, this.table.getAbsolutePath());
                fail();
            } catch (final KException e) {
                // expected
            }
        }
    }

    @Test
    public void shouldHaveCorrectConstraintType() throws Exception {
        assertThat(this.accessPattern.getConstraintType(), is(TableConstraint.ConstraintType.ACCESS_PATTERN));
        assertThat(this.accessPattern.getProperty(null, TeiidDdlLexicon.Constraint.TYPE).getStringValue(null),
                   is(TableConstraint.ConstraintType.ACCESS_PATTERN.toValue()));
    }

    @Test
    public void shouldHaveCorrectDescriptor() throws Exception {
        assertThat(this.accessPattern.hasDescriptor(null, TeiidDdlLexicon.Constraint.TABLE_ELEMENT), is(true));
    }

    @Test
    public void shouldHaveCorrectName() throws Exception {
        assertThat(this.accessPattern.getName(null), is(NAME));
    }

    @Test
    public void shouldHaveParentTable() throws Exception {
        assertThat(this.accessPattern.getParent(null), is(instanceOf(Table.class)));
    }

    @Test
    public void shouldHaveParentTableAfterConstruction() throws Exception {
        assertThat(this.accessPattern.getTable(null), is(this.table));
    }

}
