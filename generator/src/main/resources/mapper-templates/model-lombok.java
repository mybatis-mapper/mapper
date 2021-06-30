package ${package};

import io.mybatis.provider.Entity;
import org.apache.ibatis.type.JdbcType;

import lombok.Getter;
import lombok.Setter;

<#list it.importJavaTypes as javaType>
import ${javaType};
</#list>

/**
 * ${it.name} - ${it.comment}
 *
 * @author ${SYS['user.name']}
 */
@Getter
@Setter
@Entity.Table("${it.name}" remark = "${it.comment}", autoResultMap = true)
public class ${it.name.className} {
  <#list it.columns as column>
  <#if column.pk>
  @Entity.Column(value = "${column.name}", id = true, remark = "${column.comment}", updatable = false, insertable = false)
  <#else>
  @Entity.Column(value = "${column.name}", remark = "${column.comment}"<#if column.tags.jdbcType>, jdbcType = JdbcType.${column.jdbcType}</#if>)
  </#if>
  private ${column.javaType} ${column.name.fieldName};

  </#list>
}
