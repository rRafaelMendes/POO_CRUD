package br.com.POO_CRUD.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DEFAULT_DATABASE_NAME = "banco_curso";
    private static final String MYSQL_LOCALHOST_URL = "jdbc:mysql://localhost:3306";
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private static boolean initialized = false;

    public static Connection getConnection() throws SQLException {
        initializeDatabase();

        return DriverManager.getConnection(
                getDatabaseUrl(),
                getRequiredConfig("DB_USER"),
                getRequiredConfig("DB_PASSWORD")
        );
    }

    private static synchronized void initializeDatabase() throws SQLException {
        if (initialized) {
            return;
        }

        createDatabase();

        try (Connection conn = DriverManager.getConnection(
                getDatabaseUrl(),
                getRequiredConfig("DB_USER"),
                getRequiredConfig("DB_PASSWORD"))) {
            createTables(conn);
        }

        initialized = true;
    }

    private static void createDatabase() throws SQLException {
        String databaseName = getDatabaseName();

        try (Connection conn = DriverManager.getConnection(
                getServerUrl(),
                getRequiredConfig("DB_USER"),
                getRequiredConfig("DB_PASSWORD"));
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + databaseName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        String clienteTable = """
                CREATE TABLE IF NOT EXISTS cliente (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nome VARCHAR(100) NOT NULL,
                    cpf VARCHAR(11) NOT NULL UNIQUE,
                    cep VARCHAR(8) NOT NULL,
                    rua VARCHAR(150) NOT NULL,
                    cidade VARCHAR(100) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

        String contaTable = """
                CREATE TABLE IF NOT EXISTS conta_bancaria (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    numero_conta VARCHAR(20) NOT NULL UNIQUE,
                    saldo DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                    cliente_id INT NOT NULL,
                    CONSTRAINT fk_conta_cliente
                        FOREIGN KEY (cliente_id) REFERENCES cliente(id)
                        ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(clienteTable);
            stmt.executeUpdate(contaTable);
        }

        updateClienteTable(conn);
    }

    private static void updateClienteTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (!columnExists(conn, "cliente", "rua")) {
                stmt.executeUpdate("ALTER TABLE cliente ADD COLUMN rua VARCHAR(150) NOT NULL DEFAULT ''");
            }

            if (!columnExists(conn, "cliente", "cidade")) {
                stmt.executeUpdate("ALTER TABLE cliente ADD COLUMN cidade VARCHAR(100) NOT NULL DEFAULT ''");
            }
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metadata = conn.getMetaData();

        try (ResultSet columns = metadata.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            return columns.next();
        }
    }

    private static String getDatabaseUrl() {
        return MYSQL_LOCALHOST_URL + "/" + getDatabaseName();
    }

    private static String getServerUrl() {
        return MYSQL_LOCALHOST_URL;
    }

    private static String getDatabaseName() {
        String databaseName = dotenv.get("DB_NAME");

        if (databaseName == null || databaseName.isBlank()) {
            databaseName = System.getenv("DB_NAME");
        }

        return databaseName == null || databaseName.isBlank() ? DEFAULT_DATABASE_NAME : databaseName;
    }

    private static String getRequiredConfig(String key) {
        String value = dotenv.get(key);

        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configure a variavel " + key + " no arquivo .env ou nas variaveis de ambiente.");
        }

        return value;
    }
}
