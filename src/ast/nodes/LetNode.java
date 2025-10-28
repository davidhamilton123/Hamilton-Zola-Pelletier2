/*
 *  This is the LetNode class.
 *  This is responsible for evaluating: let <id> := <expr> in <expr>
 *  This is where we create a new scope, bind the name, and evaluate the body.
 *  Author: David Hamilton
 */

package ast.nodes;

import ast.EvaluationException;
import environment.Environment;
import lexer.Token;

public final class LetNode extends SyntaxNode {

    // This is the identifier being introduced by the let
    private final Token id;

    // This is the expression that computes the value to bind to the identifier
    private final SyntaxNode boundExpr;

    // This is the body expression that runs with the new binding in scope
    private final SyntaxNode body;

    // This is the constructor wiring up id, bound expression, body, and line number
    public LetNode(Token id, SyntaxNode boundExpr, SyntaxNode body, long line) {
        super(line);
        this.id = id;
        this.boundExpr = boundExpr;
        this.body = body;
    }

    // This is for AST visualization (used by --ast)
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Let(" + id.getValue() + " := ... in ...)", indentAmt);
        printIndented("This is the bound expression:", indentAmt + 1);
        boundExpr.displaySubtree(indentAmt + 2);
        printIndented("This is the body:", indentAmt + 1);
        body.displaySubtree(indentAmt + 2);
    }

    // This is the evaluate method that enforces lexical scoping
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        // This is evaluating the bound expression in the current environment
        Object value = boundExpr.evaluate(env);

        // This is making a new environment (copy) for the body so we get proper shadowing
        Environment child = env.copy();

        // This is binding the identifier in the new environment
        child.updateEnvironment(id.getValue(), value);

        // This is evaluating the body in the new (child) environment
        return body.evaluate(child);
    }
}
