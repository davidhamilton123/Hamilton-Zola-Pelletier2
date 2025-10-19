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
package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;

import ast.SyntaxTree;
import ast.nodes.BinOpNode;
import ast.nodes.ProgNode;
import ast.nodes.RelOpNode;
import ast.nodes.SyntaxNode;
import ast.nodes.TokenNode;
import ast.nodes.UnaryOpNode;
import ast.nodes.ValNode;
import lexer.Lexer;
import lexer.TokenType;
import lexer.Token;

/**
 * <p>
 * Parser for the MFL language. This is largely private methods where
 * there is one method the "eval" method for each non-terminal of the grammar.
 * There are also a collection of private "handle" methods that handle one
 * production associated with a non-terminal.
 * </p>
 * <p>
 * Each of the private methods operates on the token stream. It is important to
 * remember that all of our non-terminal processing methods maintain the
 * invariant
 * that each method leaves the concludes such that the next unprocessed token is
 * at
 * the front of the token stream. This means each method can assume the current
 * token
 * has not yet been processed when the method begins. The methods
 * {@code checkMatch}
 * and {@code match} are methods that maintain this invariant in the case of a
 * match.
 * The method {@code tokenIs} does NOT advnace the token stream. To advance the
 * token
 * stream the {@code nextTok} method can be used. In the rare cases that the
 * token
 * at the head of the stream must be accessed directly, the {@code getCurrToken}
 * method can be used.
 * </p>
 * 
 * @author Zach Kissel
 */
public class MFLParser extends Parser {

  /**
   * Constructs a new parser for the file {@code source} by setting up lexer.
   * 
   * @param src the source code file to parse.
   * @throws FileNotFoundException if the file can not be found.
   */
  public MFLParser(File src) throws FileNotFoundException {
    super(new Lexer(src));
  }

  /**
   * Construct a parser that parses the string {@code str}.
   * 
   * @param str the code to evaluate.
   */
  public MFLParser(String str) {
    super(new Lexer(str));
  }

  /**
   * Parses the file according to the grammar.
   * 
   * @return the abstract syntax tree representing the parsed program.
   * @throws ParseException when parsing fails.
   */
  public SyntaxTree parse() throws ParseException {
    SyntaxTree ast;

    nextToken(); // Get the first token.
    ast = new SyntaxTree(evalProg()); // Start processing at the root of the
                                      // tree.

    match(TokenType.EOF, "EOF");

    return ast;
  }

  /************
   * Evaluation methods to constrct the AST associated with the non-terminals
   ***********/
  /**
   * Method to evaluate the program non-terminal. <prog> -> <expr> { <expr> }
   * 
   * @throws ParseException if the evaluation of an expression fails.
   */
  private SyntaxNode evalProg() throws ParseException {
    LinkedList<SyntaxNode> exprs = new LinkedList<>();

    trace("Enter <prog>");
    while (!checkMatch(TokenType.EOF)) {
      SyntaxNode currNode = evalValues();
      if (currNode == null)
        break;

      // Make sure we have a semi colon ending the line.
      match(TokenType.SEMI, ";");

      exprs.add(currNode);
    }

    // We have an empty colleciton of expressions.
    if (exprs.size() == 0)
      return null;

    trace("Exit <prog>");
    return new ProgNode(exprs, super.getCurrLine());// lex.getLineNumber());
  }

  /**
   * Method to evaluate the <values> non-terminal
   * 
   * @throws ParseException if there is an error during parsing
   */
  private SyntaxNode evalValues() throws ParseException {
    // Function definition.
    if (checkMatch(TokenType.VAL))
      return getGoodParse(handleValues());
    else // Just an expression.
      return getGoodParse(evalExpr());
  }

  /**
   * Method to evaluate the expression non-terminal <expr>
   * 
   * @throws ParseException if there is an error during parsing.
   */
  private SyntaxNode evalExpr() throws ParseException {
    SyntaxNode rexpr;
    TokenType op;
    SyntaxNode expr = null;

    trace("Enter <expr>");

    expr = getGoodParse(evalRexpr());

    op = getCurrToken().getType(); // Save off the supposed operation.

    while (checkMatch(TokenType.AND) || checkMatch(TokenType.OR)) {
      rexpr = getGoodParse(evalRexpr());
      expr = new BinOpNode(expr, op, rexpr, getCurrLine());
      op = getCurrToken().getType();
    }
    trace("Exit <expr>");

    return expr;
  }

  /**
   * Evaluates relational expressions (the <rexpr> non-terminal)
   * 
   * @return a SyntaxNode representing the relation expression.
   * @throws ParseException when parsing fails.
   */
  private SyntaxNode evalRexpr() throws ParseException {
    SyntaxNode left = null;
    SyntaxNode right = null;
    TokenType op;

    left = getGoodParse(evalMexpr());

    op = getCurrToken().getType(); // Save off what should be the operator.
    if (checkMatch(TokenType.LT) || checkMatch(TokenType.LTE)
        || checkMatch(TokenType.GT) || checkMatch(TokenType.GTE)
        || checkMatch(TokenType.EQ) || checkMatch(TokenType.NEQ)) {
      right = getGoodParse(evalMexpr());
      return new RelOpNode(left, op, right, getCurrLine());
    }

    return left;
  }

  /**
   * evaluates the math expression non-terminal (mexpr).
   * 
   * @return a SyntaxNode representing the expression.
   * @throws ParseException when parsing fails.
   */
  private SyntaxNode evalMexpr() throws ParseException {
    SyntaxNode expr = null;
    SyntaxNode rterm = null;
    TokenType op;

    expr = getGoodParse(evalTerm());

    op = getCurrToken().getType(); // This should be an operator.
    while (checkMatch(TokenType.ADD) || checkMatch(TokenType.SUB)) {
      rterm = getGoodParse(evalTerm());
      expr = new BinOpNode(expr, op, rterm, getCurrLine());
      op = getCurrToken().getType(); // Save off the next operator(?).
    }

    return expr;
  }

  /**
   * Method to evaluate the term nonterminal.
   * 
   * @return the subtree representing the expression.
   * @throws ParseException when the parsing fails.
   */
  private SyntaxNode evalTerm() throws ParseException {
    SyntaxNode rfact;
    TokenType op;
    SyntaxNode term;

    trace("Enter <term>");

    // Handle unary not.
    if (checkMatch(TokenType.NOT)) {
      SyntaxNode expr = getGoodParse(evalRexpr());
      return new UnaryOpNode(expr, TokenType.NOT,
          getCurrLine());
    }

    term = getGoodParse(evalFactor());

    // Handle the higher level binary operations.
    op = getCurrToken().getType(); // Save off what we think is an operation
    while (checkMatch(TokenType.MULT) || checkMatch(TokenType.DIV)
        || checkMatch(TokenType.MOD)) {
      rfact = getGoodParse(evalFactor());
      term = new BinOpNode(term, op, rfact, getCurrLine());
      op = getCurrToken().getType();
    }
    trace("Exit <term>");
    return term;
  }

  /**
     * Method to evaluate the factor non-terminal (the tightest binding operations). 
     * @return the subtree resulting from the parse.
     * @throws ParseException when parsing fails.
     */
    private SyntaxNode evalFactor() throws ParseException {
        trace("Enter <factor>");
        SyntaxNode fact = null;

        // Do we have a unary sub (i.e., a negative).
        if (checkMatch(TokenType.SUB))
        {
            SyntaxNode expr = getGoodParse(evalFactor());
            return new UnaryOpNode(expr, TokenType.SUB, getCurrLine());
        }

    
        // Parenthisized expression
        else if (checkMatch(TokenType.LPAREN)) { 
            
            fact = getGoodParse(evalExpr());
            
            // Force the right paren.
            match(TokenType.RPAREN, ")");        
        } 
        
        // Handle the literals.
        else if (tokenIs(TokenType.INT) || tokenIs(TokenType.REAL) ||
                   tokenIs(TokenType.TRUE) || tokenIs(TokenType.FALSE)) {
                fact = new TokenNode(getCurrToken(), getCurrLine());
                nextToken();        // advance the token stream.
                return fact;
        }

        // Handle an identifer.
        else if (tokenIs(TokenType.ID)) {
            Token ident = getCurrToken(); // Store off the next token.
            nextToken();    // advance the token stream.

            // Just a run of the mill token.
            fact = new TokenNode(ident, getCurrLine());

        }
            
        trace("Exit <factor>");
        return fact;
    }


  /***********
   *
   * Methods for handling a specific rule of a non-terminal
   * 
   ***********/

  /**
   * This method handles a value definition. <id> := <expr>
   * 
   * @return a global value node.
   * @throws ParseException when this is not a valid value.
   */
  private SyntaxNode handleValues() throws ParseException {
    Token id = getCurrToken();
    SyntaxNode expr;

    match(TokenType.ID, "identifier");
    match(TokenType.ASSIGN, ":=");
    expr = evalExpr();
    return new ValNode(id, expr, getCurrLine());
  }

}
