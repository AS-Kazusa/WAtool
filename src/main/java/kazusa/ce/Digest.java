package kazusa.ce;

import lombok.Data;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 数字摘要对象
 * 官方文档:<a href="https://docs.oracle.com/en/java/javase/14/docs/specs/security/standard-names.html#messagedigest-algorithms">...</a>
 * @author kazusa
 * @version 1.0.0
 */
@Data
public class Digest {

	/**
	 * 默认使用的数字摘要算法SHA3-256
	 */
	private String hashCodeAlgorithm = "SHA3-256";

	/**
	 * 默认使用HmacSHA256算法
	 */
	private String hMacHashCodeAlgorithm = "HmacSHA256";

	/**
	 * hmac算法密钥(salt盐)
	 */
	private SecretKey key;

	/**
	 * 加解密对象
	 */
	private Mac mac;

	/**
	 * hashCode显示编码格式:默认16进制
	 */
	private String type = "16";

	public Digest() {}

	/**
	 * @param hMacHashCodeAlgorithm Hmac算法
	 * @throws NoSuchAlgorithmException
	 */
	public Digest(String hMacHashCodeAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException {
		this.hMacHashCodeAlgorithm = hMacHashCodeAlgorithm;
		key = KeyGenerator.getInstance(hMacHashCodeAlgorithm).generateKey();
		mac = Mac.getInstance(hMacHashCodeAlgorithm);
		// 初始化
		mac.init(key);
	}

	public void setHashCodeAlgorithm(String hashCodeAlgorithm) {
		this.hashCodeAlgorithm = hashCodeAlgorithm;
	}

	/**
	 * 根据key数组和算法转回key对象
	 * @param keyBytes 数组密钥
	 */
	public void setKey(byte[] keyBytes) {
		this.key = new SecretKeySpec(keyBytes,getHashCodeAlgorithm());
	}
}
