package es.amplia.oda.statemanager.inmemory;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class Main {

	public static Gson gson = new Gson();
	public static Connection connection;

	public static void main(String[] args) throws IOException {
		String localPath = new File(".").getCanonicalPath();
		String url = "jdbc:sqlite:" + localPath + "/oda-statemanagers/inmemory/src/main/java/es/amplia/oda/statemanager/inmemory/AWOrangeStar.db";
		System.out.println("The url to create the database is " + url);

		try (Connection conn = DriverManager.getConnection(url)) {
			connection = conn;

			String sql = "CREATE TABLE IF NOT EXISTS troops (" +
					"unitNumber integer PRIMARY KEY," +
					"type text," +
					"hp integer," +
					"class Data" +
					");";
			execute(conn.createStatement(), sql);
			insert("infanteria", 10, new Data("orange", "river"));
			insert("infanteria", 5, new Data("orange", "mountain"));
			insert("infanteria", 7, new Data("green", "beach"));
			insert("TOA", 10, new Data("orange", "plain"));
			insert("infanteria mec.", 10, new Data("orange", "forest"));
			ResultSet result = query("infanteria mec.");

			while (result.next()) {
				System.out.println("Unidad de infantería #" + (result.getInt("unitNumber")) + ": " + (gson.fromJson(result.getString("class"), Data.class).terrain));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static boolean execute(Statement statement, String sql) throws SQLException {
		return statement.execute(sql);
	}

	public static void insert(String type, int hp, Data data) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO troops (type, hp, class) VALUES(?,?,?)");

		preparedStatement.setString(1,  type);
		preparedStatement.setInt(2,  hp);
		String toStoreObject = gson.toJson(data, Data.class);
		preparedStatement.setString(3,  toStoreObject);
		preparedStatement.executeUpdate();
	}

	public static ResultSet query(String type) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM troops WHERE type=?");
		preparedStatement.setString(1, type);
		return preparedStatement.executeQuery();
	}

	public static class Data {
		public final String color;
		public final String terrain;

		public Data(String color, String terrain) {
			this.color = color;
			this.terrain = terrain;
		}
	}
}
