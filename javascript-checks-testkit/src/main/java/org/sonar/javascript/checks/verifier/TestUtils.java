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
package org.sonar.javascript.checks.verifier;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.typed.ActionParser;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.sonar.api.config.Settings;
import org.sonar.javascript.parser.JavaScriptParserBuilder;
import org.sonar.javascript.tree.symbols.GlobalVariableNames;
import org.sonar.javascript.tree.symbols.type.JQuery;
import org.sonar.javascript.visitors.JavaScriptVisitorContext;
import org.sonar.plugins.javascript.api.tree.ScriptTree;
import org.sonar.plugins.javascript.api.tree.Tree;

class TestUtils {

  protected static final ActionParser<Tree> p = JavaScriptParserBuilder.createParser(Charsets.UTF_8);

  private TestUtils() {
  }

  public static JavaScriptVisitorContext createContext(File file) {
    ScriptTree scriptTree = (ScriptTree) p.parse(file);
    return new JavaScriptVisitorContext(scriptTree, file, settings());
  }

  private static Settings settings() {
    Settings settings = new Settings();

    Map<String, String> properties = new HashMap<>();
    properties.put(JQuery.JQUERY_OBJECT_ALIASES, JQuery.JQUERY_OBJECT_ALIASES_DEFAULT_VALUE);
    properties.put(GlobalVariableNames.ENVIRONMENTS_PROPERTY_KEY, GlobalVariableNames.ENVIRONMENTS_DEFAULT_VALUE);
    properties.put(GlobalVariableNames.GLOBALS_PROPERTY_KEY, GlobalVariableNames.GLOBALS_PROPERTY_KEY);
    settings.addProperties(properties);

    return settings;
  }

}
