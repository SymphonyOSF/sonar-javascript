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
import org.sonar.javascript.tree.impl.expression.SuperTreeImpl;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.visitors.DoubleDispatchVisitorCheck;

@Rule(key = "S3854")
public class SuperInvocationCheck extends DoubleDispatchVisitorCheck {

  private static final String MESSAGE_SUPER_ONLY_IN_DERIVED_CLASS_CONSTRUCTOR = "super() can only be invoked in a derived class constructor.";

  @Override
  public void visitSuper(SuperTreeImpl tree) {
    System.out.println("visitSuper " + tree + " in " + CheckUtils.parent(tree));
    checkSuperCanOnlyBeInvokedInDerivedClassConstructor(tree);
    super.visitSuper(tree);
  }

  private void checkSuperCanOnlyBeInvokedInDerivedClassConstructor(SuperTreeImpl superTree) {
    boolean mustRaiseIssue = true;
    
    if (superTree.getParent().is(Kind.CALL_EXPRESSION) && isInConstructor(superTree) && isInDerivedClass(superTree)) {
      mustRaiseIssue = false;
    }
    
    if (mustRaiseIssue) {
      addIssue(superTree, MESSAGE_SUPER_ONLY_IN_DERIVED_CLASS_CONSTRUCTOR);
    }
  }
  
  private boolean isInDerivedClass(SuperTreeImpl superTree) {
    return true;
  }

  private boolean isInConstructor(Tree tree) {
    return true;
  }



}
