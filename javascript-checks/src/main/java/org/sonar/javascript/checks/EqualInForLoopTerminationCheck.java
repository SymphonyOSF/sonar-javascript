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

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.javascript.tree.TreeKinds;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.tree.declaration.BindingElementTree;
import org.sonar.plugins.javascript.api.tree.declaration.InitializedBindingElementTree;
import org.sonar.plugins.javascript.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.ExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.IdentifierTree;
import org.sonar.plugins.javascript.api.tree.expression.LiteralTree;
import org.sonar.plugins.javascript.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.javascript.api.tree.statement.ForStatementTree;
import org.sonar.plugins.javascript.api.tree.statement.VariableDeclarationTree;
import org.sonar.plugins.javascript.api.visitors.DoubleDispatchVisitorCheck;

@Rule(key = "S888")
public class EqualInForLoopTerminationCheck extends DoubleDispatchVisitorCheck {

  private static final String MESSAGE = "Replace '%s' operator with one of '<=', '>=', '<', or '>' comparison operators.";

  private static final Kind[] INCREMENT_ASSIGNMENT = {
    Tree.Kind.PLUS_ASSIGNMENT,
    Tree.Kind.MINUS_ASSIGNMENT
  };

  @Override
  public void visitForStatement(ForStatementTree tree) {
    ExpressionTree condition = tree.condition();
    ExpressionTree update = tree.update();

    boolean conditionCondition = condition != null && isEquality(condition);
    boolean updateCondition = update != null && isUpdateIncDec(update);

    if (conditionCondition && updateCondition && !isException(tree)) {
      addIssue((BinaryExpressionTree) condition);
    }

    super.visitForStatement(tree);
  }

  private static void counters(ExpressionTree update, Set<String> counters) {
    ExpressionTree counter = null;
    if (update.is(Tree.Kind.COMMA_OPERATOR)) {
      BinaryExpressionTree commaExpressions = (BinaryExpressionTree) update;
      counters(commaExpressions.leftOperand(), counters);
      counters(commaExpressions.rightOperand(), counters);

    } else if (TreeKinds.isIncrementOrDecrement(update)) {
      counter = ((UnaryExpressionTree) update).expression();

    } else if (update.is(Kind.PLUS_ASSIGNMENT, Kind.MINUS_ASSIGNMENT)) {
      counter = ((AssignmentExpressionTree) update).variable();
    }

    if (counter != null && counter.is(Kind.IDENTIFIER_REFERENCE)) {
      counters.add(((IdentifierTree) counter).name());
    }
  }

  private void addIssue(BinaryExpressionTree condition) {
    String message = String.format(MESSAGE, condition.operator().text());
    addIssue(condition.operator(), message);
  }

  private static boolean isEquality(ExpressionTree condition) {
    return condition.is(Tree.Kind.EQUAL_TO, Tree.Kind.NOT_EQUAL_TO);
  }

  private static boolean isException(ForStatementTree forStatement) {
    return isNontrivialConditionException(forStatement) || isTrivialIteratorException(forStatement);
  }

  private static boolean isTrivialIteratorException(ForStatementTree forStatement) {
    // todo(Lena): SONARJS-383 consider usage of counter inside the loop. Do it with symbol table.
    ExpressionTree condition = forStatement.condition();
    if (condition != null && condition.is(Tree.Kind.NOT_EQUAL_TO)) {
      ExpressionTree update = forStatement.update();
      Tree init = forStatement.init();
      if (init != null && update != null) {
        return checkForTrivialIteratorException(init, condition, update);
      }
    }
    return false;
  }

  private static boolean checkForTrivialIteratorException(Tree init, ExpressionTree condition, ExpressionTree update) {
    int updateByOne = checkForUpdateByOne(update);
    if (updateByOne != 0) {
      Integer endValue = getValue(condition);
      Integer beginValue = getValue(init);
      if (endValue != null && beginValue != null && updateByOne == Integer.signum(endValue - beginValue)) {
        return true;
      }
    }
    return false;
  }

  // returns true is condition in forStatement is not just comparing counter with something
  private static boolean isNontrivialConditionException(ForStatementTree forStatement) {
    ExpressionTree condition = forStatement.condition();
    ExpressionTree update = forStatement.update();
    if (update != null && condition != null && condition.is(Kind.EQUAL_TO, Kind.NOT_EQUAL_TO, Kind.STRICT_EQUAL_TO, Kind.STRICT_NOT_EQUAL_TO)) {
      Set<String> counters = new HashSet<>();
      counters(update, counters);
      ExpressionTree leftOperand = ((BinaryExpressionTree) condition).leftOperand();
      return !leftOperand.is(Kind.IDENTIFIER_REFERENCE) || !counters.contains(((IdentifierTree) leftOperand).name());
    }
    return false;
  }

  @Nullable
  private static Integer getValue(Tree tree) {
    Integer result = null;
    if (tree.is(Tree.Kind.NOT_EQUAL_TO)) {
      result = getInteger(((BinaryExpressionTree) tree).rightOperand());
    } else if (isOneVarDeclaration(tree)) {
      BindingElementTree variable = ((VariableDeclarationTree) tree).variables().get(0);
      if (variable.is(Tree.Kind.INITIALIZED_BINDING_ELEMENT)) {
        result = getInteger(((InitializedBindingElementTree) variable).right());
      }
    } else if (tree.is(Tree.Kind.ASSIGNMENT)) {
      result = getInteger(((AssignmentExpressionTree) tree).expression());
    }
    return result;
  }

  private static boolean isOneVarDeclaration(Tree tree) {
    return tree.is(Tree.Kind.VAR_DECLARATION) && ((VariableDeclarationTree) tree).variables().size() == 1;
  }

  private static Integer getInteger(ExpressionTree expression) {
    if (expression.is(Tree.Kind.NUMERIC_LITERAL)) {
      LiteralTree literal = (LiteralTree) expression;
      Integer decoded;
      try {
        decoded = Integer.decode(literal.value());
        return decoded;
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  private static int checkForUpdateByOne(ExpressionTree update) {
    if (update.is(Tree.Kind.POSTFIX_INCREMENT, Tree.Kind.PREFIX_INCREMENT) || (update.is(Tree.Kind.PLUS_ASSIGNMENT) && isUpdateOnOneWithAssign(update))) {
      return +1;
    }
    if (update.is(Tree.Kind.POSTFIX_DECREMENT, Tree.Kind.PREFIX_DECREMENT) || (update.is(Tree.Kind.MINUS_ASSIGNMENT) && isUpdateOnOneWithAssign(update))) {
      return -1;
    }
    return 0;
  }

  private static boolean isUpdateIncDec(ExpressionTree update) {
    boolean result = false;
    if (update.is(Tree.Kind.COMMA_OPERATOR)) {
      BinaryExpressionTree commaExpressions = (BinaryExpressionTree) update;
      result = isUpdateIncDec(commaExpressions.leftOperand()) && isUpdateIncDec(commaExpressions.rightOperand());

    } else if (TreeKinds.isIncrementOrDecrement(update) || update.is(INCREMENT_ASSIGNMENT)) {
      result = true;
    }

    return result;
  }

  private static boolean isUpdateOnOneWithAssign(ExpressionTree update) {
    if (update.is(Tree.Kind.PLUS_ASSIGNMENT, Tree.Kind.MINUS_ASSIGNMENT)) {
      ExpressionTree rightExpression = ((AssignmentExpressionTree) update).expression();
      return rightExpression.is(Tree.Kind.NUMERIC_LITERAL) && "1".equals(((LiteralTree) rightExpression).value());
    }
    return false;
  }
}
