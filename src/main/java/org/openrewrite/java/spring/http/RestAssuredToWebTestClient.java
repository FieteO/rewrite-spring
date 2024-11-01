/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.spring.http;

import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.TreeVisitingPrinter;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.MethodInvocation;

public class RestAssuredToWebTestClient extends Recipe {

    private static final MethodMatcher ASSURED_GIVEN_MATCHER = new MethodMatcher("io.restassured.RestAssured given()");
    private static final String RESTASSURED_IMPORT = "io.restassured.RestAssured";
    private static final String WEBTESTCLIENT_IMPORT = "org.springframework.test.web.reactive.server.WebTestClient";

    @Override
    public String getDisplayName() {
        return "Migrate from REST-assured to WebTestClient";
    }

    @Override
    public String getDescription() {
        return "Migrate from REST-assured to WebTestClient.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
            new UsesType<>("io.restassured.RestAssured", false), new MigrateToWebTestClientVisitor());
    }

    private class MigrateToWebTestClientVisitor extends JavaIsoVisitor<ExecutionContext> {

        // @Override
        // public J.CompilationUnit visitCompilationUnit(J.CompilationUnit compUnit, ExecutionContext executionContext) {
        //     System.out.println(TreeVisitingPrinter.printTree(getCursor()));
        //     return super.visitCompilationUnit(compUnit, executionContext);
        // }

        @Override
        public MethodInvocation visitMethodInvocation(MethodInvocation method, ExecutionContext p) {
            if(!ASSURED_GIVEN_MATCHER.matches(method)) {
                return method;
            }
            maybeAddImport(WEBTESTCLIENT_IMPORT);
            maybeRemoveImport(RESTASSURED_IMPORT);
            return replaceMethod(p, method, "WebTestClient.bindToServer()");
        }

        private MethodInvocation replaceMethod(ExecutionContext ctx, MethodInvocation method, String methodName) {
            JavaTemplate template = JavaTemplate
                .builder(methodName)
                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx,"spring-webflux-5", "spring-test-6"))
                .imports(WEBTESTCLIENT_IMPORT)
                .build();
            return template.apply(getCursor(), method.getCoordinates().replace());
        }
    }
}
