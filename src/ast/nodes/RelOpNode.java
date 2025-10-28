/*
 *  This is the RelOpNode class.
 *  This is responsible for evaluating relational operators in MFL.
 *  This is where we handle: <, >, <=, >=, =, !=
 *  Author: David Hamilton
 */

package ast.nodes;

import ast.EvaluationException;
import environment.Environment;

public final class RelOpNode extends SyntaxNode {

    // This is the operator string (e.g., "<", ">", "<=", ">=", "=", "!=")
    private final String op;

    // This is the left-hand side expression
    private final SyntaxNode left;

    // This is the right-hand side expression
    private final SyntaxNode right;

    // This is the constructor wiring up operator and children
    public RelOpNode(String op, SyntaxNode left, SyntaxNode right, long line) {
        super(line);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    // This is for AST printing (used by --ast)
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("RelOp(" + op + ")", indentAmt);
        left.displaySubtree(indentAmt + 1);
        right.displaySubtree(indentAmt + 1);
    }

    // This is where we actually evaluate the relational expression
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object lv = left.evaluate(env);
        Object rv = right.evaluate(env);

        // This is the numeric comparison path
        if (lv instanceof Number && rv instanceof Number) {
            // This is enforcing the same-type rule (no mixed int/real)
            boolean bothInts = (lv instanceof Integer) && (rv instanceof Integer);
            boolean bothReals = (lv instanceof Double) && (rv instanceof Double);
            if (!(bothInts || bothReals)) {
                logError("This is a mixed numeric type comparison, which is not allowed.");
                throw new EvaluationException("This is a mixed numeric type comparison, which is not allowed.");
            }

            // This is converting to double for comparison (safe because types match)
            double L = ((Number) lv).doubleValue();
            double R = ((Number) rv).doubleValue();

            // This is returning a boolean result for numeric comparisons
            return switch (op) {
                case "<"  -> L <  R;
                case ">"  -> L >  R;
                case "<=" -> L <= R;
                case ">=" -> L >= R;
                case "="  -> L == R;
                case "!=" -> L != R;
                default -> {
                    logError("This is an unknown relational operator: " + op);
                    throw new EvaluationException("This is an unknown relational operator: " + op);
                }
            };
        }

        // This is the boolean comparison path (only "=" and "!=" make sense)
        if (lv instanceof Boolean && rv instanceof Boolean) {
            boolean L = (Boolean) lv;
            boolean R = (Boolean) rv;

            // This is limiting boolean comparisons to equality/inequality
            return switch (op) {
                case "="  -> L == R;
                case "!=" -> L != R;
                default -> {
                    logError("This is an invalid boolean comparison; only '=' and '!=' are allowed.");
                    throw new EvaluationException("This is an invalid boolean comparison; only '=' and '!=' are allowed.");
                }
            };
        }

        // This is rejecting cross-type comparisons outright
        logError("This is a relational comparison with incompatible operand types.");
        throw new EvaluationException("This is a relational comparison with incompatible operand types.");
    }
}
