package kazusa.ce;

import lombok.Getter;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

/**
 * 对称加密对象
 * <a href="https://www.lddgo.net/encrypt/rc6">RC6加解密网站</a>
 * @author kazusa
 * @version 1.0.0
 */
@Getter
public class SymmetryEncryption {

	/**
	 * 对称加密密钥
	 */
	private byte[] key;

	/**
	 * 加密算法:默认使用AES
	 */
	private String algorithm;

	/**
	 * 加解密模式:默认CTR
	 */
	private String model;

	/**
	 * 需要IV向量的加解密模式
	 */
	private String[] models = {"CBC","CTR","CFB"};

	/**
	 * 明文长度填充参数:默认使用PKCS5Padding填充
	 * nopadding:不填充,明文字节长度需为16的倍数
	 * PKCS5Padding:填充,对明文字节长度无要求,为7的子集,固定块的大小为8字节
	 * PKCS7Padding:填充,对明文字节长度无要求
	 */
	private String fillParameters;

	/**
	 * 加密指定的算法,模式,填充参数
	 * 算法	密钥长度	工作模式	填充模式
	 * DES	56/64	ECB/CBC/PCBC/CTR/...	NoPadding/PKCS5Padding/...
	 * RC5
	 * IDEA	128	ECB	PKCS5Padding/PKCS7Padding/...
	 * RC6 128
	 * SM4 128
	 * AES	128/192/256	ECB/CBC/PCBC/CTR/...	NoPadding/PKCS5Padding/PKCS7Padding/...
	 * 序列算法:
	 * 序列zuc祖冲之
	 */
	private String parameter;

	/**
	 * iv向量:用作增强加密
	 */
	private byte[] iv = "1234567812345678".getBytes();;

	/**
	 * 加解密对象
	 */
	private Cipher cipher;

	/**
	 * @param key 密钥
	 * @param algorithm 算法
	 */
	public SymmetryEncryption(byte[] key, String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException {
		this(key,algorithm,"CTR");
	}

	/**
	 * @param key 密钥
	 * @param algorithm 算法
	 * @param model 加解密模式
	 */
	public SymmetryEncryption(byte[] key, String algorithm, String model) throws NoSuchPaddingException, NoSuchAlgorithmException {
		this(key,algorithm,model,"PKCS5Padding");
	}

	/**
	 * @param key 密钥
	 * @param algorithm 算法
	 * @param model 加解密模式
	 * @param fillParameters 填充参数
	 */
	public SymmetryEncryption(byte[] key, String algorithm, String model, String fillParameters) throws NoSuchPaddingException, NoSuchAlgorithmException {
		this.key = key;
		this.algorithm = algorithm;
		this.model = model;
		this.fillParameters = fillParameters;
		parameter = algorithm + "/" + model + "/" + fillParameters;
		cipher = Cipher.getInstance(parameter);
	}

	public void setIv(byte[] iv) {
		this.iv = iv;
	}
}
