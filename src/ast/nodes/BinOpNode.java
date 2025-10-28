/*
 *  This is the BinOpNode class.
 *  This is responsible for evaluating binary operators in MFL.
 *  This is where we handle: +, -, *, /, mod, and, or.
 *  Author: David Hamilton
 */

package ast.nodes;

import ast.EvaluationException;
import environment.Environment;

public final class BinOpNode extends SyntaxNode {

    // This is the operator string (e.g., "+", "-", "*", "/", "mod", "and", "or")
    private final String op;

    // This is the left-hand side expression
    private final SyntaxNode left;

    // This is the right-hand side expression
    private final SyntaxNode right;

    // This is the constructor that wires up the children and the operator
    public BinOpNode(String op, SyntaxNode left, SyntaxNode right, long line) {
        super(line);
        this.op = op;
        this.left = left;
        this.right = right;
    }

    // This is for pretty-printing the AST (used by --ast)
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("BinOp(" + op + ")", indentAmt);
        left.displaySubtree(indentAmt + 1);
        right.displaySubtree(indentAmt + 1);
    }

    // This is where we actually evaluate the binary operation
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object lv = left.evaluate(env);
        Object rv = right.evaluate(env);

        // This is handling boolean operators first (and / or)
        if ("and".equals(op) || "or".equals(op)) {
            if (!(lv instanceof Boolean) || !(rv instanceof Boolean)) {
                logError("This is a boolean operator used with non-boolean operands.");
                throw new EvaluationException("This is a boolean operator used with non-boolean operands.");
            }
            boolean L = (Boolean) lv;
            boolean R = (Boolean) rv;
            return "and".equals(op) ? (L && R) : (L || R);
        }

        // This is ensuring we have numbers for arithmetic and relational-style math ops
        if (!(lv instanceof Number) || !(rv instanceof Number)) {
            logError("This is an arithmetic operator used with non-numeric operands.");
            throw new EvaluationException("This is an arithmetic operator used with non-numeric operands.");
        }

        // This is enforcing the no mixed-type rule (ints with ints, reals with reals)
        boolean bothInts = (lv instanceof Integer) && (rv instanceof Integer);
        boolean bothReals = (lv instanceof Double) && (rv instanceof Double);

        if (!(bothInts || bothReals)) {
            logError("This is a mixed numeric type expression, which is not allowed.");
            throw new EvaluationException("This is a mixed numeric type expression, which is not allowed.");
        }

        // This is the integer arithmetic branch
        if (bothInts) {
            int L = (Integer) lv;
            int R = (Integer) rv;

            try {
                switch (op) {
                    case "+":
                        return L + R;
                    case "-":
                        return L - R;
                    case "*":
                        return L * R;
                    case "/":
                        // This is integer division (Java will throw on divide by zero)
                        return L / R;
                    case "mod":
                        // This is modulus (allowed only for integers)
                        return L % R;
                    default:
                        logError("This is an unknown integer operator: " + op);
                        throw new EvaluationException("This is an unknown integer operator: " + op);
                }
            } catch (ArithmeticException ae) {
                // This is wrapping runtime arithmetic errors (like divide by zero)
                logError("This is an arithmetic error: " + ae.getMessage());
                throw new EvaluationException("This is an arithmetic error: " + ae.getMessage());
            }
        }

        // This is the real (double) arithmetic branch
        double L = ((Number) lv).doubleValue();
        double R = ((Number) rv).doubleValue();

        if ("mod".equals(op)) {
            // This is rejecting mod on reals per the spec
            logError("This is an invalid use of 'mod' with real numbers.");
            throw new EvaluationException("This is an invalid use of 'mod' with real numbers.");
        }

        switch (op) {
            case "+":
                return L + R;
            case "-":
                return L - R;
            case "*":
                return L * R;
            case "/":
                return L / R;
            default:
                logError("This is an unknown real operator: " + op);
                throw new EvaluationException("This is an unknown real operator: " + op);
        }
    }
}
