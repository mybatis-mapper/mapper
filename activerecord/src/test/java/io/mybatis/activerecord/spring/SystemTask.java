package io.mybatis.activerecord.spring;

import java.util.Optional;

public class SystemTask {

  public void run() {
    Optional<User> user = new User().baseMapper().wrapper().eq(User::getId, 1).one();
    user.ifPresent(u -> System.out.println(u.getName()));
  }

}
