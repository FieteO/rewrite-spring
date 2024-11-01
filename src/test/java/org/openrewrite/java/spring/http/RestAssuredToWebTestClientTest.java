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

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class RestAssuredToWebTestClientTest implements RewriteTest {

  @Override
  public void defaults(RecipeSpec spec) {
      spec
        .parser(JavaParser.fromJavaVersion()
          .classpathFromResources(new InMemoryExecutionContext(), "rest-assured-5.5", "spring-web-6", "spring-webflux-5", "spring-test-6"))
        .recipe(new RestAssuredToWebTestClient());
  }

  @Test
  void simple() {
      //language=java
      rewriteRun(
        java(
          """
            import io.restassured.RestAssured;

            class MyTest {

                void someTest() {
                    RestAssured.given();
                }
            }
            """,
          """
            import org.springframework.test.web.reactive.server.WebTestClient;

            class MyTest {

                void someTest() {
                    WebTestClient.bindToServer();
                }
            }
            """
        )
      );
  }
}
