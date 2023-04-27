package kazusa.common.operateinterface.operateparameterinterface;

/**
 * @author kazusa
 * @version 1.1.0
 */
@FunctionalInterface
public interface OperateParameterReturn<T> {

	T operateParameterReturnImpl(T t) throws Exception;
}