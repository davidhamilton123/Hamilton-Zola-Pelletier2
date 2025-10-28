/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ast.nodes;

import ast.EvaluationException;
import environment.Environment;
import lexer.Token;
import lexer.TokenType;

/**
 * This is a class that represents a token node in the AST (Abstract Syntax Tree).
 * This is responsible for handling identifiers, literals (int, real, boolean),
 * and ensuring correct evaluation by retrieving values from the Environment.
 *
 * Author: David Hamilton (Syntax & Evaluation Lead)
 * Course: CSC3120 – Programming Languages, Fall 2025
 * Instructor: Prof. Zach Kissel
 */
public final class TokenNode extends SyntaxNode {

    /** This is the token represented by this node. */
    private final Token tok;

    /**
     * This is the constructor that creates a new TokenNode.
     * 
     * @param tok   This is the token to associate with this node.
     * @param line  This is the line number in the source code.
     */
    public TokenNode(Token tok, long line) {
        super(line);
        this.tok = tok;
    }

    /**
     * This is a method that displays this subtree in the AST with indentation.
     * This is useful for debugging and AST visualization.
     * 
     * @param indentAmt This is the indentation level to print with.
     */
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Token(" + tok + ")", indentAmt);
    }

    /**
     * This is a method that evaluates this token node within the given environment.
     * This is responsible for:
     *  - Returning numeric, real, or boolean literals
     *  - Looking up an identifier's value in the environment
     * 
     * @param env This is the current execution environment.
     * @return This is the evaluated value (Integer, Double, Boolean, or bound value).
     * @throws EvaluationException This is thrown if the token is invalid or unbound.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        final TokenType type = tok.getType();
        final String lex = tok.getValue();

        try {
            switch (type) {
                // This is an integer literal.
                case INT:
                    return Integer.valueOf(lex);

                // This is a real (floating point) literal.
                case REAL:
                    return Double.valueOf(lex);

                // This is a boolean true literal.
                case TRUE:
                    return Boolean.TRUE;

                // This is a boolean false literal.
                case FALSE:
                    return Boolean.FALSE;

                // This is an identifier (variable name).
                case ID: {
                    // This is where we look up the variable name in the environment.
                    Object val = env.lookup(tok);
                    if (val == null) {
                        logError("Unbound identifier: " + lex);
                        throw new EvaluationException("Unbound identifier: " + lex);
                    }
                    return val;
                }

                // This is an unexpected token case.
                default:
                    logError("Unexpected token type in TokenNode: " + type);
                    throw new EvaluationException("Unexpected token type in TokenNode: " + type);
            }
        } 
        catch (NumberFormatException nfe) {
            // This is for catching invalid numeric literals.
            logError("Invalid numeric literal: " + lex);
            throw new EvaluationException("Invalid numeric literal: " + lex);
        }
    }
}
