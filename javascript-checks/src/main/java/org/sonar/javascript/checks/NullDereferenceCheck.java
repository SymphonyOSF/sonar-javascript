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
import org.sonar.javascript.se.Constraint;
import org.sonar.javascript.se.Nullability;
import org.sonar.javascript.se.ProgramState;
import org.sonar.javascript.se.SeCheck;
import org.sonar.javascript.tree.impl.JavaScriptTree;
import org.sonar.javascript.tree.symbols.Scope;
import org.sonar.plugins.javascript.api.symbols.Symbol;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.tree.expression.ExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.IdentifierTree;
import org.sonar.plugins.javascript.api.tree.expression.MemberExpressionTree;

@Rule(key = "S2259")
public class NullDereferenceCheck extends SeCheck {

  private static final String MESSAGE = "TypeError can be thrown as \"%s\" might be null or undefined here.";

  private Set<Symbol> hasIssue;

  @Override
  public void startOfExecution(Scope functionScope) {
    hasIssue = new HashSet<>();
  }

  @Override
  public void beforeBlockElement(ProgramState currentState, Tree element) {
    ExpressionTree object = getObject(element);
    Symbol symbol = getSymbol(object);

    if (symbol != null) {
      Constraint constraint = currentState.getConstraint(symbol);

      if (constraint != null && constraint.nullability() == Nullability.NULL && !hasIssue.contains(symbol)) {
        addIssue(object, String.format(MESSAGE, symbol.name()));
        hasIssue.add(symbol);
      }
    }
  }

  private static Symbol getSymbol(@Nullable ExpressionTree object) {
    if (object != null && object.is(Kind.IDENTIFIER_REFERENCE)) {
      return ((IdentifierTree) object).symbol();
    }
    return null;
  }

  @Nullable
  private static ExpressionTree getObject(Tree element) {
    if (element.is(Kind.BRACKET_MEMBER_EXPRESSION, Kind.DOT_MEMBER_EXPRESSION)) {
      return ((MemberExpressionTree) element).object();
    }
    if (element.is(Kind.IDENTIFIER_REFERENCE) && ((JavaScriptTree) element).getParent().is(Kind.FOR_OF_STATEMENT)) {
      return (ExpressionTree) element;
    }
    return null;
  }

}
