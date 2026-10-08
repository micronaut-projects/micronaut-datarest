package io.micronaut.datarest.ords;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import io.micronaut.datarest.core.Dialect;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

public class Ords {
    /** Set once the containers are up, so several test classes can share them. */
    private static String url;
    private static Throwable setupFailure;

    private static final String SCHEMA_USER = "demo";
    private static final String SCHEMA = "foo";    // p_url_mapping_pattern
    private static final String TABLE = "books";    // p_object_alias
    private static final String DB_PASSWORD = "Oracle_123";
    private static final String ORDS_PASSWORD = "Ords_123";
    private static final String OAUTH_CLIENT = "datarest";
    private static String clientId;
    private static String clientSecret;
    private static final String DB_ALIAS = "db";
    private static final int ORDS_PORT = 8080;

    private static final Network NETWORK = Network.newNetwork();

    private static final OracleContainer DB = new OracleContainer(
        DockerImageName.parse("gvenzl/oracle-free:slim-faststart"))
        .withUsername(SCHEMA_USER)
        .withPassword(DB_PASSWORD)
        .withNetwork(NETWORK)
        .withNetworkAliases(DB_ALIAS)
        .withStartupTimeout(Duration.ofMinutes(5));

    private static final GenericContainer<?> ORDS = new GenericContainer<>(
        DockerImageName.parse("container-registry.oracle.com/database/ords:26.3.0"))
        .dependsOn(DB)
        .withNetwork(NETWORK)
        .withEnv(Map.of(
            "DBHOST", DB_ALIAS,
            "DBPORT", "1521",
            "DBSERVICENAME", "FREEPDB1",
            "ORACLE_PWD", DB_PASSWORD,
            "ORACLE_USER_PWD", ORDS_PASSWORD,
            "DEBUG", "FALSE"))
        .withExposedPorts(ORDS_PORT)
        .waitingFor(Wait.forHttp("/ords/")
            .forPort(ORDS_PORT)
            .forStatusCodeMatching(status -> status == 200 || status == 302)
            .withStartupTimeout(Duration.ofMinutes(10)));

    private static final List<String> SETUP_SQL = List.of(
        """
        create table books (
          id        number generated always as identity primary key,
          title     varchar2(200) not null,
          author    varchar2(200),
          published date
        )""",
        "commit",
        """
        begin
          ords.enable_schema(
            p_enabled             => true,
            p_schema              => 'DEMO',
            p_url_mapping_type    => 'BASE_PATH',
            p_url_mapping_pattern => 'foo',
            p_auto_rest_auth      => false);
          ords.enable_object(
            p_enabled        => true,
            p_schema         => 'DEMO',
            p_object         => 'BOOKS',
            p_object_type    => 'TABLE',
            p_object_alias   => 'books',
            p_auto_rest_auth => true);
          commit;
        end;""",
        // An OAuth client credentials client holding the built-in role that grants access to protected AutoREST objects.
        """
        begin
          oauth.create_client(
            p_name             => '%s',
            p_grant_type       => 'client_credentials',
            p_support_email    => 'datarest@example.com',
            p_privilege_names  => null,
            p_token_duration   => null,
            p_refresh_duration => null,
            p_code_duration    => null);
          oauth.grant_client_role('%s', 'oracle.dbtools.autorest.any.schema');
          commit;
        end;""".formatted(OAUTH_CLIENT, OAUTH_CLIENT));

    public static Map<String, String> getProperties() {
        return getProperties("default");
    }

    public static synchronized Map<String, String> getProperties(String nameQualifier) {
        if (setupFailure != null) {
            // Report the original failure to every test class instead of re-running a half-applied setup.
            throw new IllegalStateException("ORDS fixture setup failed", setupFailure);
        }
        if (url == null) {
            try {
                ORDS.start();
                executeSQL();
                readOAuthClient();
                url = new URI("http", null, ORDS.getHost(), ORDS.getMappedPort(ORDS_PORT), "/ords/" + SCHEMA, null, null).toString();
            } catch (RuntimeException | URISyntaxException e) {
                setupFailure = e;
                throw new IllegalStateException("ORDS fixture setup failed", e);
            }
        }
        return Map.of("restdatasources." + nameQualifier + ".dialect", Dialect.ORDS.toString(),
            "restdatasources." + nameQualifier + ".url", url);
    }

    /**
     * @return the ORDS base URL, ending with the schema alias, once the containers are up
     */
    public static String getUrl() {
        getProperties();
        return url;
    }

    /**
     * @return the OAuth client id ORDS generated for the client credentials client
     */
    public static String getClientId() {
        getProperties();
        return clientId;
    }

    /**
     * @return the OAuth client secret ORDS generated for the client credentials client
     */
    public static String getClientSecret() {
        getProperties();
        return clientSecret;
    }

    private static void readOAuthClient() {
        try (Connection connection = DriverManager.getConnection(DB.getJdbcUrl(), SCHEMA_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                 "select client_id, client_secret from user_ords_clients where name = ?")) {
            statement.setString(1, OAUTH_CLIENT);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("ORDS did not register the OAuth client " + OAUTH_CLIENT);
                }
                clientId = resultSet.getString(1);
                clientSecret = resultSet.getString(2);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read the OAuth client credentials", e);
        }
    }

    private static void executeSQL() {
        try (Connection connection = DriverManager.getConnection(DB.getJdbcUrl(), SCHEMA_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            for (String sql : SETUP_SQL) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create the books table", e);
        }
    }
}
