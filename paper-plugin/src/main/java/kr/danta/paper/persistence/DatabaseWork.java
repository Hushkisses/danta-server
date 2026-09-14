package kr.danta.paper.persistence;

import java.sql.Connection;

@FunctionalInterface
public interface DatabaseWork<T> {
    T execute(Connection connection) throws Exception;
}
