package ${package};

import io.mybatis.provider.Entity;

<#list it.importJavaTypes as javaType>
import ${javaType};
</#list>

/**
 * ${it.name} - ${it.comment}
 *
 * @author ${SYS['user.name']}
 */
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

  <#list it.columns as column>
  /**
   * 获取 ${column.comment}
   *
   * @return ${column.name.fieldName} - ${column.comment}
   */
  public ${column.javaType} get${column.name.className}() {
    return ${column.name.fieldName};
  }

  /**
   * 设置${column.comment}
   *
   * @param ${column.name.fieldName} ${column.comment}
   */
  public void set${column.name.className}(${column.javaType} ${column.name.fieldName}) {
    this.${column.name.fieldName} = ${column.name.fieldName};
  }

  </#list>
}
