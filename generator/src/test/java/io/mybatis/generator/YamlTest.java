/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mybatis.generator;

import io.mybatis.generator.project.Project;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

public class YamlTest {

  public static void main(String[] args) throws IOException {
    Yaml yaml = new Yaml(new Constructor(Project.class));
    InputStream inputStream = YamlTest.class.getResourceAsStream("/project.yaml");
    Project project = yaml.load(inputStream);
    System.out.println(project.getName());
    inputStream.close();
  }

}
