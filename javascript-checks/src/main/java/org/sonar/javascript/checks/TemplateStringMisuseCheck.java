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

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.tree.expression.LiteralTree;
import org.sonar.plugins.javascript.api.visitors.SubscriptionVisitorCheck;

@Rule(key = "S3786")
public class TemplateStringMisuseCheck extends SubscriptionVisitorCheck {

  private static final String MESSAGE = "Replace the quotes (%s) with back-ticks (`).";

  @Override
  public List<Kind> nodesToVisit() {
    return ImmutableList.of(Kind.STRING_LITERAL);
  }

  @Override
  public void visitNode(Tree tree) {
    LiteralTree stringLiteral = (LiteralTree) tree;
    String value = stringLiteral.value();
    int startIndex = value.indexOf("${");
    int endIndex = value.indexOf('}');

    if (startIndex != -1 && startIndex < endIndex) {
      addIssue(tree, String.format(MESSAGE, value.charAt(0)));
    }

  }
}
