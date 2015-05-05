/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.vdb.internal;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.komodo.relational.RelationalModelTest;
import org.komodo.relational.internal.RelationalModelFactory;
import org.komodo.relational.internal.RelationalObjectImpl;
import org.komodo.relational.model.RelationalObject.Filter;
import org.komodo.relational.vdb.Translator;
import org.komodo.relational.vdb.Vdb;
import org.komodo.spi.KException;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.modeshape.sequencer.teiid.lexicon.VdbLexicon;

@SuppressWarnings( { "javadoc", "nls" } )
public final class TranslatorImplTest extends RelationalModelTest {

    private Translator translator;
    private Vdb vdb;

    @Before
    public void init() throws Exception {
        final UnitOfWork transaction = _repo.createTransaction( TranslatorImplTest.class.getSimpleName(), false, null );

        this.vdb = RelationalModelFactory.createVdb( transaction, _repo, null, "vdb", "/Users/sledge/hammer/MyVdb.vdb" );
        this.translator = RelationalModelFactory.createTranslator( transaction, _repo, this.vdb, "translator", "type" );

        transaction.commit();
    }

    @Test
    public void shouldBeChildRestricted() {
        assertThat( this.translator.isChildRestricted(), is( true ) );
    }

    @Test
    public void shouldFailConstructionIfNotTranslator() {
        if (RelationalObjectImpl.VALIDATE_INITIAL_STATE) {
            try {
                new TranslatorImpl( null, _repo, this.vdb.getAbsolutePath() );
                fail();
            } catch (final KException e) {
                // expected
            }
        }
    }

    @Test
    public void shouldHaveCorrectPrimaryType() throws Exception {
        assertThat( this.translator.getPrimaryType( null ).getName(), is( VdbLexicon.Translator.TRANSLATOR ) );
    }

    @Test
    public void shouldHaveMoreRawProperties() throws Exception {
        final String[] filteredProps = this.translator.getPropertyNames( null );
        final String[] rawProps = this.translator.getRawPropertyNames( null );
        assertThat( ( rawProps.length > filteredProps.length ), is( true ) );
    }

    @Test
    public void shouldHaveParentVdb() throws Exception {
        assertThat( this.translator.getParent( null ), is( instanceOf( Vdb.class ) ) );
    }

    @Test
    public void shouldHaveTypeAfterConstruction() throws Exception {
        assertThat( this.translator.getType( null ), is( notNullValue() ) );
    }

    @Test( expected = UnsupportedOperationException.class )
    public void shouldNotAllowChildren() throws Exception {
        this.translator.addChild( null, "blah", null );
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotBeAbleToSetEmptyType() throws Exception {
        this.translator.setType( null, StringConstants.EMPTY_STRING );
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotBeAbleToSetNullType() throws Exception {
        this.translator.setType( null, null );
    }

    @Test
    public void shouldNotContainFilteredProperties() throws Exception {
        final String[] filteredProps = this.translator.getPropertyNames( null );
        final Filter[] filters = this.translator.getFilters();

        for ( final String name : filteredProps ) {
            for ( final Filter filter : filters ) {
                assertThat( filter.rejectProperty( name ), is( false ) );
            }
        }
    }

    @Test
    public void shouldNotHaveDescriptionAfterConstruction() throws Exception {
        assertThat( this.translator.getDescription( null ), is( nullValue() ) );
    }

    @Test
    public void shouldSetCustomProperty() throws Exception {
        final String propName = "custom";
        final String propValue = "value";
        this.translator.setProperty( null, propName, propValue );

        assertThat( this.translator.getProperty( null, propName ), is( notNullValue() ) );
        assertThat( this.translator.getProperty( null, propName ).getStringValue( null ), is( propValue ) );
    }

    @Test
    public void shouldSetDescription() throws Exception {
        final String newValue = "newDescription";
        this.translator.setDescription( null, newValue );
        assertThat( this.translator.getDescription( null ), is( newValue ) );
    }

    @Test
    public void shouldSetType() throws Exception {
        final String newValue = "newType";
        this.translator.setType( null, newValue );
        assertThat( this.translator.getType( null ), is( newValue ) );
    }

}
