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
package io.mybatis.service;

import io.mybatis.mapper.example.Example;
import io.mybatis.service.model.Role;
import io.mybatis.service.model.User;
import org.apache.ibatis.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.List;


public class ServiceTest {
  protected static ClassPathXmlApplicationContext context;

  @BeforeClass
  public static void init() {
    LogFactory.useLog4JLogging();
    context = new ClassPathXmlApplicationContext("classpath:io/mybatis/service/spring.xml");
  }

  @AfterClass
  public static void close() {
    context.close();
  }

  @Test
  public void shouldReturnService() {
    RoleService roleService = (RoleService) context.getBean("roleServiceImpl");
    Assert.assertNotNull(roleService);

    UserService userService = (UserService) context.getBean("userServiceImpl");
    Assert.assertNotNull(userService);
  }

  @Test
  public void testRoleService() {
    RoleService roleService = context.getBean(RoleService.class);
    Role role = roleService.findById(1);
    Assert.assertNotNull(role);
    role = roleService.save(role);
    Assert.assertFalse(role.getId().equals(1));

    role.setName("develop");
    role = roleService.update(role);
    role = roleService.findById(role.getId());
    Assert.assertEquals("develop", role.getName());

    role.setName(null);
    role = roleService.updateSelective(role, Role::getName);
    role = roleService.findById(role.getId());
    Assert.assertNull(role.getName());

    Assert.assertEquals(1, roleService.deleteById(role.getId()));

    List<String> names = Arrays.asList("role1", "role2", "role3");
    for (String name : names) {
      Role r = new Role();
      r.setName(name);
      roleService.save(r);
    }
    Assert.assertEquals(names.size(), roleService.deleteByFieldList(Role::getName, names));
  }

  @Test
  public void testUserService() {
    UserService userService = context.getBean(UserService.class);
    Assert.assertEquals(2, userService.findAll().size());

    User user = new User();
    userService.save(user);
    Assert.assertNotNull(user.getId());

    Assert.assertEquals(3, userService.count((User) null));
    user.setName("test");
    userService.save(user);
    Assert.assertEquals(4, userService.count((User) null));
    user.setName("test2");
    userService.saveOrUpdate(user);
    Assert.assertEquals(4, userService.count((User) null));
    Assert.assertEquals(1, userService.deleteById(user.getId()));
    user.setId(null);
    userService.saveOrUpdate(user);
    Assert.assertEquals(4, userService.count((User) null));
    user.setName("test3");
    userService.saveOrUpdateSelective(user);
    Assert.assertEquals(4, userService.count((User) null));
    Assert.assertEquals(1, userService.deleteById(user.getId()));
    user.setId(null);
    userService.saveOrUpdateSelective(user);
    Assert.assertEquals(4, userService.count((User) null));
    Assert.assertEquals(1, userService.deleteById(user.getId()));
    Assert.assertEquals(3, userService.count((User) null));
    int err = 0;
    try {
      userService.saveOrUpdateSelective(user);
    } catch (Exception e) {
      err++;
    }
    Assert.assertEquals(1, err);

    user = new User();
    user.setRoleId(2);
    userService.saveSelective(user);
    Assert.assertNotNull(user.getId());
    user = userService.findById(user.getId());
    Assert.assertNotNull(user.getName());
    Assert.assertEquals("DEFAULT", user.getName());

    user.setName("develop");
    user.setRoleId(null);
    userService.updateSelective(user);
    user = userService.findById(user.getId());
    Assert.assertEquals("develop", user.getName());
    Assert.assertNotNull(user.getRoleId());

    user.setRoleId(null);
    userService.update(user);
    user = userService.findById(user.getId());
    Assert.assertNull(user.getRoleId());

    user.setName(null);
    userService.updateSelective(user, User::getName, User::getRoleId);
    user = userService.findById(user.getId());
    Assert.assertNull(user.getName());

    userService.deleteById(user.getId());
    Assert.assertFalse(userService.findById(user.getId()) != null);

    user.setName("delete");
    user.setId(null);
    userService.save(user);
    user.setId(null);
    userService.save(user);
    user.setId(null);
    Assert.assertEquals(2, userService.delete(user));
    userService.save(user);
    Assert.assertEquals(1, userService.deleteById(user.getId()));

    List<String> names = Arrays.asList("test1", "test2", "test3");
    names.forEach(name -> {
      User u = new User();
      u.setName(name);
      u.setId(null);
      userService.save(u);
    });
    Assert.assertEquals(3, userService.findByFieldList(User::getName, names).size());
    Assert.assertEquals(3, userService.deleteByFieldList(User::getName, names));

    names = Arrays.asList("test", "test", "test");
    names.forEach(name -> {
      User u = new User();
      u.setName(name);
      u.setId(null);
      userService.save(u);
    });
    Assert.assertEquals(6, userService.findAll().size());
    user = new User();
    user.setName(names.get(0));
    Assert.assertEquals(3, userService.count(user));
    Assert.assertEquals(3, userService.findList(user).size());
    user.setName("admin");
    Assert.assertTrue(userService.findOne(user) != null);
  }

  @Test
  public void testUserServiceExample() {
    UserService userService = context.getBean(UserService.class);
    Example<User> example = userService.example();
    example.createCriteria().andEqualTo(User::getId, 2);
    User user = userService.findOne(example);
    Assert.assertEquals(1, userService.delete(example));
    userService.save(user);
    String name = "example";
    int count = 5;
    user = new User(name);
    //新增5个
    for (int i = 0; i < count; i++) {
      userService.saveSelective(user);
    }
    user = new User(name);
    example.clear();
    example.createCriteria().andEqualTo(User::getName, name);
    List<User> users = userService.findList(example);
    Assert.assertEquals(count, users.size());
    Assert.assertEquals(count, userService.count(example));
    for (User u : users) {
      Assert.assertNotNull(u.getName());
      Assert.assertNull(u.getRoleId());
    }
    user.setRoleId(10);
    user.setName(null);
    Assert.assertEquals(count, userService.update(user, example));
    example.clear();
    example.createCriteria().andEqualTo(User::getRoleId, 10);
    users = userService.findList(example);
    Assert.assertEquals(count, users.size());
    for (User u : users) {
      Assert.assertNull(u.getName());
      Assert.assertNotNull(u.getRoleId());
    }
    user.setName(name);
    Assert.assertEquals(count, userService.update(user, example));
    user.setName(null);
    Assert.assertEquals(count, userService.updateSelective(user, example));
    users = userService.findList(example);
    Assert.assertEquals(count, users.size());
    for (User u : users) {
      Assert.assertNotNull(u.getName());
      Assert.assertNotNull(u.getRoleId());
    }

    example.clear();
    example.createCriteria().andEqualTo(User::getName, name);
    Assert.assertEquals(count, userService.delete(example));

    example.clear();
    example.createCriteria().andEqualTo(User::getName, "admin");
    user = userService.findOne(example);
    Assert.assertNotNull(user);
    Assert.assertEquals(1, user.getId().intValue());
  }

}
