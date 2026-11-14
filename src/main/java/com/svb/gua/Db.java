package com.svb.gua;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    private Db() {}
    public static Connection connect() throws SQLException {
        String url = System.getenv().getOrDefault("SVB_DB_URL", "jdbc:postgresql://localhost:5433/svb_gua");
        String user = System.getenv().getOrDefault("SVB_DB_USER", "postgres");
        String pass = System.getenv().getOrDefault("SVB_DB_PASSWORD", "123456");
        return DriverManager.getConnection(url, user, pass);
    }
    public static String hash(String value) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) out.append(String.format("%02x", b));
        return out.toString();
    }
}
