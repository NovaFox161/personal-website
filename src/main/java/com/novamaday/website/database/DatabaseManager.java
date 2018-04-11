package com.novamaday.website.database;

import com.novamaday.website.objects.SiteSettings;
import com.novamaday.website.objects.User;
import com.novamaday.website.objects.UserAPIAccount;
import com.novamaday.website.utils.Logger;

import java.sql.*;
import java.util.UUID;

@SuppressWarnings({"SqlResolve", "UnusedReturnValue", "SqlNoDataSourceInspection"})
public class DatabaseManager {
    private static DatabaseManager instance;
    private DatabaseInfo databaseInfo;

    private DatabaseManager() {
    } //Prevent initialization.

    /**
     * Gets the instance of the {@link DatabaseManager}.
     *
     * @return The instance of the {@link DatabaseManager}
     */
    public static DatabaseManager getManager() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Connects to the MySQL server specified.
     */
    public void connectToMySQL() {
        try {
            MySQL mySQL = new MySQL(SiteSettings.SQL_HOST.get(), SiteSettings.SQL_PORT.get(), SiteSettings.SQL_DB.get(), SiteSettings.SQL_PREFIX.get(), SiteSettings.SQL_USER.get(), SiteSettings.SQL_PASSWORD.get());

            Connection mySQLConnection = mySQL.openConnection();
            databaseInfo = new DatabaseInfo(mySQL, mySQLConnection, mySQL.getPrefix());
            System.out.println("Connected to MySQL database!");
        } catch (Exception e) {
            System.out.println("Failed to connect to MySQL database! Is it properly configured?");
            e.printStackTrace();
            Logger.getLogger().exception("Failed to connect to MySQL Database!", e, this.getClass());
        }
    }

    /**
     * Disconnects from the MySQL server if still connected.
     */
    public void disconnectFromMySQL() {
        if (databaseInfo != null) {
            try {
                databaseInfo.getMySQL().closeConnection();
                System.out.println("Successfully disconnected from MySQL Database!");
            } catch (SQLException e) {
                System.out.println("MySQL Connection may not have closed properly! Data may be invalidated!");
            }
        }
    }

    /**
     * Creates all required tables in the database if they do not exist.
     */
    public void createTables() {
        try {
            Statement statement = databaseInfo.getConnection().createStatement();

            String accountsTableName = String.format("%saccounts", databaseInfo.getPrefix());
            String apiTableName = String.format("%sapi", databaseInfo.getPrefix());

            String createAccountsTable = "CREATE TABLE IF NOT EXISTS " + accountsTableName +
                    "(user_id VARCHAR(255) not NULL, " +
                    " username VARCHAR(255) not NULL, " +
                    " email LONGTEXT not NULL, " +
                    " hash LONGTEXT not NULL, " +
                    " PRIMARY KEY (user_id))";
            String createAPITable = "CREATE TABLE IF NOT EXISTS " + apiTableName +
                    " (user_id varchar(255) not NULL, " +
                    " api_key varchar(64) not NULL, " +
                    " blocked BOOLEAN not NULL, " +
                    " time_issued LONG not NULL, " +
                    " uses INT not NULL, " +
                    " PRIMARY KEY (user_id, api_key))";

            statement.execute(createAccountsTable);
            statement.execute(createAPITable);

            statement.close();
            System.out.println("Successfully created needed tables in MySQL database!");
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to create database tables", e, this.getClass());
        }
    }

    //Actual database methods that are very useful
    public void addNewUser(String username, String email, String hash) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                //TODO: Use ps to add account!
                String tableName = String.format("%saccounts", databaseInfo.getPrefix());
                String query = "INSERT INTO " + tableName + " (user_id, username, email, hash) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);

                statement.setString(1, UUID.randomUUID().toString());
                statement.setString(2, username);
                statement.setString(3, email);
                statement.setString(4, hash);

                statement.execute();
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to register new user", e, this.getClass());
        }
    }

    public User getUserFromUsername(String username) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%saccounts", databaseInfo.getPrefix());
                String query = "SELECT * FROM " + tableName + " WHERE username = ?";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                statement.setString(1, username);

                ResultSet res = statement.executeQuery();

                Boolean hasStuff = res.next();

                if (hasStuff) {
                    User u = new User(UUID.fromString(res.getString("user_id")));
                    u.setUsername(username);
                    u.setEmail(res.getString("email"));

                    statement.close();
                    return u;
                }
                statement.close();
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to get user from database by username", e, this.getClass());
        }
        return null;
    }

    public User getUserFromEmail(String email) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%saccounts", databaseInfo.getPrefix());
                String query = "SELECT * FROM " + tableName + " WHERE email = ?";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                statement.setString(1, email);

                ResultSet res = statement.executeQuery();

                Boolean hasStuff = res.next();

                if (hasStuff) {
                    User u = new User(UUID.fromString(res.getString("user_id")));
                    u.setUsername(res.getString("username"));
                    u.setEmail(email);

                    statement.close();
                    return u;
                }
                statement.close();
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to get user from database by email", e, this.getClass());
        }
        return null;
    }

    public boolean validLogin(String email, String hash) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%saccounts", databaseInfo.getPrefix());
                String query = "SELECT * FROM " + tableName + " WHERE email = ?";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                statement.setString(1, email);

                ResultSet res = statement.executeQuery();

                Boolean hasStuff = res.next();

                if (hasStuff && res.getString("hash").equals(hash)) {
                    statement.close();
                    return true;
                }
                statement.close();
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to validate login", e, this.getClass());
        }
        return false;
    }

    public boolean usernameOrEmailTaken(String username, String email) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%saccounts", databaseInfo.getPrefix());

                //Try email first....
                String query = "SELECT * FROM " + tableName + " WHERE email = ? OR username = ?";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, username);

                ResultSet res = statement.executeQuery();

                Boolean hasStuff = res.next();

                if (hasStuff) {
                    statement.close();
                    return true;
                }
                statement.close();
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to verify username/email taken", e, this.getClass());
        }
        return false;
    }

    public boolean updateAPIAccount(UserAPIAccount acc) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%sapi", databaseInfo.getPrefix());

                String query = "SELECT * FROM " + tableName + " WHERE api_key = '" + acc.getAPIKey() + "';";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                ResultSet res = statement.executeQuery();

                Boolean hasStuff = res.next();

                if (!hasStuff || res.getString("api_key") == null) {
                    //Data not present, add to DB.
                    String insertCommand = "INSERT INTO " + tableName +
                            "(user_id, api_key, blocked, time_issued, uses)" +
                            " VALUES (?, ?, ?, ?, ?);";
                    PreparedStatement ps = databaseInfo.getConnection().prepareStatement(insertCommand);
                    ps.setString(1, acc.getUserId().toString());
                    ps.setString(2, acc.getAPIKey());
                    ps.setBoolean(3, acc.isBlocked());
                    ps.setLong(4, acc.getTimeIssued());
                    ps.setInt(5, acc.getUses());

                    ps.executeUpdate();
                    ps.close();
                    statement.close();
                } else {
                    //Data present, update.
                    String update = "UPDATE " + tableName
                            + " SET user_id = ?, blocked = ?,"
                            + " uses = ? WHERE api_key = ?";
                    PreparedStatement ps = databaseInfo.getConnection().prepareStatement(update);

                    ps.setString(1, acc.getUserId().toString());
                    ps.setBoolean(2, acc.isBlocked());
                    ps.setInt(3, acc.getUses());
                    ps.setString(4, acc.getAPIKey());

                    ps.executeUpdate();

                    ps.close();
                    statement.close();
                }
                return true;
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to update API account", e, this.getClass());
        }
        return false;
    }

    public UserAPIAccount getAPIAccount(String APIKey) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String dataTableName = String.format("%sapi", databaseInfo.getPrefix());

                String query = "SELECT * FROM " + dataTableName + " WHERE api_key = '" + APIKey + "';";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                ResultSet res = statement.executeQuery();

                Boolean hasStuff = res.next();

                if (hasStuff && res.getString("api_key") != null) {
                    UserAPIAccount account = new UserAPIAccount();
                    account.setAPIKey(APIKey);
                    account.setUserId(UUID.fromString(res.getString("user_id")));
                    account.setBlocked(res.getBoolean("blocked"));
                    account.setTimeIssued(res.getLong("time_issued"));
                    account.setUses(res.getInt("uses"));

                    statement.close();

                    return account;
                } else {
                    //Data not present.
                    statement.close();
                    return null;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to get API Account.", e, this.getClass());
        }
        return null;
    }
}