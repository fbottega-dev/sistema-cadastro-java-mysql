package com.example.sistema_usuarios;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.DriverManager;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class DisplayNameMigrationTests {
  @Test
  void existingAccountKeepsItsCredentialsWhenDisplayNameIsAdded() throws Exception {
    var url = "jdbc:h2:mem:migration_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    Flyway.configure().dataSource(url, "sa", "").target("1").load().migrate();
    try (var connection = DriverManager.getConnection(url, "sa", "");
        var statement = connection.createStatement()) {
      statement.executeUpdate(
          "INSERT INTO usuarios (username, password, role) VALUES ('conta.antiga',"
              + " 'hash-existente', 'USER')");
    }

    Flyway.configure().dataSource(url, "sa", "").load().migrate();

    try (var connection = DriverManager.getConnection(url, "sa", "");
        var statement = connection.createStatement();
        var result =
            statement.executeQuery("SELECT username, display_name, password, role FROM usuarios")) {
      assertTrue(result.next());
      assertEquals("conta.antiga", result.getString("username"));
      assertEquals("conta.antiga", result.getString("display_name"));
      assertEquals("hash-existente", result.getString("password"));
      assertEquals("USER", result.getString("role"));
      assertFalse(result.next());
    }
  }
}
