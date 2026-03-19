package br.com.taylor.infra;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionSupplier {
    Connection get() throws SQLException;
}