package kazusa.common.codeoptimize.operateinterface;

/**
 * @param <T> 非void返回值操作
 */
@FunctionalInterface
public interface OperateReturn<T> {

	T operateReturnImpl() throws Exception;
}
