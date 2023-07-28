package xyz.rtsvk.alfax.util;

import xyz.rtsvk.alfax.scheduler.Task;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

	public static final byte PERMISSION_ADMIN = 0x01;
	public static final byte PERMISSION_API_CHANNEL = 0x02;
	public static final byte PERMISSION_API_DM = 0x04;
	public static final byte PERMISSION_API = PERMISSION_API_CHANNEL | PERMISSION_API_DM;
	public static final byte PERMISSION_MQTT = 0x08;

	public static void init(String host, String user, String password, String db) {
		logger = new Logger(Database.class);
		initialized = false;
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
			st.addBatch("CREATE TABLE IF NOT EXISTS `auth` (`id` varchar(128), `auth_key` varchar(128), `permissions` int, PRIMARY KEY(`id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `sensors`(`id` int AUTO_INCREMENT, `key` varchar(128), `description` text, `type` varchar(32), `unit` varchar(16), `min` float, `max` float, `value` float, `last_updated` datetime, PRIMARY KEY(`id`));");
			st.executeBatch();

			st.close();
			conn.close();
			url += ("/" + db);
			initialized = true;
			logger.info("Database wrapper class initialized successfully.");
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

	@Deprecated
	public static boolean addAPIUser(String id, String hash) {
		return addUser(id, hash, PERMISSION_API);
	}

	public static boolean addUser(String id, String hash, int permissions) {
		if (!initialized) return false;

		try {
			Connection conn = DriverManager.getConnection(url);

			String sql = "INSERT INTO `auth`(`id`, `auth_key`, `permissions`) VALUES (";
			sql += "'" + id + "'," +
					"'" + hash + "'," +
					"'" + permissions + "'";
			sql += ");";
			conn.createStatement().execute(sql);
			conn.close();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean checkPermissions(String id, byte permissions) {
		if (!initialized) return false;

		try {
			Connection conn = DriverManager.getConnection(url);
			ResultSet set = conn.createStatement().executeQuery("SELECT `permissions` FROM `auth` WHERE `id`='" + id + "';");
			if (set.next()) {
				byte userPermissions = set.getByte("permissions");
				logger.info("checkin permissions for " + id);
				set.close();
				conn.close();
				return (userPermissions & permissions) == permissions;
			}
			else {
				set.close();
				conn.close();
				logger.warn("User " + id + " not found in database.");
				return false;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean updateUserPermissions(String id, int permissions) {
		if (!initialized) return false;

		try {
			Connection conn = DriverManager.getConnection(url);
			Statement st = conn.createStatement();

			// check if user exists
			ResultSet set = st.executeQuery("SELECT `permissions` FROM `auth` WHERE `id`='" + id + "';");
			if (!set.next()) {
				set.close();
				st.close();
				conn.close();
				return false;
			}

			// update permissions
			String sql = "UPDATE `auth` SET `permissions`='" + permissions + "' WHERE `id`='" + id + "';";
			st.execute(sql);

			set.close();
			st.close();
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
			String query = "SELECT `permissions` FROM `auth` WHERE `auth_key`='" + key + "';";
			ResultSet set = conn.createStatement().executeQuery(query);
			if (set.next()) {
				byte p = set.getByte("permissions");
				result = (p & PERMISSION_API) == PERMISSION_API || (p & PERMISSION_ADMIN) == PERMISSION_ADMIN;
			}
			set.close();
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean registerSensor(String key, String description, String type, String unit, float min, float max) {
		if (!initialized) return false;

		try {
			Connection conn = DriverManager.getConnection(url);

			String sql = "INSERT INTO `sensors`(`key`, `description`, `type`, `unit`, `min`, `max`, `value`, `last_updated`) VALUES (";
			sql += "'" + key + "'," +
					"'" + description + "'," +
					"'" + type + "'," +
					"'" + unit + "'," +
					"'" + min + "'," +
					"'" + max + "'," +
					"'0'," +
					"'" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "'";
			sql += ");";
			conn.createStatement().execute(sql);
			conn.close();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String hash(String input) throws NoSuchAlgorithmException {
		String result = input;
		if(input != null) {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(input.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			result = hash.toString(16);
			while(result.length() < 128) {
				result = "0" + result;
			}
		}
		return result;
	}

	public static boolean updateSensorData(SensorData data) {
		return false;
	}

	public static int getAdminCount() {
		if (!initialized) return -1;

		try {
			Connection conn = DriverManager.getConnection(url);
			ResultSet set = conn.createStatement().executeQuery("SELECT COUNT(*) FROM `auth` WHERE `permissions` & " + PERMISSION_ADMIN + " = " + PERMISSION_ADMIN + ";");
			if (set.next()) {
				int count = set.getInt(1);
				set.close();
				conn.close();
				return count;
			}
			else {
				set.close();
				conn.close();
				return -1;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static class SensorData {

	}
}
