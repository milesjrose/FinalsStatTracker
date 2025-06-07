package uk.ac.ed.inf.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import uk.ac.ed.inf.model.PlayerStats;

public class PlayerStatsDAO {

    private final static String dbURL = "jdbc:sqlite:playerstats.db"; // Local SQLite file
    private final static String tableName = "player_stats"; // Table name

    public static void initialise() {
        try {
            createTable();
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    private static void createTable() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "hash INTEGER PRIMARY KEY," +  // Using hash as primary key, as you have it
                "name TEXT NOT NULL," +
                "combat INTEGER," +
                "objective INTEGER," +
                "support INTEGER," +
                "elims INTEGER," +
                "assists INTEGER," +
                "deaths INTEGER," +
                "revives INTEGER" +
                ")";

        try (Connection connection = DriverManager.getConnection(dbURL);
             PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL)) {
            preparedStatement.execute();
        }
    }

    public static void addPlayerStats(PlayerStats stats) throws SQLException {
        String insertSQL = "INSERT INTO " + tableName +
                " (hash, name, combat, objective, support, elims, assists, deaths, revives) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(dbURL);
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setInt(1, stats.hash);
            preparedStatement.setString(2, stats.name);
            preparedStatement.setInt(3, stats.combat);
            preparedStatement.setInt(4, stats.objective);
            preparedStatement.setInt(5, stats.support);
            preparedStatement.setInt(6, stats.elims);
            preparedStatement.setInt(7, stats.assists);
            preparedStatement.setInt(8, stats.deaths);
            preparedStatement.setInt(9, stats.revives);

            preparedStatement.executeUpdate();
        }
    }

    public static PlayerStats getPlayerStats(String col, String val) throws SQLException {
        String selectSQL = "SELECT * FROM " + tableName + " WHERE " + col + " = ?";

        try (Connection connection = DriverManager.getConnection(dbURL);
             PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {

            preparedStatement.setString(1, val);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new PlayerStats(
                            resultSet.getString("name"),
                            new int[]{
                                    resultSet.getInt("combat"),
                                    resultSet.getInt("objective"),
                                    resultSet.getInt("support"),
                                    resultSet.getInt("elims"),
                                    resultSet.getInt("assists"),
                                    resultSet.getInt("deaths"),
                                    resultSet.getInt("revives")
                            }
                    );
                } else {
                    return null; // PlayerStats not found
                }
            }
        }
    }

    // Example: Get all player stats (potentially large result set)
    public static java.util.List<PlayerStats> getAllPlayerStats() throws SQLException {
        String selectSQL = "SELECT * FROM " + tableName;
        java.util.List<PlayerStats> playerStatsList = new java.util.ArrayList<>();

        try (Connection connection = DriverManager.getConnection(dbURL);
             PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                PlayerStats stats = new PlayerStats(
                        resultSet.getString("name"),
                        new int[]{
                                resultSet.getInt("combat"),
                                resultSet.getInt("objective"),
                                resultSet.getInt("support"),
                                resultSet.getInt("elims"),
                                resultSet.getInt("assists"),
                                resultSet.getInt("deaths"),
                                resultSet.getInt("revives")
                        }
                );
                playerStatsList.add(stats);
            }
        }
        return playerStatsList;
    }

    public static void deletePlayerStats(String hash) throws SQLException {
        String deleteSQL = "DELETE FROM " + tableName + " WHERE hash = ?";
        try (Connection connection = DriverManager.getConnection(dbURL);
             PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
            preparedStatement.setString(1, hash);
            preparedStatement.executeUpdate();
        }
    }

    // Add update, delete methods as needed

}