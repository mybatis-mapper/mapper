package ${package};

import io.mybatis.service.AbstractService;

import ${project.attrs.basePackage}.service.${it.name.className}Service;
import ${project.attrs.basePackage}.mapper.${it.name.className}Mapper;
import ${project.attrs.basePackage}.model.${it.name.className};
import org.springframework.stereotype.Service;

/**
 * ${it.name} - ${it.comment}
 *
 * @author ${SYS['user.name']}
 */
@Service
public class  ${it.name.className}ServiceImpl extends AbstractService<${it.name.className}, Long, ${it.name.className}Mapper> implements ${it.name.className}Service {

}
