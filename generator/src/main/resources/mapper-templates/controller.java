package ${package};

import io.mybatis.service.BaseService;

import ${project.attrs.basePackage}.model.${it.name.className};
import ${project.attrs.basePackage}.service.${it.name.className}Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * ${it.name} - ${it.comment}
 *
 * @author ${SYS['user.name']}
 */
@RestController
@RequestMapping("${it.name.fieldName.s}")
public class ${it.name.className}Controller {

  @Autowired
  private ${it.name.className}Service ${it.name.fieldName}Service;

  @RequestMapping(method = RequestMethod.GET)
  public List<${it.name.className}> ${it.name.fieldName.s}(${it.name.className} ${it.name.fieldName}) {
    return null;
  }

}
