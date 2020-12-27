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
package io.mybatis.activerecord.spring;

import io.mybatis.mapper.Mapper;
import io.mybatis.mapper.example.Example;
import org.apache.ibatis.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class ActiveRecordTest {
  protected static ClassPathXmlApplicationContext context;

  @BeforeClass
  public static void init() {
    LogFactory.useLog4JLogging();
    context = new ClassPathXmlApplicationContext("classpath:io/mybatis/activerecord/spring/spring.xml");
  }

  @AfterClass
  public static void close() {
    context.close();
  }

  @Test
  public void shouldReturnMapper() {
    RoleMapper roleMapper = (RoleMapper) context.getBean("roleMapper");
    Assert.assertNotNull(roleMapper);
    Mapper<Role, Integer> roleMapper2 = new Role().baseMapper();
    Assert.assertNotNull(roleMapper2);
    Assert.assertTrue(roleMapper2 instanceof RoleMapper);

    UserMapper userMapper = (UserMapper) context.getBean("userMapper");
    Assert.assertNotNull(userMapper);
    Mapper<User, Integer> userMapper2 = new User().baseMapper();
    Assert.assertNotNull(userMapper2);
    Assert.assertTrue((Object) userMapper2 instanceof UserMapper);
  }

  @Test
  public void testRole() {
    Role role = new Role();
    role.save();
    Assert.assertEquals(1, role.count());
    Assert.assertNotNull(role.getId());
    Assert.assertNotNull(role.pkValue());
    Assert.assertTrue(role.pkHasValue());
    role.setId(null);
    Assert.assertEquals(3, role.count());
  }

  @Test
  public void testUser() {
    User user = new User();
    Assert.assertEquals(2, user.findAll().size());
    user.save();
    Assert.assertNotNull(user.getId());
    Assert.assertTrue(user.pkHasValue());
    Assert.assertNotNull(user.pkValue());
    Assert.assertEquals(3, new User().count());
    user.setName("test");
    user.save();
    Assert.assertEquals(4, new User().count());
    user.setName("test2");
    user.saveOrUpdate();
    Assert.assertEquals(4, new User().count());
    user.setName("test3");
    user.saveOrUpdateSelective();
    user.deleteById();
    Assert.assertEquals(3, new User().count());
    //数据已经删除，更新会失败
    int err = 0;
    try {
      user.saveOrUpdateSelective();
    } catch (Exception e) {
      err++;
    }
    Assert.assertEquals(1, err);
    Assert.assertEquals(3, new User().count());

    user = new User();
    user.setRoleId(2);
    user.saveSelective();
    Assert.assertNotNull(user.getId());
    user = user.findById(user.getId()).get();
    Assert.assertNotNull(user.getName());

    user.setName("develop");
    user.setRoleId(null);
    user.updateSelective();
    user = user.findById(user.getId()).get();
    Assert.assertEquals("develop", user.getName());
    Assert.assertNotNull(user.getRoleId());

    user.setRoleId(null);
    user.update();
    user = user.findById(user.getId()).get();
    Assert.assertNull(user.getRoleId());

    user.setName(null);
    user.updateSelective(User::getName, User::getRoleId);
    user = user.findById(user.getId()).get();
    Assert.assertNull(user.getName());

    user.deleteById();
    Assert.assertFalse(user.findById(user.getId()).isPresent());

    user.setName("delete");
    user.setId(null);
    user.save();
    user.setId(null);
    user.save();
    user.setId(null);
    Assert.assertEquals(2, user.delete());
    user.save();
    user.deleteById(user.getId());

    List<String> names = Arrays.asList("test1", "test2", "test3");
    names.forEach(name -> {
      User u = new User();
      u.setName(name);
      u.setId(null);
      u.save();
    });
    Assert.assertEquals(3, user.findByFieldList(User::getName, names).size());
    Assert.assertEquals(3, user.deleteByFieldList(User::getName, names));

    names = Arrays.asList("test", "test", "test");
    names.forEach(name -> {
      User u = new User();
      u.setName(name);
      u.setId(null);
      u.save();
    });
    Assert.assertEquals(6, user.findAll().size());
    user = new User();
    user.setName(names.get(0));
    Assert.assertEquals(3, user.count());
    Assert.assertEquals(3, user.findList().size());
    user.setName("admin");
    Assert.assertTrue(user.findOne().isPresent());
  }

  @Test
  public void testUserExample() {
    User user = new User();
    Example<User> example = user.example();
    example.createCriteria().andEqualTo(User::getId, 2);
    Assert.assertEquals(1, user.delete(example));
    String name = "example";
    int count = 5;
    user = new User(name);
    //新增5个
    for (int i = 0; i < count; i++) {
      user.save();
    }
    user = new User(name);
    example.clear();
    example.createCriteria().andEqualTo(User::getName, name);
    List<User> users = user.findList(example);
    Assert.assertEquals(count, users.size());
    Assert.assertEquals(count, user.count(example));
    for (User u : users) {
      Assert.assertNotNull(u.getName());
      Assert.assertNull(u.getRoleId());
    }
    user.setRoleId(10);
    user.setName(null);
    Assert.assertEquals(count, user.update(example));
    example.clear();
    example.createCriteria().andEqualTo(User::getRoleId, 10);
    users = user.findList(example);
    Assert.assertEquals(count, users.size());
    for (User u : users) {
      Assert.assertNull(u.getName());
      Assert.assertNotNull(u.getRoleId());
    }
    user.setName(name);
    Assert.assertEquals(count, user.update(example));
    user.setName(null);
    Assert.assertEquals(count, user.updateSelective(example));
    users = user.findList(example);
    Assert.assertEquals(count, users.size());
    for (User u : users) {
      Assert.assertNotNull(u.getName());
      Assert.assertNotNull(u.getRoleId());
    }
    example.clear();
    example.createCriteria().andEqualTo(User::getName, "admin");
    Optional<User> userOptional = user.findOne(example);
    Assert.assertTrue(userOptional.isPresent());
    Assert.assertEquals(1, userOptional.get().getId().intValue());
  }

}
