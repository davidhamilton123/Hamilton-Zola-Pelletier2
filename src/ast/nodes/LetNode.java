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
        // 1) Evaluate E1 in the *original* environment
        Object boundValue = valueExpr.evaluate(env);

        // 2) Create a new environment and bind I -> value (using Token API)
        Environment inner = env.copy();
        inner.updateEnvironment(name, boundValue);

        // 3) Evaluate the body under the extended environment
        return body.evaluate(inner);
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
