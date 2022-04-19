import norswap.autumn.AutumnTestFixture;
import norswap.autumn.positions.LineMapString;
import norswap.sigh.SemanticAnalysis;
import norswap.sigh.SighGrammar;
import norswap.sigh.ast.SighNode;
import norswap.uranium.Reactor;
import norswap.uranium.UraniumTestFixture;
import norswap.utils.visitors.Walker;
import org.testng.annotations.Test;

/**
 * NOTE(norswap): These tests were derived from the {@link InterpreterTests} and don't test anything
 * more, but show how to idiomatically test semantic analysis. using {@link UraniumTestFixture}.
 */
public final class SemanticAnalysisTests extends UraniumTestFixture
{
    // ---------------------------------------------------------------------------------------------

    private final SighGrammar grammar = new SighGrammar();
    private final AutumnTestFixture autumnFixture = new AutumnTestFixture();

    {
        autumnFixture.rule = grammar.root();
        autumnFixture.runTwice = false;
        autumnFixture.bottomClass = this.getClass();
    }

    private String input;

    @Override protected Object parse (String input) {
        this.input = input;
        return autumnFixture.success(input).topValue();
    }

    @Override protected String astNodeToString (Object ast) {
        LineMapString map = new LineMapString("<test>", input);
        return ast.toString() + " (" + ((SighNode) ast).span.startString(map) + ")";
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected void configureSemanticAnalysis (Reactor reactor, Object ast) {
        Walker<SighNode> walker = SemanticAnalysis.createWalker(reactor);
        walker.walk(((SighNode) ast));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testLiteralsAndUnary() {
        successInput("return 42");
        successInput("return 42.0");
        successInput("return \"hello\"");
        successInput("return (42)");
        successInput("return [1, 2, 3]");
        successInput("return true");
        successInput("return false");
        successInput("return null");
        successInput("return !false");
        successInput("return !true");
        successInput("return !!true");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testNumericBinary() {
        successInput("return 1 + 2");
        successInput("return 2 - 1");
        successInput("return 2 * 3");
        successInput("return 2 / 3");
        successInput("return 3 / 2");
        successInput("return 2 % 3");
        successInput("return 3 % 2");

        successInput("return 1.0 + 2.0");
        successInput("return 2.0 - 1.0");
        successInput("return 2.0 * 3.0");
        successInput("return 2.0 / 3.0");
        successInput("return 3.0 / 2.0");
        successInput("return 2.0 % 3.0");
        successInput("return 3.0 % 2.0");

        successInput("return 1 + 2.0");
        successInput("return 2 - 1.0");
        successInput("return 2 * 3.0");
        successInput("return 2 / 3.0");
        successInput("return 3 / 2.0");
        successInput("return 2 % 3.0");
        successInput("return 3 % 2.0");

        successInput("return 1.0 + 2");
        successInput("return 2.0 - 1");
        successInput("return 2.0 * 3");
        successInput("return 2.0 / 3");
        successInput("return 3.0 / 2");
        successInput("return 2.0 % 3");
        successInput("return 3.0 % 2");

        failureInputWith("return 2 + true", "Trying to add Int with Bool");
        failureInputWith("return true + 2", "Trying to add Bool with Int");
        failureInputWith("return 2 + [1]", "Trying to add Int with Int[]");
        failureInputWith("return [1] + 2", "Trying to add Int[] with Int");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testOtherBinary() {
        successInput("return true && false");
        successInput("return false && true");
        successInput("return true && true");
        successInput("return true || false");
        successInput("return false || true");
        successInput("return false || false");

        failureInputWith("return false || 1",
            "Attempting to perform binary logic on non-boolean type: Int");
        failureInputWith("return 2 || true",
            "Attempting to perform binary logic on non-boolean type: Int");

        successInput("return 1 + \"a\"");
        successInput("return \"a\" + 1");
        successInput("return \"a\" + true");

        successInput("return 1 == 1");
        successInput("return 1 == 2");
        successInput("return 1.0 == 1.0");
        successInput("return 1.0 == 2.0");
        successInput("return true == true");
        successInput("return false == false");
        successInput("return true == false");
        successInput("return 1 == 1.0");

        failureInputWith("return true == 1", "Trying to compare incomparable types Bool and Int");
        failureInputWith("return 2 == false", "Trying to compare incomparable types Int and Bool");

        successInput("return \"hi\" == \"hi\"");
        successInput("return [1] == [1]");

        successInput("return 1 != 1");
        successInput("return 1 != 2");
        successInput("return 1.0 != 1.0");
        successInput("return 1.0 != 2.0");
        successInput("return true != true");
        successInput("return false != false");
        successInput("return true != false");
        successInput("return 1 != 1.0");

        failureInputWith("return true != 1", "Trying to compare incomparable types Bool and Int");
        failureInputWith("return 2 != false", "Trying to compare incomparable types Int and Bool");

        successInput("return \"hi\" != \"hi\"");
        successInput("return [1] != [1]");

        successInput("return 3 is Int");
        successInput("return \"hi\" is Float");
        successInput("return 0.3465 is Void");
        successInput("return 8 is Int");
        successInput("return true is Bool");
        successInput(
                "struct P { var x: Int; var y: Int }" +
                        "var p: P = $P(1, 2);" +
                        "return p is P");

        failureInput("return false is false");

    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testVarDecl() {
        successInput("var x: Int = 1; return x");
        successInput("var x: Float = 2.0; return x");

        successInput("var x: Int = 0; return x = 3");
        successInput("var x: String = \"0\"; return x = \"S\"");

        failureInputWith("var x: Int = true", "expected Int but got Bool");
        failureInputWith("return x + 1", "Could not resolve: x");
        failureInputWith("return x + 1; var x: Int = 2", "Variable used before declaration: x");

        // implicit conversions
        successInput("var x: Float = 1 ; x = 2");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testRootAndBlock () {
        successInput("return");
        successInput("return 1");
        successInput("return 1; return 2");

        successInput("print(\"a\")");
        successInput("print(\"a\" + 1)");
        successInput("print(\"a\"); print(\"b\")");

        successInput("{ print(\"a\"); print(\"b\") }");

        successInput(
            "var x: Int = 1;" +
            "{ print(\"\" + x); var x: Int = 2; print(\"\" + x) }" +
            "print(\"\" + x)");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testCalls() {
        successInput(
            "fun add (a: Int, b: Int): Int { return a + b } " +
            "return add(4, 7)");

        successInput(
            "struct Point { var x: Int; var y: Int }" +
            "return $Point(1, 2)");

        successInput("var str: String = null; return print(str + 1)");

        failureInputWith("return print(1)", "argument 0: expected String but got Int");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testArrayStructAccess() {
        successInput("return [1][0]");
        successInput("return [1.0][0]");
        successInput("return [1, 2][1]");

        failureInputWith("return [1][true]", "Indexing an array using a non-Int-valued expression");

        // TODO make this legal?
        // successInput("[].length", 0L);

        successInput("return [1].length");
        successInput("return [1, 2].length");

        successInput("var array: Int[] = null; return array[0]");
        successInput("var array: Int[] = null; return array.length");

        successInput("var x: Int[] = [0, 1]; x[0] = 3; return x[0]");
        successInput("var x: Int[] = []; x[0] = 3; return x[0]");
        successInput("var x: Int[] = null; x[0] = 3");

        successInput(
            "struct P { var x: Int; var y: Int }" +
            "return $P(1, 2).y");

        successInput(
            "struct P { var x: Int; var y: Int }" +
            "var p: P = null;" +
            "return p.y");

        successInput(
            "struct P { var x: Int; var y: Int }" +
            "var p: P = $P(1, 2);" +
            "p.y = 42;" +
            "return p.y");

        successInput(
            "struct P { var x: Int; var y: Int }" +
            "var p: P = null;" +
            "p.y = 42");

        failureInputWith(
            "struct P { var x: Int; var y: Int }" +
            "return $P(1, true)",
            "argument 1: expected Int but got Bool");

        failureInputWith(
            "struct P { var x: Int; var y: Int }" +
            "return $P(1, 2).z",
            "Trying to access missing field z on struct P");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testIfWhile () {
        successInput("if (true) return 1 else return 2");
        successInput("if (false) return 1 else return 2");
        successInput("if (false) return 1 else if (true) return 2 else return 3 ");
        successInput("if (false) return 1 else if (false) return 2 else return 3 ");

        successInput("var i: Int = 0; while (i < 3) { print(\"\" + i); i = i + 1 } ");

        failureInputWith("if 1 return 1",
            "If statement with a non-boolean condition of type: Int");
        failureInputWith("while 1 return 1",
            "While statement with a non-boolean condition of type: Int");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testInference() {
        successInput("var array: Int[] = []");
        successInput("var array: String[] = []");
        successInput("fun use_array (array: Int[]) {} ; use_array([])");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testTypeAsValues() {
        successInput("struct S{} ; return \"\"+ S");
        successInput("struct S{} ; var type: Type = S ; return \"\"+ type");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testUnconditionalReturn()
    {
        successInput("fun f(): Int { if (true) return 1 else return 2 } ; return f()");

        // TODO: would be nice if this pinpointed the if-statement as missing the return,
        //   not the whole function declaration
        failureInputWith("fun f(): Int { if (true) return 1 } ; return f()",
            "Missing return in function");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testSwitches() {
        successInput(
                "switch(1){ case 1 { print(\"i=1\") } "+
                            "case 2{ print(\"i=2\") } }"
        );

        successInput(
                "var str : String = \"Hello\""+
                "switch(str){"+
                        "case \"Hi\"{ print(\"Hi\") } }"
        );

        successInput(
                "var num : Int = 3"+
                "switch(num){"+
                        "case 3 { print(\"3\") }"+
                        "case 3 { print(\"3\") }"+
                        "default  { print(\"3\") }"+
                        "case 3 { print(\"3\") } }"
        );

        successInput(
                "struct P { var x: Int; var y: Int }" +
                "var p: P = $P(1, 2)" +
                "switch(p){"+
                        "case (1,1) { print(\"1 and 1\") }"+
                        "case (_,3) { print(\"? and 3\") }"+
                        "case (1,_) { print(\"1 and ?\") }"+
                        "case (_,_) { print(\"this is default\") }"+
                        "default  { print(\"other\") } }"
        );

        successInput(
            "struct P { var x: Int; var y: String }" +
                "var p: P = $P(1, \"b\")" +
                "switch(p){"+
                "case (1,\"a\") { print(\"1 and a\") }"+
                "case (_,\"b\") { print(\"? and b\") }"+
                "case (1,_) { print(\"1 and ?\") }"+
                "case (_,_) { print(\"this is default\") }"+
                "default  { print(\"other\") } }"
        );

        failureInputWith(
                "switch(1){ case 1 { print(\"i=1\") } "+
                        "default { print(\"else\") } "+
                        "default { print(\"something else\") } }",
                "Too many default case, expected 0 or 1 but got 2"

        );

        failureInputWith(
                "switch (1) { if (3 > 0) {return 3}}",
                "Unexpected Statement in the switch block"
        );

        failureInputWith(
                "switch(1){ case \"1\" { print(\"i=1\") } "+
                        "case 2{ print(\"i=2\") } }",
                "Type of the case expression doesn't match with the type of the switch expression," +
                        " expected Int but got String"
        );

        failureInputWith(
                "switch(3){"+
                        "case (1,1) { print(\"1 and 1\") }"+
                        "case (_,3) { print(\"? and 3\") }"+
                        "default  { print(\"other\") } }",
                "Type of the case expression doesn't match with the type of the switch expression," +
                        " expected Int but got StructMatching"
        );

        failureInputWith(
                "struct P { var x: Int; var y: Int }" +
                        "var p: P = $P(1, 2)" +
                        "switch(p){"+
                        "case (_,\"3\") { print(\"? and 3\") }"+
                        "default  { print(\"other\") } }",
                "Type of the field number 1 doesn't match with the type of corresponding field of the switch structure," +
                        " expected Int but got String"
        );

        failureInputWith(
                "struct P { var x: Int; var y: Int }" +
                        "var p: P = $P(1, 2)" +
                        "switch(p){"+
                        "case (_,3,_) { print(\"? and 3\") }"+
                        "default  { print(\"other\") } }",
                "Number of struct fields in case doesn't match with the number of fields of the switch structure," +
                        " expected 2 but got 3"
        );

        failureInputWith(
                "struct P { var x: Int; var y: Int ; var z: Int}" +
                        "var p: P = $P(1, 2, 3)" +
                        "switch(p){"+
                        "case (_,3) { print(\"? and 3\") }"+
                        "default  { print(\"other\") } }",
                "Number of struct fields in case doesn't match with the number of fields of the switch structure," +
                        " expected 3 but got 2"
        );

        failureInputWith(
            "struct P { var x: Int}" +
                "var p: P = $P(1)" +
                "switch(p){"+
                "case () { print(\"1\") }"+
                "default  { print(\"other\") } }",
            "Number of struct fields in case doesn't match with the number of fields of the switch structure," +
                " expected 1 but got 0"
        );
    }

    // ---------------------------------------------------------------------------------------------


    @Test public void testClosures() {
        successInput("fun f() : (Int, Int[]) -> Int {" +
            "return { (index, array) in " +
            "return array[index]}}" +
            "var indexer : (Int, Int[]) -> Int = f()" +
            "indexer(1,[1,2,3,4,5])");

        successInput("fun f(x : Float) : (String) -> Void {" +
            "return { (str) in " +
            "print(str)" +
            "}}" +
            "var printer : (String) -> Void = f(3.2)" +
            "printer(\"Hello\")");

        successInput("fun f(x : Float, closure : (Float) -> Bool) : Float {" +
            "if (closure(x)) " +
            "return x " +
            "else " +
            "return 2" +
            "}" +
            "var result : Float = f(3,{ (x) in " +
            "return x>3.6" +
            "})");

        failureInputWith("fun add ( num : Int ) : (Int, Int) -> Int {"+
            "var base : Int = 42 "+
            "return { (x) in " +
            "return x}}","wrong number of arguments, expected 2 but got 1",
            "Incompatible return type, expected (Int,Int) -> Int but got (1 parameters) -> Int"
        );

        failureInputWith("fun add ( num : Int ) : (Int, String) -> Int {"+
            "var base : Int = 42 "+
            "return { (x,y) in " +
            "return y}}","Incompatible return type, expected Int but got String");

        failureInputWith("fun f(x : Float) : (String) -> Void {" +
            "return { (str) in " +
            "print(str)" +
            "}}" +
            "var printer : (String) -> Void = f(3.2)" +
            "printer(3.6)","incompatible argument provided for argument 0: expected String but got Float");

        failureInputWith("fun f(x : Float) : (String) -> Void {" +
            "return { (str) in " +
            "print(str)" +
            "}}" +
            "var printer : (String) -> Void = f(3.2)" +
            "printer(\"Hello\",3.2)","wrong number of arguments, expected 1 but got 2");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testDeclOptional(){
        successInput("var i:Int? = 2");
        successInput("var i:Int?");

        failureInputWith("var i:Int? = \"abc\"","incompatible initializer type provided for variable `i`: expected Int but got String");

    }

    @Test public void testAssignToOptional(){
        successInput("var i:Int? = 2"+
            "var sum: Int = 0"+
            "i = sum + 3");
        successInput("var test:String?"+
            "test = \"abc\"");

        failureInputWith("var test:String?"+
            "test = 3","Trying to assign a value to a non-compatible lvalue (expected: String but got Int");
    }

    @Test public void testAssignOptionalToOptional(){
        successInput("var i:Int? = 2"+
            "var sum: Int? = i\n"+
            "i = sum! + 3");
        successInput("var test:String?"+
            "var test2 : String? = test\n"+
            "print(test2!)");

        failureInputWith("var test:String?"+
            "var i: Int? = test","incompatible initializer type provided for variable `i`: expected Optional(Int) but got Optional(String)");
    }

    @Test public void testUnwrapOptionalWithBang(){
        successInput("var i:Int? = 2"+
            "var sum: Int = 0"+
            "sum = sum + i!");

        failureInputWith("var test:String?"+
            "test = \"zyx\""+
            "var i: Int = 4"+
            "i = test","Trying to assign a value to a non-compatible lvalue.");

        failureInputWith("var test: String?"+
            "test = \"zyx\""+
            "var i:Int = 4"+
            "i = test!","Trying to assign a value to a non-compatible lvalue.");
    }

    @Test public void testUnwrapOptionalWithIf(){
        successInput("var i:Int? = 2"+
            "if var iUnwrap:Int = i{}");

        failureInputWith("var i:Int = 2"+
            "if var iUnwrap:Int = i{}","impossible to unwrap non optional variable (got Int)","Need optional in this if statement condition to be unwrap but got Int");

        successInput("var j: Int\n" +
            "var i:Int? = 2"+
            "if var iUnwrap:Int = i{j = iUnwrap}");

    }

}
