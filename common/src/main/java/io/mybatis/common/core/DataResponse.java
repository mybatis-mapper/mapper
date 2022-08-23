/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mybatis.common.core;

/**
 * 返回单个值
 *
 * @param <T> 值类型
 * @author liuzh
 */
public class DataResponse<T> extends Response<DataResponse> {
  /**
   * 单个数据对象
   */
  private T data;

  public static <T> DataResponse<T> ok(T data) {
    return ok(data, null);
  }

  public static <T> DataResponse<T> ok(T data, String message) {
    DataResponse<T> response = new DataResponse<>();
    response.success = true;
    response.code = Code.SUCCESS.getCode();
    response.data = data;
    response.message = message;
    return response;
  }

  public DataResponse<T> data(T data) {
    this.data = data;
    return this;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
