drop table user if exists;
drop table user_auto if exists;

create table user
(
  id   INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,
  name VARCHAR(32) DEFAULT 'DEFAULT',
  sex  VARCHAR(2)
);


insert into user(id, name, sex)
values (1, '张无忌', '男'),
       (2, '赵敏', '女'),
       (3, '周芷若', '女'),
       (4, '小昭', '女'),
       (5, '殷离', '女');

-- 自动映射
create table user_auto
(
  id        INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,
  user_name VARCHAR(32) DEFAULT 'DEFAULT',
  address   VARCHAR(64)
);
insert into user_auto(id, user_name, address)
values (1, 'sjz', '河北省/石家庄市'),
       (2, 'hd', '河北省/邯郸市'),
       (3, 'xt', '河北省/邢台市');