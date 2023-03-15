drop table user if exists;
drop table user_ids if exists;
drop table user_auto if exists;

create table user
(
  id   INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,
  name VARCHAR(32) DEFAULT 'DEFAULT',
  sex  VARCHAR(2),
  status INTEGER
);

insert into user(id, name, sex, status)
values (1, '张无忌', '男', 1),
       (2, '赵敏', '女', 1),
       (3, '周芷若', '女', 1),
       (4, '小昭', '女', 1),
       (5, '殷离', '女', 1),
       (6, '张翠山', '男', 1),
       (7, '殷素素', '女', 1),
       (8, '金毛狮王', '男', 1),
       (9, '张三丰', '男', 1),
       (10, '宋远桥', '男', 1),
       (11, '俞莲舟', '男', 1),
       (12, '俞岱岩', '男', 1),
       (13, '张松溪', '男', 1),
       (14, '殷梨亭', '男', 1),
       (15, '莫声谷', '男', 1),
       (16, '纪晓芙', '女', 1),
       (17, '成昆', '男', 1),
       (18, '杨逍', '男', 1),
       (19, '范遥', '男', 1),
       (20, '殷天正', '男', 1),
       (21, '殷野王', '男', 1),
       (22, '黛绮丝', '女', 1),
       (23, '灭绝师太', '女', 1),
       (24, '韦一笑', '男', 1),
       (25, '周颠', '男', 1),
       (26, '说不得', '男', 1),
       (27, '谦卑', '男', 1),
       (28, '彭莹玉', '男', 1),
       (29, '常遇春', '男', 1),
       (30, '胡青牛', '男', 1),
       (31, '王难姑', '女', 1),
       (32, '朱元璋', '男', 1),
       (33, '杨不悔', '女', 1),
       (34, '鹿杖客', '男', 1),
       (35, '鹤笔翁', '男', 1),
       (36, '丁敏君', '女', 1),
       (37, '宋青书', '男', 1),
       (38, '何太冲', '男', 1),
       (39, '朱长龄', '男', 1),
       (40, '朱九真', '女', 1),
       (41, '武青婴', '女', 1),
       (42, '卫璧', '男', 1),
       (43, '汝阳王', '男', 1),
       (44, '王保保', '男', 1),
       (45, '觉远', '男', 1),
       (46, '郭襄', '女', 1),
       (47, '张君宝', '男', 1),
       (48, '何足道', '男', 1),
       (49, '都大锦', '男', 1),
       (50, '韩姬', '女', 1),
       (51, '黄衫女子', '女', null),
       (52, '陈友谅', '男', 1),
       (53, '韩千叶', '男', 1);

-- 联合主键
create table user_ids
(
  id1  INTEGER,
  id2  INTEGER,
  name VARCHAR(32) DEFAULT 'DEFAULT',
  PRIMARY KEY (id1, id2)
);

insert into user_ids(id1, id2, name)
values (1, 1, '张无忌1'),
       (1, 2, '张无忌2'),
       (1, 3, '张无忌3'),
       (1, 4, '张无忌4');

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
