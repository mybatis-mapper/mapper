package io.mybatis.mapper.logical;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记该字段为逻辑状态列
 * <p>NOTE: 单张表中仅支持标记一个字段。并且，该字段最好是满足@Entity.Column(updatable = false, insertable = false)，这样可以避免其他更新方法误更新逻辑删除状态</p>
 *
 * @author hzw
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LogicalColumn {
  /**
   * 表示逻辑删除的值，比如null、0
   */
  String delete();
}
