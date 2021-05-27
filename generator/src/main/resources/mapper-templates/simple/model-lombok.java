package ${package};

import io.mybatis.provider.Entity;

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
@Entity.Table("${it.name}")
public class ${it.name.className} {
  <#list it.columns as column>
  /**
   * ${column.comment}
   */
  <#if column.pk>
  @Entity.Column(value = "${column.name}", id = true)
  <#else>
  @Entity.Column("${column.name}")
  </#if>
  private ${column.javaType} ${column.name.fieldName};

  </#list>
}
