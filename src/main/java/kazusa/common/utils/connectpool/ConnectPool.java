package kazusa.common.utils.connectpool;

import java.util.List;

public interface ConnectPool<T> {

	/**
	 * 默认采用周期性更新连接池存在所有核心资源
	 * @param connections 记录核心资源集合
	 */
	List<T> updateConnections(List<T> connections);

	/**
	 * @return 返回一个链接
	 * @throws InterruptedException
	 */
	T getConnection() throws Exception;

	/**
	 * @param connection 归还链接
	 */
	void setConnection(T connection);
}
