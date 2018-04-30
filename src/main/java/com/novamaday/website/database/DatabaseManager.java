package com.novamaday.website.database;

import com.novamaday.website.objects.*;
import com.novamaday.website.utils.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
            String confirmationTableName = String.format("%sconfirmation", databaseInfo.getPrefix());
            String pluginTableName = String.format("%splugins", databaseInfo.getPrefix());

            String createAccountsTable = "CREATE TABLE IF NOT EXISTS " + accountsTableName +
                    "(user_id VARCHAR(255) not NULL, " +
                    " username VARCHAR(255) not NULL, " +
                    " email LONGTEXT not NULL, " +
                    " hash LONGTEXT not NULL, " +
                    " email_confirmed BOOLEAN not NULL, " +
                    " admin BOOLEAN not NULL, " +
                    " PRIMARY KEY (user_id))";
            String createAPITable = "CREATE TABLE IF NOT EXISTS " + apiTableName +
                    " (user_id varchar(255) not NULL, " +
                    " api_key varchar(64) not NULL, " +
                    " blocked BOOLEAN not NULL, " +
                    " time_issued LONG not NULL, " +
                    " uses INT not NULL, " +
                    " PRIMARY KEY (user_id, api_key))";
            String createConfirmationTable = "CREATE TABLE IF NOT EXISTS " + confirmationTableName +
                    "(user_id VARCHAR(255) not NULL, " +
                    " code VARCHAR(32) not NULL, " +
                    "PRIMARY KEY (user_id))";
            String createPluginsTable = "CREATE TABLE IF NOT EXISTS " + pluginTableName +
                    "(name VARCHAR(255) not NULL, " +
                    " version VARCHAR(255) not NULL, " +
                    " main_page LONGTEXT not NULL , " +
                    " download_link LONGTEXT not NULL, " +
                    "PRIMARY KEY (name))";

            statement.execute(createAccountsTable);
            statement.execute(createAPITable);
            statement.execute(createConfirmationTable);
            statement.execute(createPluginsTable);

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
                String tableName = String.format("%saccounts", databaseInfo.getPrefix());
                String query = "INSERT INTO " + tableName + " (user_id, username, email, hash, email_confirmed, admin) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);

                statement.setString(1, UUID.randomUUID().toString());
                statement.setString(2, username);
                statement.setString(3, email);
                statement.setString(4, hash);
                statement.setBoolean(5, false);
                statement.setBoolean(6, false);

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
                    u.setEmailConfirmed(res.getBoolean("email_confirmed"));
                    u.setAdmin(res.getBoolean("admin"));

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
                    u.setEmailConfirmed(res.getBoolean("email_confirmed"));
                    u.setAdmin(res.getBoolean("admin"));

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

    public User getUserFromId(UUID id) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%saccounts", databaseInfo.getPrefix());
                String query = "SELECT * FROM " + tableName + " WHERE user_id = ?";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                statement.setString(1, id.toString());

                ResultSet res = statement.executeQuery();

                Boolean hasStuff = res.next();

                if (hasStuff) {
                    User u = new User(id);
                    u.setUsername(res.getString("username"));
                    u.setEmail(res.getString("email"));
                    u.setEmailConfirmed(res.getBoolean("email_confirmed"));
                    u.setAdmin(res.getBoolean("admin"));

                    statement.close();
                    return u;
                }
                statement.close();
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to get user from database by id", e, this.getClass());
        }
        return null;
    }

    public boolean validLogin(String email, String password) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%saccounts", databaseInfo.getPrefix());
                String query = "SELECT * FROM " + tableName + " WHERE email = ?";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                statement.setString(1, email);

                ResultSet res = statement.executeQuery();

                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                if (res.next() && encoder.matches(password, res.getString("hash"))) {
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

    public boolean updateUser(User user) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%saccounts", databaseInfo.getPrefix());

                String query = "SELECT * FROM " + tableName + " WHERE user_id = '" + user.getId().toString() + "';";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                ResultSet res = statement.executeQuery();

                Boolean hasStuff = res.next();

                if (!hasStuff || res.getString("user_id") == null) {
                    //Data not present. this should not be possible.
                    statement.close();
                    return false;
                } else {
                    //Data present, update.
                    String update = "UPDATE " + tableName
                            + " SET username = ?, email = ?,"
                            + " email_confirmed = ?, admin = ? WHERE user_id = ?";
                    PreparedStatement ps = databaseInfo.getConnection().prepareStatement(update);

                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getEmail());
                    ps.setBoolean(3, user.isEmailConfirmed());
                    ps.setBoolean(4, user.isAdmin());
                    ps.setString(5, user.getId().toString());

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

    public void addPendingConfirmation(User user, String code) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%sconfirmation", databaseInfo.getPrefix());
                String query = "INSERT INTO " + tableName + " (user_id, code) VALUES (?, ?)";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);

                statement.setString(1, user.getId().toString());
                statement.setString(2, code);

                statement.execute();
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to input confirmation code", e, this.getClass());
        }
    }

    public Confirmation getConfirmationInfo(String code) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%sconfirmation", databaseInfo.getPrefix());
                String query = "SELECT * FROM " + tableName + " WHERE code = '" + code + "';";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                ResultSet res = statement.executeQuery();

                Boolean hasStuff = res.next();

                if (hasStuff && res.getString("code") != null) {
                    Confirmation con = new Confirmation();
                    con.setUserId(UUID.fromString(res.getString("user_id")));
                    con.setCode(code);

                    statement.close();

                    return con;
                } else {
                    //Data not present.
                    statement.close();
                    return null;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to get confirmation data", e, this.getClass());
        }
        return null;
    }

    public void removeConfirmationInfo(String code) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%sconfirmation", databaseInfo.getPrefix());
                String query = "DELETE FROM " + tableName + " WHERE code = '" + code + "';";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);

                statement.execute();
                statement.close();
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to delete confirmation data", e, this.getClass());
        }
    }

    public boolean addPlugin(Plugin plugin) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%splugins", databaseInfo.getPrefix());

                String query = "SELECT * FROM " + tableName + " WHERE name = '" + plugin.getName() + "';";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                ResultSet res = statement.executeQuery();

                Boolean hasStuff = res.next();

                if (!hasStuff || res.getString("name") == null) {
                    statement.close();

                    //Data not present, add.
                    String update = "INSERT INTO " + tableName + " (name, version, main_page, download_page) VALUES (?, ?, ?, ?)";
                    PreparedStatement ps = databaseInfo.getConnection().prepareStatement(update);

                    ps.setString(1, plugin.getName());
                    ps.setString(2, plugin.getVersion());
                    ps.setString(3, plugin.getMainPage());
                    ps.setString(4, plugin.getDownloadLink());

                    ps.execute();
                    ps.close();

                    return true;
                } else {
                    statement.close();

                    //Data present, update.
                    String update = "UPDATE " + tableName
                            + " SET version = ?, main_page = ?,"
                            + " download_link = ? WHERE name = ?";
                    PreparedStatement ps = databaseInfo.getConnection().prepareStatement(update);

                    ps.setString(1, plugin.getVersion());
                    ps.setString(2, plugin.getMainPage());
                    ps.setString(3, plugin.getDownloadLink());
                    ps.setString(4, plugin.getName());

                    ps.executeUpdate();

                    ps.close();
                    return true;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to add/update plugin in database", e, this.getClass());
        }
        return false;
    }

    public Plugin getPlugin(String name) {
        try {
            if (databaseInfo.getMySQL().checkConnection()) {
                String tableName = String.format("%splugins", databaseInfo.getPrefix());

                String query = "SELECT * FROM " + tableName + " WHERE name = '" + name + "';";
                PreparedStatement statement = databaseInfo.getConnection().prepareStatement(query);
                ResultSet res = statement.executeQuery();

                Boolean hasStuff = res.next();

                if (hasStuff && res.getString("name") != null) {
                    Plugin plugin = new Plugin(name);
                    plugin.setVersion(res.getString("version"));
                    plugin.setMainPage(res.getString("main_page"));
                    plugin.setDownloadLink(res.getString("download_link"));

                    statement.close();
                    return plugin;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger().exception("Failed to get plugin from database", e, this.getClass());
        }
        return null;
    }
}