package kazusa.common.operateinterface;

/**
 * @author kazusa
 * @param <T> 非void返回值形参操作
 */
@FunctionalInterface
public interface OperateParameterReturn<T> {

	T operateParameterReturnImpl(T... ts) throws Exception;
}
