package kazusa.common.operateinterface;

/**
 * @author kazusa
 */
@FunctionalInterface
public interface OperateParameter<T> {

	void operateParameterImpl(T... ts) throws Exception;
}
