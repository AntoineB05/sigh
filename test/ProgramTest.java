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
    public void patternMatching () {
        runAndCheckOutputProgram("patternMatching1.si","block 2\n");
    }



}
