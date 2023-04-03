package kazusa.common.codeoptimize.operateinterface;

@FunctionalInterface
public interface OperateParameter<T> {

	void operateParameterImpl(T... ts) throws Exception;
}
