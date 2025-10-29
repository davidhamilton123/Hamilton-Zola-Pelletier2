/*
 *  This is the ValNode class.
 *  This is responsible for handling "val" declarations in MFL.
 *  This is where we deal with "val <id> := <expr>" statements.
 *  Author: David Hamilton
 */

package ast.nodes;

import ast.EvaluationException;
import environment.Environment;
import lexer.Token;

public final class ValNode extends SyntaxNode {

    // This is the identifier being declared
    private final Token id;

    // This is the expression that gives the identifier its value
    private final SyntaxNode expr;

    // This is the constructor that sets the id, expression, and line number
    public ValNode(Token id, SyntaxNode expr, long line) {
        super(line);
        this.id = id;
        this.expr = expr;
    }

    // This is for AST visualization (used with the --ast flag)
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Val(" + id.getValue() + " := )", indentAmt);
        expr.displaySubtree(indentAmt + 1);
    }

    // This is the evaluate method
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        // This is printing out the identifier name as per the spec
        return id.getValue();
    }

    // This is a helper to get the identifier name
    public String getName() {
        return id.getValue();
    }

    // This is a helper to get the expression part
    public SyntaxNode getExpr() {
        return expr;
    }

    // (helper needed to bind using Token in Environment)
    public Token getIdToken() {
        return id;
    }
}
