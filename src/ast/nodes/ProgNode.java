/*
 *  This is the ProgNode class.
 *  This is responsible for evaluating a full MFL program (a list of <val> items).
 *  This is where we walk each top-level statement, bind globals, and track the last result.
 *  Author: David Hamilton
 */

package ast.nodes;

import java.util.List;

import ast.EvaluationException;
import environment.Environment;

public final class ProgNode extends SyntaxNode {

    // This is the ordered list of top-level statements (<val> ::= "val ..." | <expr>)
    private final List<SyntaxNode> statements;

    // This is the constructor that sets the statement list and line number
    public ProgNode(List<SyntaxNode> statements, long line) {
        super(line);
        this.statements = statements;
    }

    // This is for AST printing (used by --ast)
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Prog", indentAmt);
        for (SyntaxNode s : statements) {
            s.displaySubtree(indentAmt + 1);
        }
    }

    // This is where the program actually evaluates
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        // This is the running "last value" to return at the end
        Object last = null;

        // This is iterating through each top-level statement in order
        for (SyntaxNode stmt : statements) {
            // This is handling a declaration: val <id> := <expr>
            if (stmt instanceof ValNode v) {
                // This is evaluating the RHS expression in the current environment
                Object value = v.getExpr().evaluate(env);

                // This is updating the global binding as required by the spec
                // (val has global scope; we bind directly into the given env)
                env.updateEnvironment(v.getName(), value);

                // This is updating the last value so the program's result makes sense if the last is a val
                last = value;

                // This is continuing to the next top-level statement
                continue;
            }

            // This is evaluating a plain expression and remembering its result
            last = stmt.evaluate(env);
        }

        // This is returning the value of the last statement evaluated (null if program was empty)
        return last;
    }
}
