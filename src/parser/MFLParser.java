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
import ast.nodes.LetNode; // The newly made LetNode class
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
 * Parser for the MFL language.
 */
public class MFLParser extends Parser {

  public MFLParser(File src) throws FileNotFoundException {
    super(new Lexer(src));
  }

  public MFLParser(String str) {
    super(new Lexer(str));
  }

  public SyntaxTree parse() throws ParseException {
    SyntaxTree ast;
    nextToken();                 // Get the first token.
    ast = new SyntaxTree(evalProg()); // Start at the root.
    match(TokenType.EOF, "EOF");
    return ast;
  }

  /************
   * Non-terminals
   ***********/
  /** <prog> -> <expr> { <expr> } */
  private SyntaxNode evalProg() throws ParseException {
    LinkedList<SyntaxNode> exprs = new LinkedList<>();

    trace("Enter <prog>");
    while (!checkMatch(TokenType.EOF)) {
      SyntaxNode currNode = evalValues();
      if (currNode == null)
        break;

      match(TokenType.SEMI, ";");   // require semicolon
      exprs.add(currNode);
    }

    if (exprs.size() == 0)
      return null;

    trace("Exit <prog>");
    return new ProgNode(exprs, super.getCurrLine());
  }

  /** <values> -> val <id> := <expr> | <expr> */
  private SyntaxNode evalValues() throws ParseException {
    if (checkMatch(TokenType.VAL))
      return getGoodParse(handleValues());
    else
      return getGoodParse(evalExpr());
  }

  /**
   * <expr> -> let <id> := <expr> in <expr> | <rexpr> { (and|or) <rexpr> }
   */
  private SyntaxNode evalExpr() throws ParseException {
    SyntaxNode rexpr;
    TokenType opTok;
    SyntaxNode expr;

    trace("Enter <expr>");

    // let <id> := <expr> in <expr>
    if (checkMatch(TokenType.LET)) {
      long line = getCurrLine();

      Token name = getCurrToken();
      match(TokenType.ID, "identifier");

      match(TokenType.ASSIGN, ":=");

      SyntaxNode valueExpr = getGoodParse(evalExpr());
      match(TokenType.IN, "in");
      SyntaxNode bodyExpr = getGoodParse(evalExpr());

      trace("Exit <expr> (let)");
      return new LetNode(name, valueExpr, bodyExpr, line);
    }

    // otherwise: <rexpr> { (and|or) <rexpr> }
    expr = getGoodParse(evalRexpr());
    opTok = getCurrToken().getType();

    while (checkMatch(TokenType.AND) || checkMatch(TokenType.OR)) {
      SyntaxNode rhs = getGoodParse(evalRexpr());
      String opStr = binOpToString(opTok); // map to "+"/"and"/etc.
      expr = new BinOpNode(opStr, expr, rhs, getCurrLine());
      opTok = getCurrToken().getType();
    }

    trace("Exit <expr>");
    return expr;
  }

  /** <rexpr> -> <mexpr> [ (< | <= | > | >= | = | !=) <mexpr> ] */
  private SyntaxNode evalRexpr() throws ParseException {
    SyntaxNode left, right;
    TokenType opTok;

    left = getGoodParse(evalMexpr());

    opTok = getCurrToken().getType();
    if (checkMatch(TokenType.LT) || checkMatch(TokenType.LTE)
        || checkMatch(TokenType.GT) || checkMatch(TokenType.GTE)
        || checkMatch(TokenType.EQ) || checkMatch(TokenType.NEQ)) {
      right = getGoodParse(evalMexpr());
      String opStr = relOpToString(opTok);
      return new RelOpNode(opStr, left, right, getCurrLine());
    }

    return left;
  }

  /** <mexpr> -> <term> { (+ | -) <term> } */
  private SyntaxNode evalMexpr() throws ParseException {
    SyntaxNode expr = getGoodParse(evalTerm());
    TokenType opTok = getCurrToken().getType();

    while (checkMatch(TokenType.ADD) || checkMatch(TokenType.SUB)) {
      SyntaxNode rterm = getGoodParse(evalTerm());
      String opStr = binOpToString(opTok);
      expr = new BinOpNode(opStr, expr, rterm, getCurrLine());
      opTok = getCurrToken().getType();
    }
    return expr;
  }

  /** <term> -> [not] <factor> { (* | / | mod) <factor> } */
  private SyntaxNode evalTerm() throws ParseException {
    trace("Enter <term>");

    // unary not
    if (checkMatch(TokenType.NOT)) {
      SyntaxNode expr = getGoodParse(evalRexpr()); // matches your original structure
      return new UnaryOpNode("not", expr, getCurrLine());
    }

    SyntaxNode term = getGoodParse(evalFactor());

    // { * | / | mod } <factor>
    TokenType opTok = getCurrToken().getType();
    while (checkMatch(TokenType.MULT) || checkMatch(TokenType.DIV)
        || checkMatch(TokenType.MOD)) {
      SyntaxNode rfact = getGoodParse(evalFactor());
      String opStr = binOpToString(opTok);
      term = new BinOpNode(opStr, term, rfact, getCurrLine());
      opTok = getCurrToken().getType();
    }

    trace("Exit <term>");
    return term;
  }

  /** <factor> -> - <factor> | ( <expr> ) | INT | REAL | TRUE | FALSE | ID */
  private SyntaxNode evalFactor() throws ParseException {
    trace("Enter <factor>");
    SyntaxNode fact = null;

    // unary minus
    if (checkMatch(TokenType.SUB)) {
      SyntaxNode expr = getGoodParse(evalFactor());
      return new UnaryOpNode("-", expr, getCurrLine());
    }

    // ( <expr> )
    else if (checkMatch(TokenType.LPAREN)) {
      fact = getGoodParse(evalExpr());
      match(TokenType.RPAREN, ")");
    }

    // literals
    else if (tokenIs(TokenType.INT) || tokenIs(TokenType.REAL)
        || tokenIs(TokenType.TRUE) || tokenIs(TokenType.FALSE)) {
      fact = new TokenNode(getCurrToken(), getCurrLine());
      nextToken();
      return fact;
    }

    // identifier
    else if (tokenIs(TokenType.ID)) {
      Token ident = getCurrToken();
      nextToken();
      fact = new TokenNode(ident, getCurrLine());
    }

    trace("Exit <factor>");
    return fact;
  }

  /***********
   * Helpers for rules / utilities
   ***********/
  /** val <id> := <expr> */
  private SyntaxNode handleValues() throws ParseException {
    Token id = getCurrToken();
    match(TokenType.ID, "identifier");
    match(TokenType.ASSIGN, ":=");
    SyntaxNode expr = evalExpr();
    return new ValNode(id, expr, getCurrLine());
  }

  // --- NEW: map TokenType -> String for nodes that want string operators ---

  private static String binOpToString(TokenType t) {
    switch (t) {
      case ADD:  return "+";
      case SUB:  return "-";
      case MULT: return "*";
      case DIV:  return "/";
      case MOD:  return "mod";
      case AND:  return "and";
      case OR:   return "or";
      default:
        throw new IllegalArgumentException("Unexpected binary operator token: " + t);
    }
  }

  private static String relOpToString(TokenType t) {
    switch (t) {
      case LT:  return "<";
      case GT:  return ">";
      case LTE: return "<=";
      case GTE: return ">=";
      case EQ:  return "=";
      case NEQ: return "!=";
      default:
        throw new IllegalArgumentException("Unexpected relational operator token: " + t);
    }
  }
}
