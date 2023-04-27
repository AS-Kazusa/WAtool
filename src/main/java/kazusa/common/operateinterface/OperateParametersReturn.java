package kazusa.common.operateinterface;

/**
 * @author kazusa
 * @param <T> 非void返回值形参操作
 */
@FunctionalInterface
public interface OperateParametersReturn<T> {

	T operateParametersReturnImpl(T... ts) throws Exception;
}
