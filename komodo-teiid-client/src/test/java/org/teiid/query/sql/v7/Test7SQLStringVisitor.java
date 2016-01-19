/*
 * JBoss, Home of Professional Open Source.
* See the COPYRIGHT.txt file distributed with this work for information
* regarding copyright ownership. Some portions may be licensed
* to Red Hat, Inc. under one or more contributor license agreements.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
* 02110-1301 USA.
*/
package org.teiid.query.sql.v7;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.komodo.spi.runtime.version.DefaultTeiidVersion.Version;
import org.teiid.language.SQLConstants.NonReserved;
import org.teiid.query.parser.TeiidNodeFactory.ASTNodes;
import org.teiid.query.sql.AbstractTestSQLStringVisitor;
import org.teiid.query.sql.lang.CriteriaImpl;
import org.teiid.query.sql.lang.CriteriaOperator.Operator;
import org.teiid.query.sql.lang.CriteriaSelectorImpl;
import org.teiid.query.sql.lang.DeleteImpl;
import org.teiid.query.sql.proc.AssignmentStatementImpl;
import org.teiid.query.sql.proc.BlockImpl;
import org.teiid.query.sql.proc.CommandStatementImpl;
import org.teiid.query.sql.proc.CreateUpdateProcedureCommandImpl;
import org.teiid.query.sql.proc.IfStatementImpl;
import org.teiid.query.sql.proc.RaiseErrorStatementImpl;
import org.teiid.query.sql.proc.StatementImpl;
import org.teiid.query.sql.symbol.BaseAggregateSymbol;
import org.teiid.query.sql.symbol.ElementSymbolImpl;

/**
 *
 */
@SuppressWarnings( {"nls", "javadoc"} )
public class Test7SQLStringVisitor extends AbstractTestSQLStringVisitor {

    private Test7Factory factory;

    public Test7SQLStringVisitor() {
        super(Version.TEIID_7_7.get());
    }

    @Override
    protected Test7Factory getFactory() {
        if (factory == null)
            factory = new Test7Factory(parser);

        return factory;
    }

    @Test
    public void testAggregateSymbol1() {
        BaseAggregateSymbol agg = getFactory().newAggregateSymbol("abc", NonReserved.COUNT, false, getFactory().newConstant("abc"));
        helpTest(agg, "COUNT('abc')");
    }

    @Test
    public void testAggregateSymbol2() {
        BaseAggregateSymbol agg = getFactory().newAggregateSymbol("abc", NonReserved.COUNT, true, getFactory().newConstant("abc"));
        helpTest(agg, "COUNT(DISTINCT 'abc')");
    }

    @Test
    public void testAggregateSymbol3() {
        BaseAggregateSymbol agg = getFactory().newAggregateSymbol("abc", NonReserved.COUNT, false, null);
        helpTest(agg, "COUNT(*)");
    }

    @Test
    public void testAggregateSymbol4() {
        BaseAggregateSymbol agg = getFactory().newAggregateSymbol("abc", NonReserved.AVG, false, getFactory().newConstant("abc"));
        helpTest(agg, "AVG('abc')");
    }

    @Test
    public void testAggregateSymbol5() {
        BaseAggregateSymbol agg = getFactory().newAggregateSymbol("abc", NonReserved.SUM, false, getFactory().newConstant("abc"));
        helpTest(agg, "SUM('abc')");
    }

    @Test
    public void testAggregateSymbol6() {
        BaseAggregateSymbol agg = getFactory().newAggregateSymbol("abc", NonReserved.MIN, false, getFactory().newConstant("abc"));
        helpTest(agg, "MIN('abc')");
    }

    @Test
    public void testAggregateSymbol7() {
        BaseAggregateSymbol agg = getFactory().newAggregateSymbol("abc", NonReserved.MAX, false, getFactory().newConstant("abc"));
        helpTest(agg, "MAX('abc')");
    }

    @Test
    public void testRaiseErrorStatement() {
        StatementImpl errStmt = getFactory().newRaiseStatement(getFactory().newConstant("My Error"));
        helpTest(errStmt, "ERROR 'My Error';");
    }

    @Test
    public void testRaiseErrorStatementWithExpression() {
        StatementImpl errStmt = getFactory().newRaiseStatement(getFactory().newElementSymbol("a"));
        helpTest(errStmt, "ERROR a;");
    }

    @Test public void testHasCriteria1() {
        ElementSymbolImpl sy1 = getFactory().newElementSymbol("x"); //$NON-NLS-1$
        ElementSymbolImpl sy2 = getFactory().newElementSymbol("y"); //$NON-NLS-1$
        ElementSymbolImpl sy3 = getFactory().newElementSymbol("z"); //$NON-NLS-1$
        List elmnts = new ArrayList(3);
        elmnts.add(sy1);
        elmnts.add(sy2);
        elmnts.add(sy3);                
        CriteriaSelectorImpl cs = getFactory().newCriteriaSelector();
        cs.setElements(elmnts);
        cs.setSelectorType(Operator.LIKE);
        helpTest(getFactory().newHasCriteria(cs), "HAS LIKE CRITERIA ON (x, y, z)"); //$NON-NLS-1$
    }
    
    @Test public void testHasCriteria2() {
        ElementSymbolImpl sy1 = getFactory().newElementSymbol("x"); //$NON-NLS-1$
        ElementSymbolImpl sy2 = getFactory().newElementSymbol("y"); //$NON-NLS-1$
        ElementSymbolImpl sy3 = getFactory().newElementSymbol("z"); //$NON-NLS-1$
        List elmnts = new ArrayList(3);
        elmnts.add(sy1);
        elmnts.add(sy2);
        elmnts.add(sy3);                
        CriteriaSelectorImpl cs = getFactory().newCriteriaSelector();
        cs.setElements(elmnts);
        cs.setSelectorType(Operator.LIKE);
        helpTest(getFactory().newHasCriteria(cs), "HAS LIKE CRITERIA ON (x, y, z)"); //$NON-NLS-1$
    }
    
    @Test public void testHasCriteria3() {
        ElementSymbolImpl sy1 = getFactory().newElementSymbol("x"); //$NON-NLS-1$
        ElementSymbolImpl sy2 = getFactory().newElementSymbol("y"); //$NON-NLS-1$
        ElementSymbolImpl sy3 = getFactory().newElementSymbol("z"); //$NON-NLS-1$
        List elmnts = new ArrayList(3);
        elmnts.add(sy1);
        elmnts.add(sy2);
        elmnts.add(sy3);                
        CriteriaSelectorImpl cs = getFactory().newCriteriaSelector();
        cs.setElements(elmnts);
        cs.setSelectorType(Operator.BETWEEN);
        helpTest(getFactory().newHasCriteria(cs), "HAS BETWEEN CRITERIA ON (x, y, z)"); //$NON-NLS-1$
    }

    @Test
    public void testBlock1() {
        DeleteImpl d1 = getFactory().newNode(ASTNodes.DELETE);
        d1.setGroup(getFactory().newGroupSymbol("g"));
        CommandStatementImpl cmdStmt = getFactory().newCommandStatement(d1);
        AssignmentStatementImpl assigStmt = getFactory().newAssignmentStatement(getFactory().newElementSymbol("a"), getFactory().newConstant(new Integer(1)));
        StatementImpl errStmt = getFactory().newRaiseStatement(getFactory().newConstant("My Error"));
        BlockImpl b = getFactory().newBlock();
        b.addStatement(cmdStmt);
        b.addStatement(assigStmt);
        b.addStatement(errStmt);
        helpTest(b, "BEGIN\nDELETE FROM g;\na = 1;\nERROR 'My Error';\nEND");
    }    
    
    @Test public void testBlock2() {        
        // construct If statement

        DeleteImpl d1 = getFactory().newNode(ASTNodes.DELETE);
        d1.setGroup(getFactory().newGroupSymbol("g")); //$NON-NLS-1$
        CommandStatementImpl cmdStmt =  getFactory().newCommandStatement(d1);
        BlockImpl ifblock = getFactory().newBlock(cmdStmt);
        // construct If criteria        
        ElementSymbolImpl sy1 = getFactory().newElementSymbol("x"); //$NON-NLS-1$
        List elmnts = new ArrayList(1);
        elmnts.add(sy1);
        CriteriaSelectorImpl cs = getFactory().newCriteriaSelector();
        cs.setElements(elmnts);
        cs.setSelectorType(Operator.LIKE); 
        CriteriaImpl crit = getFactory().newHasCriteria(cs);        
        IfStatementImpl ifStmt = getFactory().newIfStatement(crit, ifblock);        
        
        // other statements
        RaiseErrorStatementImpl errStmt =   getFactory().newRaiseStatement(getFactory().newConstant("My Error")); //$NON-NLS-1$
        BlockImpl b = getFactory().newBlock();
        b.addStatement(cmdStmt);
        b.addStatement(ifStmt);
        b.addStatement(errStmt);        

        helpTest(b, "BEGIN\nDELETE FROM g;\nIF(HAS LIKE CRITERIA ON (x))\nBEGIN\nDELETE FROM g;\nEND\nERROR 'My Error';\nEND"); //$NON-NLS-1$
    } 
    
    @Test public void testIfStatement1() {
        // construct If block
        DeleteImpl d1 = getFactory().newNode(ASTNodes.DELETE);
        d1.setGroup(getFactory().newGroupSymbol("g")); //$NON-NLS-1$
        CommandStatementImpl cmdStmt =  getFactory().newCommandStatement(d1);
        AssignmentStatementImpl assigStmt = getFactory().newAssignmentStatement(getFactory().newElementSymbol("a"), getFactory().newConstant(new Integer(1))); //$NON-NLS-1$
        RaiseErrorStatementImpl errStmt =   getFactory().newRaiseStatement(getFactory().newConstant("My Error")); //$NON-NLS-1$
        BlockImpl ifblock = getFactory().newBlock();
        ifblock.addStatement(cmdStmt);
        ifblock.addStatement(assigStmt);
        ifblock.addStatement(errStmt);

        // construct If criteria        
        ElementSymbolImpl sy1 = getFactory().newElementSymbol("x"); //$NON-NLS-1$
        List elmnts = new ArrayList(1);
        elmnts.add(sy1);
        CriteriaSelectorImpl cs = getFactory().newCriteriaSelector();
        cs.setElements(elmnts);
        cs.setSelectorType(Operator.LIKE); 
        CriteriaImpl crit = getFactory().newHasCriteria(cs);
        
        IfStatementImpl ifStmt = getFactory().newIfStatement(crit, ifblock);
        helpTest(ifStmt, "IF(HAS LIKE CRITERIA ON (x))\nBEGIN\nDELETE FROM g;\na = 1;\nERROR 'My Error';\nEND"); //$NON-NLS-1$
    }

    @Test public void testIfStatement2() {
        // construct If block
        DeleteImpl d1 = getFactory().newNode(ASTNodes.DELETE);
        d1.setGroup(getFactory().newGroupSymbol("g")); //$NON-NLS-1$
        CommandStatementImpl cmdStmt =  getFactory().newCommandStatement(d1);
        BlockImpl ifblock = getFactory().newBlock(cmdStmt);

        // construct If criteria        
        ElementSymbolImpl sy1 = getFactory().newElementSymbol("x"); //$NON-NLS-1$
        List elmnts = new ArrayList(1);
        elmnts.add(sy1);
        CriteriaSelectorImpl cs = getFactory().newCriteriaSelector();
        cs.setElements(elmnts);
        cs.setSelectorType(Operator.LIKE); 
        CriteriaImpl crit = getFactory().newHasCriteria(cs);
        
        IfStatementImpl ifStmt = getFactory().newIfStatement(crit, ifblock);
        helpTest(ifStmt, "IF(HAS LIKE CRITERIA ON (x))\nBEGIN\nDELETE FROM g;\nEND"); //$NON-NLS-1$
    }

    @Test public void testIfStatement3() {
        // construct If block
        DeleteImpl d1 = getFactory().newNode(ASTNodes.DELETE);
        d1.setGroup(getFactory().newGroupSymbol("g")); //$NON-NLS-1$
        CommandStatementImpl cmdStmt =  getFactory().newCommandStatement(d1);
        AssignmentStatementImpl assigStmt = getFactory().newAssignmentStatement(getFactory().newElementSymbol("a"), getFactory().newConstant(new Integer(1))); //$NON-NLS-1$
        RaiseErrorStatementImpl errStmt =   getFactory().newRaiseStatement(getFactory().newConstant("My Error")); //$NON-NLS-1$
        BlockImpl ifblock = getFactory().newBlock();
        ifblock.addStatement(cmdStmt);
        ifblock.addStatement(assigStmt);
        ifblock.addStatement(errStmt);

        // construct If criteria        
        ElementSymbolImpl sy1 = getFactory().newElementSymbol("x"); //$NON-NLS-1$
        List elmnts = new ArrayList(1);
        elmnts.add(sy1);
        CriteriaSelectorImpl cs = getFactory().newCriteriaSelector();
        cs.setElements(elmnts);
        cs.setSelectorType(Operator.LIKE);     
        CriteriaImpl crit = getFactory().newHasCriteria(cs);
        
        BlockImpl elseblock = getFactory().newBlock();
        elseblock.addStatement(cmdStmt);
        
        IfStatementImpl ifStmt = getFactory().newIfStatement(crit, ifblock);
        ifStmt.setElseBlock(elseblock);
        helpTest(ifStmt, "IF(HAS LIKE CRITERIA ON (x))\nBEGIN\nDELETE FROM g;\na = 1;\nERROR 'My Error';\nEND\nELSE\nBEGIN\nDELETE FROM g;\nEND"); //$NON-NLS-1$
    }

    @Test
    public void testCreateUpdateProcedure1() {
        DeleteImpl d1 = getFactory().newNode(ASTNodes.DELETE);
        d1.setGroup(getFactory().newGroupSymbol("g"));
        CommandStatementImpl cmdStmt = getFactory().newCommandStatement(d1);
        AssignmentStatementImpl assigStmt = getFactory().newAssignmentStatement(getFactory().newElementSymbol("a"), getFactory().newConstant(new Integer(1)));
        RaiseErrorStatementImpl errStmt = getFactory().newRaiseStatement(getFactory().newConstant("My Error"));
        BlockImpl b = getFactory().newBlock();
        b.addStatement(cmdStmt);
        b.addStatement(assigStmt);
        b.addStatement(errStmt);
        CreateUpdateProcedureCommandImpl cup = getFactory().newCreateUpdateProcedureCommand(b);
        helpTest(cup, "CREATE PROCEDURE\nBEGIN\nDELETE FROM g;\na = 1;\nERROR 'My Error';\nEND");
    }

    @Test
    public void testCreateUpdateProcedure2() {
        DeleteImpl d1 = getFactory().newNode(ASTNodes.DELETE);
        d1.setGroup(getFactory().newGroupSymbol("g"));
        CommandStatementImpl cmdStmt = getFactory().newCommandStatement(d1);
        AssignmentStatementImpl assigStmt = getFactory().newAssignmentStatement(getFactory().newElementSymbol("a"), getFactory().newConstant(new Integer(1)));
        RaiseErrorStatementImpl errStmt = getFactory().newRaiseStatement(getFactory().newConstant("My Error"));
        BlockImpl b = getFactory().newBlock();
        b.addStatement(cmdStmt);
        b.addStatement(assigStmt);
        b.addStatement(errStmt);
        CreateUpdateProcedureCommandImpl cup = getFactory().newCreateUpdateProcedureCommand(b);
        helpTest(cup, "CREATE PROCEDURE\nBEGIN\nDELETE FROM g;\na = 1;\nERROR 'My Error';\nEND");
    }

    @Test
    public void testCreateUpdateProcedure3() {
        DeleteImpl d1 = getFactory().newNode(ASTNodes.DELETE);
        d1.setGroup(getFactory().newGroupSymbol("g"));
        CommandStatementImpl cmdStmt = getFactory().newCommandStatement(d1);
        AssignmentStatementImpl assigStmt = getFactory().newAssignmentStatement(getFactory().newElementSymbol("a"), getFactory().newConstant(new Integer(1)));
        StatementImpl errStmt = getFactory().newRaiseStatement(getFactory().newConstant("My Error"));
        BlockImpl b = getFactory().newBlock();
        b.addStatement(cmdStmt);
        b.addStatement(assigStmt);
        b.addStatement(errStmt);
        CreateUpdateProcedureCommandImpl cup = getFactory().newCreateUpdateProcedureCommand(b);
        helpTest(cup, "CREATE PROCEDURE\nBEGIN\nDELETE FROM g;\na = 1;\nERROR 'My Error';\nEND");
    }
}
