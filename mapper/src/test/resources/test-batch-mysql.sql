
drop table if exists user_batch;
create table user_batch
(
  id  bigint,
  name VARCHAR(32) DEFAULT 'DEFAULT',
  PRIMARY KEY (id)
);

insert into user_batch(id,  name)
values ( 1, '张无忌1'),
       ( 2, '张无忌2'),
       ( 3, '张无忌3'),
       ( 4, '张无忌4');
