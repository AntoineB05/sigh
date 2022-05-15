import norswap.autumn.Autumn;
import norswap.autumn.AutumnTestFixture;
import norswap.autumn.Grammar;
import norswap.autumn.Grammar.rule;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMap;
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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;

import static norswap.utils.Util.cast;
import static org.testng.Assert.*;

public final class ProgramTest {

    public void runAndCheckOutputProgram(String programFile, String expectedOutput){
        String path = Paths.get("programTest/", programFile).toAbsolutePath().toString();
        String src = IO.slurp(path);
        SighGrammar grammar = new SighGrammar();
        ParseOptions options = ParseOptions.builder().recordCallStack(true).get();
        ParseResult result = Autumn.parse(grammar.root, src, options);

        if (!result.fullMatch)
            throw new AssertionError("Grammar not full parse");


        SighNode tree = cast(result.topValue());
        Reactor reactor = new Reactor();
        Walker<SighNode> walker = SemanticAnalysis.createWalker(reactor);
        walker.walk(tree);
        reactor.run();
        if (!reactor.errors().isEmpty())
            throw new AssertionError("Semantic Error");


        Interpreter interpreter = new Interpreter(reactor);
        Pair<String, Object> resultInterpret = IO.captureStdout(() -> interpreter.interpret(tree));
        if (expectedOutput != null) assertEquals(resultInterpret.a, expectedOutput);
    }

    @Test
    public void testPatternMatching () {
        runAndCheckOutputProgram("patternMatching1.si","block 2\n");
        runAndCheckOutputProgram("patternMatching2.si","b is 1\n");
        runAndCheckOutputProgram("patternMatching3.si","true\ntrue\nfalse\n");
    }

    @Test
    public void closures() {
        runAndCheckOutputProgram("closures1.si","6\n");

        runAndCheckOutputProgram("closures2.si","43\n44\n44\n46\n");

        runAndCheckOutputProgram("closures3.si","0\n1\n0\n2\n0\n1\n0\n2\n10\n11\n10\n");
    }


    @Test
    public void testOptional () {
        runAndCheckOutputProgram("optional1.si","null\n");
        try{
            runAndCheckOutputProgram("optional2.si","");
        }catch (Exception e){
            assertEquals(e.getMessage(),"error : variable notOptional not initialize");
        }
        runAndCheckOutputProgram("optional3.si","4\n");
        runAndCheckOutputProgram("optional4.si","5\n");
        try{
            runAndCheckOutputProgram("optional5.si","");
        }catch (Exception e){
            assertEquals(e.getMessage(),"error : force to unwrap empty optional");
        }
    }

    @Test
    public void testListComprehension () {
        runAndCheckOutputProgram("listComprehension1.si","[4, 6]\n");
        runAndCheckOutputProgram("listComprehension2.si","[0, 2, 4, 8, 12]\n");
        runAndCheckOutputProgram("listComprehension3.si","[1, 3]\n");
        runAndCheckOutputProgram("listComprehension4.si","[0, 1, 2]\n");
    }

    @Test
    public void testAllFeatures () {
        runAndCheckOutputProgram("allFeatures.si","length list : 2\ncode 0\ncallback: code 0\n");
    }
}
