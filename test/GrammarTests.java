import norswap.autumn.AutumnTestFixture;
import norswap.sigh.SighGrammar;
import norswap.sigh.ast.*;
import org.testng.annotations.Test;

import javax.lang.model.type.ArrayType;
import java.util.ArrayList;

import static java.util.Arrays.asList;
import static norswap.sigh.ast.BinaryOperator.*;

public class GrammarTests extends AutumnTestFixture {
    // ---------------------------------------------------------------------------------------------

    private final SighGrammar grammar = new SighGrammar();
    private final Class<?> grammarClass = grammar.getClass();

    // ---------------------------------------------------------------------------------------------

    private static IntLiteralNode intlit (long i) {
        return new IntLiteralNode(null, i);
    }

    private static StringLiteralNode stringlit (String i) {
        return new StringLiteralNode(null, i);
    }

    private static FloatLiteralNode floatlit (double d) {
        return new FloatLiteralNode(null, d);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testLiteralsAndUnary () {
        rule = grammar.expression;

        successExpect("42", intlit(42));
        successExpect("42.0", floatlit(42d));
        successExpect("\"hello\"", new StringLiteralNode(null, "hello"));
        successExpect("(42)", new ParenthesizedNode(null, intlit(42)));
        successExpect("[1, 2, 3]", new ArrayLiteralNode(null, asList(intlit(1), intlit(2), intlit(3))));
        successExpect("true", new ReferenceNode(null, "true",false));
        successExpect("false", new ReferenceNode(null, "false",false));
        successExpect("null", new ReferenceNode(null, "null",false));
        successExpect("!false", new UnaryExpressionNode(null, UnaryOperator.NOT, new ReferenceNode(null, "false",false)));
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testNumericBinary () {
        successExpect("1 + 2", new BinaryExpressionNode(null, intlit(1), ADD, intlit(2)));
        successExpect("2 - 1", new BinaryExpressionNode(null, intlit(2), SUBTRACT,  intlit(1)));
        successExpect("2 * 3", new BinaryExpressionNode(null, intlit(2), MULTIPLY, intlit(3)));
        successExpect("2 / 3", new BinaryExpressionNode(null, intlit(2), DIVIDE, intlit(3)));
        successExpect("2 % 3", new BinaryExpressionNode(null, intlit(2), REMAINDER, intlit(3)));

        successExpect("1.0 + 2.0", new BinaryExpressionNode(null, floatlit(1), ADD, floatlit(2)));
        successExpect("2.0 - 1.0", new BinaryExpressionNode(null, floatlit(2), SUBTRACT, floatlit(1)));
        successExpect("2.0 * 3.0", new BinaryExpressionNode(null, floatlit(2), MULTIPLY, floatlit(3)));
        successExpect("2.0 / 3.0", new BinaryExpressionNode(null, floatlit(2), DIVIDE, floatlit(3)));
        successExpect("2.0 % 3.0", new BinaryExpressionNode(null, floatlit(2), REMAINDER, floatlit(3)));

        successExpect("2 * (4-1) * 4.0 / 6 % (2+1)", new BinaryExpressionNode(null,
            new BinaryExpressionNode(null,
                new BinaryExpressionNode(null,
                    new BinaryExpressionNode(null,
                        intlit(2),
                        MULTIPLY,
                        new ParenthesizedNode(null, new BinaryExpressionNode(null,
                            intlit(4),
                            SUBTRACT,
                            intlit(1)))),
                    MULTIPLY,
                    floatlit(4d)),
                DIVIDE,
                intlit(6)),
            REMAINDER,
            new ParenthesizedNode(null, new BinaryExpressionNode(null,
                intlit(2),
                ADD,
                intlit(1)))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testArrayStructAccess () {
        rule = grammar.expression;
        successExpect("[1][0]", new ArrayAccessNode(null,
            new ArrayLiteralNode(null, asList(intlit(1))), intlit(0)));
        successExpect("[1].length", new FieldAccessNode(null,
            new ArrayLiteralNode(null, asList(intlit(1))), "length"));
        successExpect("p.x", new FieldAccessNode(null, new ReferenceNode(null, "p",false), "x"));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testDeclarations() {
        rule = grammar.statement;

        successExpect("var x: Int = 1", new VarDeclarationNode(null,
            "x", new SimpleTypeNode(null, "Int"), intlit(1),false));

        successExpect("var i: Int",new VarDeclarationNode(null,"i",new SimpleTypeNode(null,"Int"),null,false));

        successExpect("struct P {}", new StructDeclarationNode(null, "P", asList()));

        successExpect("struct P { var x: Int; var y: Int }",
            new StructDeclarationNode(null, "P", asList(
                new FieldDeclarationNode(null, "x", new SimpleTypeNode(null, "Int")),
                new FieldDeclarationNode(null, "y", new SimpleTypeNode(null, "Int")))));

        successExpect("fun f (x: Int): Int { return 1 }",
            new FunDeclarationNode(null, "f",
                asList(new ParameterNode(null, "x", new SimpleTypeNode(null, "Int"))),
                new SimpleTypeNode(null, "Int"),
                new BlockNode(null, asList(new ReturnNode(null, intlit(1))))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testStatements() {
        rule = grammar.statement;

        successExpect("return", new ReturnNode(null, null));
        successExpect("return 1", new ReturnNode(null, intlit(1)));
        successExpect("print(1)", new ExpressionStatementNode(null,
            new FunCallNode(null, new ReferenceNode(null, "print",false), asList(intlit(1)))));
        successExpect("{ return }", new BlockNode(null, asList(new ReturnNode(null, null))));


        successExpect("if true return 1 else return 2", new IfNode(null, new ReferenceNode(null, "true",false),
            new ReturnNode(null, intlit(1)),
            new ReturnNode(null, intlit(2))));

        successExpect("if false return 1 else if true return 2 else return 3 ",
            new IfNode(null, new ReferenceNode(null, "false",false),
                new ReturnNode(null, intlit(1)),
                new IfNode(null, new ReferenceNode(null, "true",false),
                    new ReturnNode(null, intlit(2)),
                    new ReturnNode(null, intlit(3)))));

        successExpect("while 1 < 2 { return } ", new WhileNode(null,
            new BinaryExpressionNode(null, intlit(1), LOWER, intlit(2)),
            new BlockNode(null, asList(new ReturnNode(null, null)))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testTypeCheck() {
        rule = grammar.expression;

        successExpect("3 is Int",new BinaryExpressionTypeCheckNode(null,intlit(3),IS,new SimpleTypeNode(null,"Int")));

        successExpect("\"String\" is String",new BinaryExpressionTypeCheckNode(null,new StringLiteralNode(null,"String"),IS,new SimpleTypeNode(null,"String")));

        successExpect("3.0 is Bool",new BinaryExpressionTypeCheckNode(null,new FloatLiteralNode(null,3.0),IS,new SimpleTypeNode(null,"Bool")));

        successExpect("true is Float", new BinaryExpressionTypeCheckNode(null,new ReferenceNode(null,"true",false),IS,new SimpleTypeNode(null,"Float")));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testSwitches() {
        rule = grammar.statement;

        successExpect("switch(1) { case 2 { return 2}; default { return 0}}",new SwitchNode(null,intlit(1),
            asList(new CaseNode(null,intlit(2),new BlockNode(null,asList(new ReturnNode(null,intlit(2))))),
                new DefaultNode(null,new BlockNode(null,asList(new ReturnNode(null,intlit(0))))))));

        successExpect("switch (p) { case (1,2) {} case(_,4) {}}",new SwitchNode(null,new ReferenceNode(null,"p",false),
            asList(new CaseNode(null,new StructMatchingNode(null,asList(intlit(1),intlit(2))),new BlockNode(null,asList())),
                new CaseNode(null,new StructMatchingNode(null,asList(new AnyLiteralNode(null),intlit(4))),new BlockNode(null,asList())))));
    }

    @Test public void testOptionalDecl() {
        rule = grammar.statement;

        successExpect("var i: Int?",new VarDeclarationNode(null,"i",new OptTypeNode(null,"Int"),null,false));

        successExpect("var test: String?",new VarDeclarationNode(null,"test",new OptTypeNode(null,"String"),null,false));

        successExpect("var i: Int? = 5",new VarDeclarationNode(null,"i",new OptTypeNode(null,"Int"), intlit(5),false));

        failure("var j: Int ?");

    }

    @Test public void testOptionalUnwrapWithBang(){
        rule = grammar.expression;

        successExpect("i!",new ReferenceNode(null, "i",true));

        failure("variable !");
    }

    @Test public void testOptionalUnwrapWithIf(){
        rule = grammar.statements;

        successExpect("var testOpt: String? = \"test\" \n" +
            "if var testUnwrap:String = testOpt{" +
            "var i: String = testUnwrap}",asList(new VarDeclarationNode(null,"testOpt", new OptTypeNode(null,"String"), stringlit("test"),false),
            new IfUnwrapNode(null,new VarDeclarationNode(null,"testUnwrap",new SimpleTypeNode(null,"String"),new ReferenceNode(null,"testOpt",true),true),new BlockNode(null,
                asList(new VarDeclarationNode(null,"i",new SimpleTypeNode(null,"String"),new ReferenceNode(null,"testUnwrap",false),false))),null)));

        successExpect("if var testUnwrap:String = testOpt{" +
            "var i: String = testUnwrap}\n" +
            "else{var i: String = \"elseStatement\"}",asList(new IfUnwrapNode(null,new VarDeclarationNode(null,"testUnwrap",new SimpleTypeNode(null,"String"),new ReferenceNode(null,"testOpt",true),true),new BlockNode(null,
            asList(new VarDeclarationNode(null,"i",new SimpleTypeNode(null,"String"),new ReferenceNode(null,"testUnwrap",false),false))),
            new BlockNode(null, asList(new VarDeclarationNode(null,"i",new SimpleTypeNode(null,"String"),stringlit("elseStatement"),false))))));

        failure("if var testUnwrap:Int = 5{}");
        failure("if var testUnwrap:Int");
        failure("if(var testUnwrap:Int = testOpt){}");

        failure("if var testUnwrap = testOpt{");

    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testClosures(){
        rule = grammar.statement;

        successExpect("fun f() : (Int) -> Float { return {(x) in return x/1.5}}",new FunDeclarationNode(null,"f",asList(),
            new ClosureTypeNode(null,asList(new SimpleTypeNode(null,"Int")),new SimpleTypeNode(null,"Float")),
            new BlockNode(null,asList(new ReturnNode(null,new ClosureExpressionNode(null,asList(new ParameterClosureNode(null,"x")),
                new BlockNode(null,asList(new ReturnNode(null,new BinaryExpressionNode(null,
                    new ReferenceNode(null,"x",false),DIVIDE,floatlit(1.5)))))))))));

        successExpect("fun f(closure : ()-> Int) : Int { return closure()}",new FunDeclarationNode(null,"f",asList(
            new ParameterNode(null,"closure",new ClosureTypeNode(null,asList(),new SimpleTypeNode(null,"Int")))),
            new SimpleTypeNode(null,"Int"), new BlockNode(null,asList(new ReturnNode(null,new FunCallNode(null,
                new ReferenceNode(null,"closure",false),asList()))))
        ));

        successExpect("fun f() : (Int, String) -> Void { return {(x,y) in print(y)}}",new FunDeclarationNode(null,"f",asList(),
            new ClosureTypeNode(null,asList(new SimpleTypeNode(null,"Int"),new SimpleTypeNode(null,"String")),
                new SimpleTypeNode(null,"Void")),
            new BlockNode(null,asList(new ReturnNode(null,new ClosureExpressionNode(null,asList(new ParameterClosureNode(null,"x"),
                new ParameterClosureNode(null,"y")),
                new BlockNode(null,asList(new ExpressionStatementNode(null,new FunCallNode(null,new ReferenceNode(null,"print",false),
                    asList(new ReferenceNode(null,"y",false))))))))))));

        successExpect("fun f(x : Int) : () -> Int {return {return x}}",new FunDeclarationNode(null,"f",asList(new ParameterNode(null,"x",
            new SimpleTypeNode(null,"Int"))),new ClosureTypeNode(null,asList(),new SimpleTypeNode(null,"Int")),new BlockNode(null,
            asList(new ReturnNode(null,new ClosureExpressionNode(null,asList(),new BlockNode(null,asList(new ReturnNode(null,
                new ReferenceNode(null,"x",false))))))))));

        successExpect("fun f() : () -> Bool {" +
            "var x : Int = 3" +
            "return {" +
            "x = x+1" +
            "return true}}",new FunDeclarationNode(null,"f",asList(),new ClosureTypeNode(null,asList(),new SimpleTypeNode(null,
            "Bool")),new BlockNode(null,asList(new VarDeclarationNode(null,"x",new SimpleTypeNode(null,"Int"),
            intlit(3),false),new ReturnNode(null,new ClosureExpressionNode(null,asList(),new BlockNode(null,
            asList(new ExpressionStatementNode(null,new AssignmentNode(null,new ReferenceNode(null,"x",false),new BinaryExpressionNode(null,
                new ReferenceNode(null,"x",false),ADD,intlit(1)))),new ReturnNode(null,new ReferenceNode(null,"true",false))))))))));

        failure("var clo : (Int,Bool) -> Float = {(x,y) in {return 6.0}}");

        failure("fun f() : (String,String) -> String {" +
            "return { a,b in" +
            "return a+b}" +
            "}");


    }

    @Test public void testListComprehension(){
        rule = grammar.statement;

        successExpect("var test:Int[] = [ x for x:Int in list ]", new VarDeclarationNode(null, "test", new ArrayTypeNode(null,new SimpleTypeNode(null, "Int")),
            new ArrayComprehensionNode(null, new VarDeclarationNode(null,"x",new SimpleTypeNode(null,"Int"),null,false),
                new ReferenceNode(null,"x",false),new ReferenceNode(null,"list",false),null),false));

        successExpect("var test:Int[] = [ x for x:Int in 1 ]", new VarDeclarationNode(null, "test", new ArrayTypeNode(null,new SimpleTypeNode(null, "Int")),
            new ArrayComprehensionNode(null, new VarDeclarationNode(null,"x",new SimpleTypeNode(null,"Int"),null,false),
                new ReferenceNode(null,"x",false),new IntLiteralNode(null,1),null),false));


        successExpect("var test:Int[] = [ x for x:Int in list if x > 3]", new VarDeclarationNode(null, "test", new ArrayTypeNode(null,new SimpleTypeNode(null, "Int")),
            new ArrayComprehensionNode(null, new VarDeclarationNode(null,"x",new SimpleTypeNode(null,"Int"),null,false),
                new ReferenceNode(null,"x",false),new ReferenceNode(null,"list",false),
                new BinaryExpressionNode(null,new ReferenceNode(null,"x",false), GREATER,new IntLiteralNode(null,3))),false));

        failure("var test:Int[] = [ x for x in list if x > 3]");
    }



    // ---------------------------------------------------------------------------------------------
}
