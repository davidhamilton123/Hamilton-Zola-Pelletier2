/*
 *  This is the UnaryOpNode class.
 *  It handles unary operators like "not" and "-" in MFL.
 *  Author: David Hamilton
 */

package ast.nodes;

import ast.EvaluationException;
import environment.Environment;

public final class UnaryOpNode extends SyntaxNode {

    // This is the operator (either "not" or "-")
    private final String op;

    // This is the right-hand side expression (the value we apply the operator to)
    private final SyntaxNode rhs;

    // This is the constructor that sets the operator, operand, and line number
    public UnaryOpNode(String op, SyntaxNode rhs, long line) {
        super(line);
        this.op = op;
        this.rhs = rhs;
    }

    // This is for displaying the AST (for debugging or the --ast flag)
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("UnaryOp(" + op + ")", indentAmt);
        rhs.displaySubtree(indentAmt + 1);
    }

    // This is where the actual evaluation happens
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object value = rhs.evaluate(env);

        switch (op) {
            // This is the "not" operator, which flips a boolean value
            case "not":
                if (!(value instanceof Boolean)) {
                    logError("This is an invalid 'not' operation on a non-boolean value.");
                    throw new EvaluationException("This is an invalid 'not' operation on a non-boolean value.");
                }
                return !((Boolean) value);

            // This is the unary "-" operator, which negates numbers
            case "-":
                if (value instanceof Integer) {
                    return -((Integer) value);
                } else if (value instanceof Double) {
                    return -((Double) value);
                } else {
                    logError("This is an invalid '-' operation on a non-numeric value.");
                    throw new EvaluationException("This is an invalid '-' operation on a non-numeric value.");
                }

            // This is a catch-all for unexpected operators
            default:
                logError("This is an unknown unary operator: " + op);
                throw new EvaluationException("This is an unknown unary operator: " + op);
        }
    }
}
