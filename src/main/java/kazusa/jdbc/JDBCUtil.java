package kazusa.jdbc;


import kazusa.common.utils.connectpool.ConnectionImpl;
import kazusa.common.utils.connectpool.CustomConnectPool;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author kazusa
 * @version 1.0.0
 */
public class JDBCUtil {

	/**
	 * 链接协议规范:jdbc:mysql:// + 数据库服务器ip地址:端口/链接数据库名
	 */
	private static String url;

	/**
	 * 注册驱动对象
	 * mysql5:com.mysql.jdbc.Driver
	 * mysql8:com.mysql.cj.jdbc.Driver
	 */
	private static Driver driver;

	private static Properties properties = new Properties();

	/**
	 * 读取数据注册驱动
	 * @param path 配置文件路径
	 */
	public static JDBCUtil config(String path) {
		try {
			// 读取配置文件
			properties.load(new FileInputStream(path));
			url = properties.getProperty("url");
			// 动态编译:通过反射读取类全限定标识符实例化对象
			driver = (Driver) Class.forName(properties.getProperty("driver")).getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			properties.clone();
		}
		return new JDBCUtil();
	}

	public Properties getProperties() {
		return properties;
	}

	/**
	 * @return 获取链接
	 */
	public Connection getConnection() throws SQLException {
		return driver.connect(url, properties);
	}

	/**
	 * @return 返回JDBC连接池
	 */
	public JDBCPool getJDBCPool() {
		JDBCPool jdbcPool = new JDBCPool(this::getConnection);
		// mysql默认8小时自动断开链接,设置为7小时提前释放获取新链接更新
		jdbcPool.setTime(7);
		jdbcPool.setTimeUnit(TimeUnit.HOURS);
		return jdbcPool;
	}

	public static class JDBCPool extends CustomConnectPool<Connection> {

		public JDBCPool(ConnectionImpl<Connection> connectionImpl) {
			super(connectionImpl);
		}

		public JDBCPool(ConnectionImpl<Connection> connectionImpl, int maxNaturalResources) {
			super(connectionImpl, maxNaturalResources);
		}

		public JDBCPool(ConnectionImpl<Connection> connectionImpl, int coreNaturalResources, int maxNaturalResources) {
			super(connectionImpl, coreNaturalResources, maxNaturalResources);
		}

		public JDBCPool(ConnectionImpl<Connection> connectionImpl, int coreNaturalResources, int maxNaturalResources, int queueLength) {
			super(connectionImpl, coreNaturalResources, maxNaturalResources, queueLength);
		}
	}

	/**
	 * 关流
	 * @param connection 链接对象
	 * @param resultSet 表列对象
	 */
	public void close(Connection connection, ResultSet resultSet) throws SQLException {
		try (connection;resultSet) {
			if (connection == null) return;
			connection.createStatement().close();
		}
	}
}