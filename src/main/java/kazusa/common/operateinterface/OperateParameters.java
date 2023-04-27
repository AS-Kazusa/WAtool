package kazusa.common.operateinterface;

/**
 * @author kazusa
 * @version 1.1.0
 * @param <T>
 */
@FunctionalInterface
public interface OperateParameters<T> {

	void operateParametersImpl(T... ts) throws Exception;
}