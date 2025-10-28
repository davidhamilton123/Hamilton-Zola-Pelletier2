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
 * This class represents a token node in the AST (Abstract Syntax Tree).
 * It handles identifiers, literals (int, real, boolean), and ensures
 * correct evaluation by retrieving values from the Environment.
 *
 * Author: David Hamilton (Syntax & Evaluation Lead)
 * Course: CSC3120 – Programming Languages, Fall 2025
 * Instructor: Prof. Zach Kissel
 */
public final class TokenNode extends SyntaxNode {

    /** The token represented by this node. */
    private final Token tok;

    /**
     * Constructs a new TokenNode.
     * 
     * @param tok   the token to associate with this node
     * @param line  the line number in the source code
     */
    public TokenNode(Token tok, long line) {
        super(line);
        this.tok = tok;
    }

    /**
     * Displays this subtree in the AST with indentation.
     * Useful for debugging and AST visualization.
     * 
     * @param indentAmt the indentation level to print with
     */
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Token(" + tok + ")", indentAmt);
    }

    /**
     * Evaluates this token node within the given environment.
     * Depending on the token type, it may:
     *  - Return a numeric, real, or boolean literal
     *  - Lookup an identifier's value in the environment
     * 
     * @param env the current execution environment
     * @return the evaluated value (Integer, Double, Boolean, or bound value)
     * @throws EvaluationException if the token is invalid or unbound
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        final TokenType type = tok.getType();
        final String lex = tok.getValue();

        try {
            switch (type) {
                // ----- Integer literal -----
                case INT:
                    return Integer.valueOf(lex);

                // ----- Real (floating point) literal -----
                case REAL:
                    return Double.valueOf(lex);

                // ----- Boolean true -----
                case TRUE:
                    return Boolean.TRUE;

                // ----- Boolean false -----
                case FALSE:
                    return Boolean.FALSE;

                // ----- Identifier (variable name) -----
                case ID: {
                    // Lookup the variable name in the environment.
                    // NOTE: If your Environment uses a different method, adjust this call.
                    Object val = env.lookup(lex);
                    if (val == null) {
                        logError("Unbound identifier: " + lex);
                        throw new EvaluationException("Unbound identifier: " + lex);
                    }
                    return val;
                }

                // ----- Any unexpected token -----
                default:
                    logError("Unexpected token type in TokenNode: " + type);
                    throw new EvaluationException("Unexpected token type in TokenNode: " + type);
            }
        } 
        catch (NumberFormatException nfe) {
            // Handles bad numeric conversions like malformed integers or reals.
            logError("Invalid numeric literal: " + lex);
            throw new EvaluationException("Invalid numeric literal: " + lex);
        }
    }
}
