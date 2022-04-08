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

import java.util.List;

/**
 * 返回多个值集合
 *
 * @param <T> 值类型
 * @author liuzh
 */
public class RowsResponse<T> extends Response<RowsResponse> {

    /**
     * 数据集合
     */
    private List<T> rows;

    /**
     * 总数，分页查询时的总条数
     */
    private Long total;

    public static <T> RowsResponse<T> ok(List<T> rows) {
        return ok(rows, null, null);
    }

    public static <T> RowsResponse<T> ok(List<T> rows, Long total) {
        return ok(rows, total, null);
    }

    public static <T> RowsResponse<T> ok(List<T> rows, String message) {
        return ok(rows, null, message);
    }

    public static <T> RowsResponse<T> ok(List<T> rows, Long total, String message) {
        RowsResponse<T> response = new RowsResponse<>();
        response.success = true;
        response.code = Code.SUCCESS.getCode();
        response.rows = rows;
        response.total = total;
        response.message = message;
        return response;
    }

    public RowsResponse<T> rows(List<T> rows) {
        this.rows = rows;
        return this;
    }

    public RowsResponse<T> total(Long total) {
        this.total = total;
        return this;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
