package kazusa.ce;

import kazusa.common.codeoptimize.designpattern.prototype.DeepCopy;
import lombok.Data;

import java.io.Serializable;

/**
 * 封装传输对象
 * @author kazusa
 * @version 1.0.0
 */
@Data
public class ce implements Serializable {

	/**
	 * 数组密文
	 */
	private byte[] encryptionBytes;

	/**
	 * 字符串密文
	 */
	private String encryptionBytesStr;

	/**
	 * 数字摘要算法
	 */
	private String hashCodeAlgorithm;

	/**
	 * hashCode显示编码格式
	 */
	private String type;

	/**
	 * 明文哈希值
	 */
	private String hashCode;

	/**
	 * 签名算法
	 */
	private String signatureAlgorithm;

	/**
	 * 数字签名
	 */
	private byte[] signature;

	/**
	 * 提示
	 */
	private String ps;

	public ce setDigest(Digest digest) {
		this.hashCodeAlgorithm = digest.getHashCodeAlgorithm();
		if (!digest.getType().equals("Base64")) this.ps = "new String(bytes)";
		this.type = digest.getType();
		return this;
	}

	public ce setDigitalCertificate(DigitalCertificate digitalCertificate) {
		this.signatureAlgorithm = digitalCertificate.getSignatureAlgorithm();
		this.signature = digitalCertificate.getSignature();
		return this;
	}

	/**
	 * @param ce 传入对象
	 * @return copy ce
	 */
	public ce copy(ce ce) {
		return (ce) DeepCopy.copy(ce);
	}
}
