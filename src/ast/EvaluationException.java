/*
 *  This is the EvaluationException class.
 *  This is used to represent any evaluation problem in the interpreter.
 *  This is thrown whenever something goes wrong during program execution,
 *  like unbound variables, invalid types, or bad operations.
 *  Author: David Hamilton
 */

package ast;

public class EvaluationException extends Exception {

    // This is the constructor with a default message
    public EvaluationException() {
        super("Interpretation failed.");
    }

    // This is the constructor that takes a custom message
    public EvaluationException(String message) {
        super(message);
    }

    // This is the constructor that lets you pass a cause (optional)
    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
