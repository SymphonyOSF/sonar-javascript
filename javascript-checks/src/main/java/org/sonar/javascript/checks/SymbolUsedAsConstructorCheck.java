/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.javascript.checks;

import org.sonar.check.Rule;
import org.sonar.javascript.checks.utils.CheckUtils;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.tree.expression.ExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.IdentifierTree;
import org.sonar.plugins.javascript.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.javascript.api.visitors.DoubleDispatchVisitorCheck;

@Rule(key = "S3834")
public class SymbolUsedAsConstructorCheck extends DoubleDispatchVisitorCheck {

  private static final String MESSAGE = "Remove this \"new\" operator.";

  private static final String SYMBOL = "Symbol";

  @Override
  public void visitNewExpression(NewExpressionTree tree) {
    if (isSymbol(tree.expression())) {
      raiseError(tree);
    }
  }

  /**
   * Returns true if the expression is the Symbol built-in, else returns false.
   * Specifically, returns false if the expression is "Symbol" but the built-in Symbol has been shadowed.
   */
  private boolean isSymbol(ExpressionTree expression) {
    if (expression.is(Kind.IDENTIFIER_REFERENCE)) {
      String name = ((IdentifierTree) expression).name();
      if (SYMBOL.equals(name) && !isSymbolShadowed(expression)) {
        return true;
      }
    }
    return false;
  }

  private boolean isSymbolShadowed(Tree tree) {
    Tree scopedTree = CheckUtils.getFirstAncestor(
      tree,
      Kind.FUNCTION_DECLARATION,
      Kind.FUNCTION_EXPRESSION,
      Kind.METHOD,
      Kind.GENERATOR_METHOD,
      Kind.GENERATOR_FUNCTION_EXPRESSION,
      Kind.GENERATOR_DECLARATION,
      Kind.GET_METHOD,
      Kind.SET_METHOD,
      Kind.ARROW_FUNCTION,
      Kind.SCRIPT);
    return getContext().getSymbolModel().getScope(scopedTree).lookupSymbol(SYMBOL) != null;
  }

  private void raiseError(NewExpressionTree tree) {
    addIssue(tree.newKeyword(), MESSAGE).secondary(tree.expression());
  }

}
