package io.mybatis.mapper.base;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @Author dengsd
 * @Date 2022/8/6 10:58
 */
public abstract class AbstractTypeHandler<T> extends BaseTypeHandler<T> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int column, T t, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(column,this.toJson(t));
    }

    @Override
    public T getNullableResult(ResultSet resultSet, String column) throws SQLException {
        return this.parse(resultSet.getString(column));
    }

    @Override
    public T getNullableResult(ResultSet resultSet, int column) throws SQLException {
        return this.parse(resultSet.getString(column));
    }

    @Override
    public T getNullableResult(CallableStatement callableStatement, int column) throws SQLException {
        return this.parse(callableStatement.getString(column));
    }
    protected abstract T parse(String json);
    protected abstract String toJson(T t);
}
