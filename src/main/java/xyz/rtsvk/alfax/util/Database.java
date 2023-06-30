package xyz.rtsvk.alfax.util;

import xyz.rtsvk.alfax.scheduler.Task;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Database {
	private static boolean initialized;
	private static String url;
	private static Logger logger;

	public static void init(String host, String user, String password, String db) {
		initialized = false;
		logger = new Logger(Database.class);
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			url = "jdbc:mysql://" + user + ":" + password + "@" + host;

			Connection conn = DriverManager.getConnection(url);
			Statement st = conn.createStatement();

			st.addBatch("CREATE DATABASE IF NOT EXISTS `" + db + "`;");
			st.addBatch("USE `" + db + "`;");
			st.executeBatch();

			st.addBatch("CREATE TABLE IF NOT EXISTS `system_info` (`vkey` varchar(64), `value` varchar(128), PRIMARY KEY(`vkey`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `schedule` (`id` int AUTO_INCREMENT, `command` varchar(32), `description` text, `channel` varchar(128), `guild` varchar(128), `exec_date` date, `exec_time` varchar(8), `days` varchar(16), PRIMARY KEY(`id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `auth` (`id` int AUTO_INCREMENT, `auth_key` varchar(128), `user` varchar(64), `enabled` boolean, PRIMARY KEY(`id`));");
			st.executeBatch();

			st.close();
			conn.close();
			url += ("/" + db);
			initialized = true;
		}
		catch (Exception e){
			logger.error("An error occured while trying to initialize the database wrapper class.");
			throw new RuntimeException(e);
		}
	}

	public static boolean schedule(String commandName, String description, String channelId, String guildId, LocalDate execDate, LocalTime execTime, String days) {
		if (!initialized) return false;

		try {
			Connection conn = DriverManager.getConnection(url);

			String sql = "INSERT INTO `schedule`(`command`, `description`, `channel`, `guild`, `exec_date`, `exec_time`, `days`) VALUES (";
			sql += "'" + commandName + "'," +
					"'" + description + "'," +
					"'" + channelId +"'," +
					"'" + guildId + "'," +
					"'" + execDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "'," +
					"'" + execTime.format(DateTimeFormatter.ISO_LOCAL_TIME) + "'," +
					"'" + days + "'";
			sql += ");";
			conn.createStatement().execute(sql);
			conn.close();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static List<Task> getScheduleFor(LocalDate date) {
		List<Task> tasks = new ArrayList<>();
		try {
			if(!initialized) return tasks;
			Connection conn = DriverManager.getConnection(url);
			ResultSet result = conn.createStatement().executeQuery("SELECT * FROM `schedule` WHERE `exec_date`=NULL OR `exec_date`='" + date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "';");

			if (result.isBeforeFirst())
				while (result.next())
					tasks.add(new Task(
							result.getInt("id"),
							result.getString("command"),
							result.getString("channel"),
							result.getString("guild"),
							result.getString("exec_date"),
							result.getString("exec_time"),
							result.getString("days")
					));

			result.close();
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return tasks;
	}

	public static boolean addAPIUser(String name, String hash) {
		if (!initialized) return false;

		try {
			Connection conn = DriverManager.getConnection(url);

			String sql = "INSERT INTO `auth`(`auth_key`, `user`, `enabled`) VALUES (";
			sql += "'" + hash + "'," +
					"'" + name + "'," +
					"'1'";
			sql += ");";
			conn.createStatement().execute(sql);
			conn.close();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean authorizeAPIUser(Object key) {
		if (key == null) return false;
		boolean result = false;

		try {
			if(!initialized) return false;
			Connection conn = DriverManager.getConnection(url);
			ResultSet set = conn.createStatement().executeQuery("SELECT `enabled` FROM `auth` WHERE `auth_key`='" + key.toString() + "';");
			if (set.next())
				result = set.getBoolean("enabled");
			set.close();
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
}
