import norswap.autumn.AutumnTestFixture;
import norswap.autumn.Grammar;
import norswap.autumn.Grammar.rule;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;
import norswap.sigh.SemanticAnalysis;
import norswap.sigh.SighGrammar;
import norswap.sigh.ast.SighNode;
import norswap.sigh.interpreter.Interpreter;
import norswap.sigh.interpreter.InterpreterException;
import norswap.sigh.interpreter.Null;
import norswap.uranium.Reactor;
import norswap.uranium.SemanticError;
import norswap.utils.IO;
import norswap.utils.TestFixture;
import norswap.utils.data.wrappers.Pair;
import norswap.utils.visitors.Walker;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Set;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

public final class InterpreterTests extends TestFixture {

    // TODO peeling

    // ---------------------------------------------------------------------------------------------

    private final SighGrammar grammar = new SighGrammar();
    private final AutumnTestFixture autumnFixture = new AutumnTestFixture();

    {
        autumnFixture.runTwice = false;
        autumnFixture.bottomClass = this.getClass();
    }

    // ---------------------------------------------------------------------------------------------

    private Grammar.rule rule;

    // ---------------------------------------------------------------------------------------------

    private void check (String input, Object expectedReturn) {
        assertNotNull(rule, "You forgot to initialize the rule field.");
        check(rule, input, expectedReturn, null);
    }

    // ---------------------------------------------------------------------------------------------

    private void check (String input, Object expectedReturn, String expectedOutput) {
        assertNotNull(rule, "You forgot to initialize the rule field.");
        check(rule, input, expectedReturn, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void check (rule rule, String input, Object expectedReturn, String expectedOutput) {
        // TODO
        // (1) write proper parsing tests
        // (2) write some kind of automated runner, and use it here

        autumnFixture.rule = rule;
        ParseResult parseResult = autumnFixture.success(input);
        SighNode root = parseResult.topValue();

        Reactor reactor = new Reactor();
        Walker<SighNode> walker = SemanticAnalysis.createWalker(reactor);
        Interpreter interpreter = new Interpreter(reactor);
        walker.walk(root);
        reactor.run();
        Set<SemanticError> errors = reactor.errors();

        if (!errors.isEmpty()) {
            LineMapString map = new LineMapString("<test>", input);
            String report = reactor.reportErrors(it ->
                it.toString() + " (" + ((SighNode) it).span.startString(map) + ")");
            //            String tree = AttributeTreeFormatter.format(root, reactor,
            //                    new ReflectiveFieldWalker<>(SighNode.class, PRE_VISIT, POST_VISIT));
            //            System.err.println(tree);
            throw new AssertionError(report);
        }

        Pair<String, Object> result = IO.captureStdout(() -> interpreter.interpret(root));
        assertEquals(result.b, expectedReturn);
        if (expectedOutput != null) assertEquals(result.a, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkExpr (String input, Object expectedReturn, String expectedOutput) {
        rule = grammar.root;
        check("return " + input, expectedReturn, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkExpr (String input, Object expectedReturn) {
        rule = grammar.root;
        check("return " + input, expectedReturn);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkThrows (String input, Class<? extends Throwable> expected) {
        assertThrows(expected, () -> check(input, null));
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testLiteralsAndUnary () {
        checkExpr("42", 42L);
        checkExpr("42.0", 42.0d);
        checkExpr("\"hello\"", "hello");
        checkExpr("(42)", 42L);
        checkExpr("[1, 2, 3]", new Object[]{1L, 2L, 3L});
        checkExpr("true", true);
        checkExpr("false", false);
        checkExpr("null", Null.INSTANCE);
        checkExpr("!false", true);
        checkExpr("!true", false);
        checkExpr("!!true", true);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testNumericBinary () {
        checkExpr("1 + 2", 3L);
        checkExpr("2 - 1", 1L);
        checkExpr("2 * 3", 6L);
        checkExpr("2 / 3", 0L);
        checkExpr("3 / 2", 1L);
        checkExpr("2 % 3", 2L);
        checkExpr("3 % 2", 1L);

        checkExpr("1.0 + 2.0", 3.0d);
        checkExpr("2.0 - 1.0", 1.0d);
        checkExpr("2.0 * 3.0", 6.0d);
        checkExpr("2.0 / 3.0", 2d / 3d);
        checkExpr("3.0 / 2.0", 3d / 2d);
        checkExpr("2.0 % 3.0", 2.0d);
        checkExpr("3.0 % 2.0", 1.0d);

        checkExpr("1 + 2.0", 3.0d);
        checkExpr("2 - 1.0", 1.0d);
        checkExpr("2 * 3.0", 6.0d);
        checkExpr("2 / 3.0", 2d / 3d);
        checkExpr("3 / 2.0", 3d / 2d);
        checkExpr("2 % 3.0", 2.0d);
        checkExpr("3 % 2.0", 1.0d);

        checkExpr("1.0 + 2", 3.0d);
        checkExpr("2.0 - 1", 1.0d);
        checkExpr("2.0 * 3", 6.0d);
        checkExpr("2.0 / 3", 2d / 3d);
        checkExpr("3.0 / 2", 3d / 2d);
        checkExpr("2.0 % 3", 2.0d);
        checkExpr("3.0 % 2", 1.0d);

        checkExpr("2 * (4-1) * 4.0 / 6 % (2+1)", 1.0d);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testOtherBinary () {
        checkExpr("true  && true",  true);
        checkExpr("true  || true",  true);
        checkExpr("true  || false", true);
        checkExpr("false || true",  true);
        checkExpr("false && true",  false);
        checkExpr("true  && false", false);
        checkExpr("false && false", false);
        checkExpr("false || false", false);

        checkExpr("1 + \"a\"", "1a");
        checkExpr("\"a\" + 1", "a1");
        checkExpr("\"a\" + true", "atrue");

        checkExpr("1 == 1", true);
        checkExpr("1 == 2", false);
        checkExpr("1.0 == 1.0", true);
        checkExpr("1.0 == 2.0", false);
        checkExpr("true == true", true);
        checkExpr("false == false", true);
        checkExpr("true == false", false);
        checkExpr("1 == 1.0", true);
        checkExpr("[1] == [1]", false);

        checkExpr("1 != 1", false);
        checkExpr("1 != 2", true);
        checkExpr("1.0 != 1.0", false);
        checkExpr("1.0 != 2.0", true);
        checkExpr("true != true", false);
        checkExpr("false != false", false);
        checkExpr("true != false", true);
        checkExpr("1 != 1.0", false);

        checkExpr("\"hi\" != \"hi2\"", true);
        checkExpr("[1] != [1]", true);

         // test short circuit
        checkExpr("true || print(\"x\") == \"y\"", true, "");
        checkExpr("false && print(\"x\") == \"y\"", false, "");

        checkExpr("3 is Int",true);
        checkExpr("3 is Float",false);
        checkExpr("3.14 is Float",true);
        checkExpr("\"3\" is Int", false);
        checkExpr("\"3\" is String", true);
        checkExpr("true is Bool",true);
        checkExpr("null is Void", false);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testVarDecl () {
        check("var x: Int = 1; return x", 1L);
        check("var x: Float = 2.0; return x", 2d);

        check("var x: Int = 0; return x = 3", 3L);
        check("var x: String = \"0\"; return x = \"S\"", "S");

        // implicit conversions
        check("var x: Float = 1; x = 2; return x", 2.0d);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testRootAndBlock () {
        rule = grammar.root;
        check("return", null);
        check("return 1", 1L);
        check("return 1; return 2", 1L);

        check("print(\"a\")", null, "a\n");
        check("print(\"a\" + 1)", null, "a1\n");
        check("print(\"a\"); print(\"b\")", null, "a\nb\n");

        check("{ print(\"a\"); print(\"b\") }", null, "a\nb\n");

        check(
            "var x: Int = 1;" +
            "{ print(\"\" + x); var x: Int = 2; print(\"\" + x) }" +
            "print(\"\" + x)",
            null, "1\n2\n1\n");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testCalls () {
        check(
            "fun add (a: Int, b: Int): Int { return a + b } " +
                "return add(4, 7)",
            11L);

        HashMap<String, Object> point = new HashMap<>();
        point.put("x", 1L);
        point.put("y", 2L);

        check(
            "struct Point { var x: Int; var y: Int }" +
                "return $Point(1, 2)",
            point);

        check("var str: String = null; return print(str + 1)", "null1", "null1\n");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testArrayStructAccess () {
        checkExpr("[1][0]", 1L);
        checkExpr("[1.0][0]", 1d);
        checkExpr("[1, 2][1]", 2L);

        // TODO check that this fails (& maybe improve so that it generates a better message?)
        // or change to make it legal (introduce a top type, and make it a top type array if thre
        // is no inference context available)
        // checkExpr("[].length", 0L);
        checkExpr("[1].length", 1L);
        checkExpr("[1, 2].length", 2L);

        checkThrows("var array: Int[] = null; return array[0]", NullPointerException.class);
        checkThrows("var array: Int[] = null; return array.length", NullPointerException.class);

        check("var x: Int[] = [0, 1]; x[0] = 3; return x[0]", 3L);
        checkThrows("var x: Int[] = []; x[0] = 3; return x[0]",
            ArrayIndexOutOfBoundsException.class);
        checkThrows("var x: Int[] = null; x[0] = 3",
            NullPointerException.class);

        check(
            "struct P { var x: Int; var y: Int }" +
                "return $P(1, 2).y",
            2L);

        checkThrows(
            "struct P { var x: Int; var y: Int }" +
                "var p: P = null;" +
                "return p.y",
            NullPointerException.class);

        check(
            "struct P { var x: Int; var y: Int }" +
                "var p: P = $P(1, 2);" +
                "p.y = 42;" +
                "return p.y",
            42L);

        checkThrows(
            "struct P { var x: Int; var y: Int }" +
                "var p: P = null;" +
                "p.y = 42",
            NullPointerException.class);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testIfWhile () {
        check("if (true) return 1 else return 2", 1L);
        check("if (false) return 1 else return 2", 2L);
        check("if (false) return 1 else if (true) return 2 else return 3 ", 2L);
        check("if (false) return 1 else if (false) return 2 else return 3 ", 3L);

        check("var i: Int = 0; while (i < 3) { print(\"\" + i); i = i + 1 } ", null, "0\n1\n2\n");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testInference () {
        check("var array: Int[] = []", null);
        check("var array: String[] = []", null);
        check("fun use_array (array: Int[]) {} ; use_array([])", null);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testTypeAsValues () {
        check("struct S{} ; return \"\"+ S", "S");
        check("struct S{} ; var type: Type = S ; return \"\"+ type", "S");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testUnconditionalReturn()
    {
        check("fun f(): Int { if (true) return 1 else return 2 } ; return f()", 1L);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testSwitches() {
        check("switch(2) { case 1 { return 1 }"+
                "case 2 { return 2} }",2L);

        check("switch(\"Good morning\") { case \"Good\" { return 1 }"+
                "case \"Good morning\" { return 2 } default { return 3 } }", 2L);

        check("switch(1.0) { case 1.1 { return 3}"+
                "default { return 4 } }", 4L);

        check("switch(2) { case 1 { return 1 }"+
                "default {return 2} case 2 { return 3} }",3L);

        check("switch(3) { case 1 { return 1 }"+
            "default {return 2} case 2 { return 3} }",2L);

        check("var i : Int = 1"+
            "switch(i) { case 1 { return 1 }"+
            "default {return 2} case 2 { return 3} }",1L);

        check("struct P { var x: Int; var y: Int }" +
                "var p: P = $P(1, 2)" +
                "switch(p){"+
                "case (1,2) { return 1 }"+
                "default  { return 3 } }",1L);

        check("struct P { var x: Int; var y: Int }" +
                "var p: P = $P(3, 4)" +
                "switch(p){"+
                "case (1,1) { return 1 }"+
                "case (_,4) { return 2 }"+
                "default  { return 3 } }",2L);

        check("struct P { var x: Int; var y: Int; var z: Int}" +
                "var p: P = $P(4, 5, 9)" +
                "switch(p){"+
                "case (1,1,2) { return 1 }"+
                "case (_,_,_) { return 2 }"+
                "default  { return 3 } }",2L);

        check("switch(2) { }"+
            "return 2",2L);
    }

    @Test public void testOptional() {
        check("var i :Int? = 2"+
            "return i!",2L);

        check("var i :Int?"+
            "i = 5" +
            "return i!",5L);

        check("var i :Int? = 1"+
            "var j :Int\n" +
            "j = i! + 2" +
            "return j",3L);

        checkThrows(
            "var test:String?" +
                "print(test!)",
            InterpreterException.class);

        check("var test:String?"+
            "return(test)",Null.INSTANCE);

        checkThrows(
            "var test:String\n" +
                "return test",
            InterpreterException.class);

        check("var i :Int? = 1"+
            "var j :Int? = i\n" +
            "i = 2" +
            "return j",1L);

        check("var i :Int? = 1"+
            "var j :Int? = i\n" +
            "j = 2" +
            "return j!",2L);

        check("var i :Int? = 1"+
            "var j :Int? = 3\n" +
            "j = i! + 1" +
            "return j!",2L);

        check("var i: Int? = 2"+
            "if var j: Int = i{"+
            "return j}"+
            "return 1",2L);
        check("var i: Int?"+
            "if var j: Int = i{"+
            "return j}"+
            "return 1",1L);


    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testClosures() {
        check("fun add ( num : Int ) : (Int, Int) -> Int {\n" +
            "    var base : Int = 42\n" +
            "    return { (x,y) in\n" +
            "        x = x + y\n" +
            "        return x\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "var adder1 : (Int,Int) -> Int = add(1)\n" +
            "return adder1(2,3)",5L);

        check("fun first ( array : Int [], closure : ( Int ) -> Bool ): Int {\n" +
            "    var i : Int = 0\n" +
            "    while i < array . length {\n" +
            "        var elem : Int = array [i]\n" +
            "        if closure ( elem )\n" +
            "            return elem\n" +
            "        i = i + 1\n" +
            "     }\n" +
            "     return i\n" +
            "}\n" +
            "\n" +
            "var intArray : Int [] = [2 ,6 ,4 ,3]\n" +
            "var firstElemGreaterThan5 : Int = first ( intArray ,{ x in\n" +
            "     if x > 5\n" +
            "     return true\n" +
            "     else\n" +
            "     return false })\n" +
            "return firstElemGreaterThan5",6L);

        check("fun first ( array : Int [], closure : ( Int ) -> Bool ): Int {\n" +
            "    var i : Int = 0\n" +
            "    while i < array . length {\n" +
            "        var elem : Int = array [i]\n" +
            "        if closure ( elem )\n" +
            "            return elem\n" +
            "        i = i + 1\n" +
            "     }\n" +
            "     return i\n" +
            "}\n" +
            "\n" +
            "var intArray : Int [] = [2 ,-1,4,9,6 ,4 ,3]\n" +
            "var firstElemGreaterThan5 : Int = first ( intArray ,{ x in\n" +
            "     if x > 5\n" +
            "     return true\n" +
            "     else\n" +
            "     return false })\n" +
            "return firstElemGreaterThan5",9L);

        check("fun first ( array : Int [], closure : ( Int ) -> Bool ): Int {\n" +
            "    var i : Int = 0\n" +
            "    while i < array . length {\n" +
            "        var elem : Int = array [i]\n" +
            "        if closure ( elem )\n" +
            "            return elem\n" +
            "        i = i + 1\n" +
            "     }\n" +
            "     return i\n" +
            "}\n" +
            "\n" +
            "var intArray : Int [] = [2 ,1 ,4 ,3]\n" +
            "var firstElemGreaterThan5 : Int = first ( intArray ,{ x in\n" +
            "     if x > 5\n" +
            "     return true\n" +
            "     else\n" +
            "     return false })\n" +
            "return firstElemGreaterThan5",4L);

        check("fun adder(x : Int) : (Int) -> Int {" +
            "return { y in " +
            "return y+x}}" +
            "var adder5 : (Int) -> Int = adder(5)" +
            "return adder5(3)",8L);

        check("fun f(closure : (Int,Int) -> Bool, x : Int, y : Int) : Int { " +
            "if closure(x,y) return 1 " +
            "else return -1" +
            "} " +
            "var array : Int[] = [1,2,3,4,5,6] " +
            "var result : Int = f({ (a,b) in " +
            "return array[a]>b },3,6) " +
            "return result",-1L);

        check("fun add ( num : Int ) : () -> Int {\n" +
            "var base : Int = 10\n" +
            "return {\n" +
            "base = base + num\n" +
            "return base\n" +
            "}\n" +
            "}\n" +
            "var adder1 : () -> Int = add(1)\n" +
            "var res1 : Int = adder1 () \n" +
            "var res2 : Int = adder1 () \n" +
            "var adder2 : () -> Int = add(2)\n" +
            "var res3 : Int = adder2 () " +
            "return res3",12L);

        check("fun add ( num : Int ) : () -> Int {\n" +
            "var base : Int = 10\n" +
            "return {\n" +
            "base = base + num\n" +
            "return base\n" +
            "}\n" +
            "}\n" +
            "var adder1 : () -> Int = add(1)\n" +
            "var res1 : Int = adder1 () \n" +
            "var res2 : Int = adder1 () \n" +
            "return res2",12L);

    }

    @Test public void testListComprehension(){
        check("var list:Int[] = [1,2,3]\n" +
            "var test:Int[] = [ x+1 for x:Int in list]"+
            "print(\"\"+test)",null,"[2, 3, 4]\n");

        check("var list:Int[] = [1,2,3]\n" +
            "var test:Int[] = [ x*2 for x:Int in list if x >= 2 ]"+
            "print(\"\"+test)",null,"[4, 6]\n");

        check("var list:String[] = [\"a\"]\n" +
            "var test:Int[] = [ 1 for x:String in list]"+
            "print(\"\"+test)",null,"[1]\n");

        check("var test:Int[] = [ x for x:Int in [5,7,9] if x >= 6 ]"+
            "print(\"\"+test)",null,"[7, 9]\n");

        check("struct Pair {\n"+
            "var a: Int\n"+
            "var b: Int}\n"+
            "var list:Pair[] = [$Pair(0,1),$Pair(2,3),$Pair(4,5)]\n" +
            "var test:Int[] = [ x.a+x.b for x:Pair in list if x.a <=2 && x.b >= 3]\n"+
            "print(\"\"+test)",null,"[5]\n");



    }

    // ---------------------------------------------------------------------------------------------

    // NOTE(norswap): Not incredibly complete, but should cover the basics.
}
