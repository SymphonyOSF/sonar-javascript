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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sonar.check.Rule;
import org.sonar.javascript.checks.utils.CheckUtils;
import org.sonar.javascript.tree.impl.expression.SuperTreeImpl;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.tree.declaration.FunctionTree;
import org.sonar.plugins.javascript.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.javascript.api.tree.expression.ClassTree;
import org.sonar.plugins.javascript.api.tree.expression.IdentifierTree;
import org.sonar.plugins.javascript.api.visitors.DoubleDispatchVisitor;
import org.sonar.plugins.javascript.api.visitors.DoubleDispatchVisitorCheck;

@Rule(key = "S3854")
public class SuperInvocationCheck extends DoubleDispatchVisitorCheck {

  private static final String MESSAGE_SUPER_ONLY_IN_DERIVED_CLASS_CONSTRUCTOR = "super() can only be invoked in a derived class constructor.";

  private static final String MESSAGE_SUPER_REQUIRED_IN_ANY_DERIVED_CLASS_CONSTRUCTOR = "super() must be invoked in any derived class constructor.";

  @Override
  public void visitSuper(SuperTreeImpl tree) {
    System.out.println("visitSuper " + tree + " in " + CheckUtils.parent(tree));
    checkSuperCanOnlyBeInvokedInDerivedClassConstructor(tree);
    super.visitSuper(tree);
  }
  
  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    checkSuperMustBeInvokedInAnyDerivedClassConstructor(tree);
    super.visitMethodDeclaration(tree);
  }
  
  private void checkSuperCanOnlyBeInvokedInDerivedClassConstructor(SuperTreeImpl superTree) {
    if (superTree.getParent().is(Kind.CALL_EXPRESSION) &&
      (!isInConstructor(superTree) || isInBaseClass(getEnclosingConstructor(superTree)))) {
      addIssue(superTree, MESSAGE_SUPER_ONLY_IN_DERIVED_CLASS_CONSTRUCTOR);
    }
  }

  private void checkSuperMustBeInvokedInAnyDerivedClassConstructor(MethodDeclarationTree method) {
    if (isConstructor(method) && !isInBaseClass(method)) {
      boolean foundSuperInvocation = false;
      Set<SuperTreeImpl> superTrees = new SuperDetector().detectIn(method);
      for (SuperTreeImpl superTree : superTrees) {
        if (superTree.getParent().is(Kind.CALL_EXPRESSION)) {
          foundSuperInvocation = true;
          break;
        }
      }
      if (!foundSuperInvocation) {
        addIssue(method.name(), MESSAGE_SUPER_REQUIRED_IN_ANY_DERIVED_CLASS_CONSTRUCTOR);        
      }
    }
  }

  private boolean isInConstructor(Tree tree) {
    return getEnclosingConstructor(tree) != null;
  }

  private boolean isInBaseClass(Tree tree) {
    ClassTree classTree = getEnclosingClass(tree);
    return classTree.extendsToken() == null;
  }

  /**
   * Gets the constructor() which encloses the specified tree.
   * Returns null if the specified tree is not enclosed in a constructor.
   */
  private MethodDeclarationTree getEnclosingConstructor(Tree tree) {
    Tree function = CheckUtils.getFirstAncestor(tree, Kind.METHOD, Kind.FUNCTION_DECLARATION, Kind.FUNCTION_EXPRESSION);
    if (function != null && isConstructor(function)) {
      return (MethodDeclarationTree) function;
    }
//    if (function != null && function.is(Kind.METHOD)) {
//      MethodDeclarationTree constructor = (MethodDeclarationTree) function;
//      Tree nameTree = constructor.name();
//      if (nameTree.is(Kind.IDENTIFIER_NAME)) {
//        String name = ((IdentifierTree) nameTree).name();
//        if ("constructor".equals(name)) {
//          return constructor;
//        }
//      }
//    }
    return null;
  }

  private boolean isConstructor(Tree tree) {
    if (tree.is(Kind.METHOD)) {
      MethodDeclarationTree constructor = (MethodDeclarationTree) tree;
      Tree nameTree = constructor.name();
      if (nameTree.is(Kind.IDENTIFIER_NAME)) {
        String name = ((IdentifierTree) nameTree).name();
        if ("constructor".equals(name)) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Gets the class which encloses the specified tree.
   * The specified tree is assumed to be enclosed within a constructor.
   */
  private ClassTree getEnclosingClass(Tree tree) {
    return (ClassTree) CheckUtils.getFirstAncestor(tree, Kind.CLASS_DECLARATION, Kind.CLASS_EXPRESSION);
  }
  
  /**
   * An object to find the occurrences of "super" in a function.
   * @author yvesdubois-pelerin
   *
   */
  private static class SuperDetector extends DoubleDispatchVisitor {
    
    private Set<SuperTreeImpl> superTrees = new HashSet<>();
    
    public Set<SuperTreeImpl> detectIn(FunctionTree tree) {
      superTrees.clear();
      scan(tree);
      return superTrees;
    }
    
    @Override
    public void visitSuper(SuperTreeImpl tree) {
      superTrees.add(tree);
    }
    
  }

}
