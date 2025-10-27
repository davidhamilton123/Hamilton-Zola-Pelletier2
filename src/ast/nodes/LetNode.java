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

/**
 * let <id> := <expr> in <expr>
 */
public final class LetNode extends SyntaxNode
{
    private final Token name;            // identifier token (holds the lexeme)
    private final SyntaxNode valueExpr;  // E1
    private final SyntaxNode body;       // E2

    public LetNode(Token name, SyntaxNode valueExpr, SyntaxNode body, long lineNumber)
    {
        super(lineNumber);
        this.name = name;
        this.valueExpr = valueExpr;
        this.body = body;
    }

    /**
     * ⟦let I := E1 in E2⟧_e  =  ⟦E2⟧_{ copy(e)[ I ← ⟦E1⟧_e ] }
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        try {
            // 1) Evaluate the bound expression in the ORIGINAL env
            Object boundValue = valueExpr.evaluate(env);

            // 2) Create an inner environment and bind I -> value
            Environment inner = env.copy();
            inner.updateEnvironment(name, boundValue);

            // 3) Evaluate the body in the extended environment
            return body.evaluate(inner);
        } catch (EvaluationException e) {
            // Already a user-facing error; include line number
            logError(e.getMessage());
            throw e;
        } catch (Exception e) {
            String msg = buildErrorMessage("Error evaluating let: " + e.getMessage());
            logError(msg);
            throw new EvaluationException();
        }
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("Let", indentAmt);
        printIndented("name: " + name.getValue(), indentAmt + 2);
        printIndented("value:", indentAmt + 2);
        valueExpr.displaySubtree(indentAmt + 4);
        printIndented("in:", indentAmt + 2);
        body.displaySubtree(indentAmt + 4);
    }
}
