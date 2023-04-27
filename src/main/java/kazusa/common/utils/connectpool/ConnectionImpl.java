package kazusa.common.utils.connectpool;

/**
 * @param <T>  自定义连接池资源实现接口
 */
@FunctionalInterface
public interface ConnectionImpl<T> {

	T getConnection() throws Exception;

}
