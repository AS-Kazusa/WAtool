package kazusa.web.ip;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 代理IP封装对象
 * @author kazusa
 * @version 1.0.0
 * @see ProxyIpPool
 */
@AllArgsConstructor
@Getter
public class ProxyIP {

	private String ip;

	private int port;

	private String type;
}
