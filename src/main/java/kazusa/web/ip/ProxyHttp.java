package kazusa.web.ip;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * 代理IP
 * @author kazusa
 * @version 1.0.0
 * @param <T>
 * @see ProxyIpPool
 */
@AllArgsConstructor
@Getter
public class ProxyHttp<T> {

	private T httpResponseType;

	private ProxyIpPool proxyIpPool;

	private long time;

	private TimeUnit timeUnit;
}
