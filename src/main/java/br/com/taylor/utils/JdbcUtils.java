package br.com.taylor.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class JdbcUtils {

    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }

    public static LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;

        // 1. Caso Postgres (ou SQLite com driver configurado): Timestamp direto
        if (obj instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }

        // 2. Caso SQLite: Número (milissegundos)
        if (obj instanceof Number num) {
            return LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(num.longValue()),
                    java.time.ZoneId.systemDefault()
            );
        }

        // 3. Caso SQLite antigo: Texto (ISO)
        if (obj instanceof String str) {
            // Se a string for apenas números, podemos tratar como milissegundos também
            if (str.matches("\\d+")) {
                return LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(Long.parseLong(str)),
                        java.time.ZoneId.systemDefault()
                );
            }
            return LocalDateTime.parse(str);
        }

        return null;
    }
}
