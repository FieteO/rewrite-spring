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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J.Literal;
import org.openrewrite.java.tree.J.MethodInvocation;
import org.openrewrite.java.tree.JavaType;

public class SimplifyWebTestClientCalls extends Recipe {

    @Override
    public String getDisplayName() {
        return "Simplify WebTestClient expressions";
    }

    @Override
    public String getDescription() {
        return "Simplifies various types of WebTestClient expressions to improve code readability.";
    }

    @Override
    public JavaIsoVisitor<ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {

            private final MethodMatcher methodMatcher
                    = new MethodMatcher("org.springframework.test.web.reactive.server.StatusAssertions isEqualTo(..)");
            private final JavaType JAVA_TYPE_INT = JavaType.buildType("int");
            private final JavaTemplate isOkTemplate
                    = JavaTemplate.builder("isOk()").build();

            @Override
            public MethodInvocation visitMethodInvocation(MethodInvocation method, ExecutionContext ctx) {
                if (!methodMatcher.matches(method.getMethodType())) {
                    return method;
                }
                Expression expression = method.getArguments().get(0);
                if (expression instanceof Literal) {
                    if (JAVA_TYPE_INT.equals(expression.getType())) {
                        Literal literal = (Literal) expression;
                        if (literal.getValue() instanceof Integer) {
                            if ((int) literal.getValue() == 200) {
                                // https://docs.openrewrite.org/concepts-explanations/javatemplate#usage
                                return isOkTemplate.apply(getCursor(), method.getCoordinates().replaceMethod());
                return super.visitMethodInvocation(method, ctx);
                        }
                        return method;
                    }
                    return method;
                }
                return super.visitMethodInvocation(method, p);
            }
        };
    }
}
