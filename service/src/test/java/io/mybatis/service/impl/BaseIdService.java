package io.mybatis.service.impl;

import io.mybatis.mapper.BaseMapper;
import io.mybatis.service.AbstractService;
import io.mybatis.service.model.BaseId;

import java.util.List;

public abstract class BaseIdService<T extends BaseId<T>, M extends BaseMapper<T, Integer>> extends AbstractService<T, Integer, M> {

  /**
   * 根据ID列表进行删除，issues #50
   */
  public int deleteByIdList(List<Integer> ids) {
    return deleteByFieldList(T::getId, ids);
  }

}
