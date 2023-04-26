package kazusa.ce;

import kazusa.ce.utils.AsymmetricEncryptionKey;
import kazusa.string.StringUtil;
import lombok.Getter;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 非对称加密对象
 * @author kazusa
 * @version 1.0.0
 */
@Getter
public class AsymmetricEncryption {

	/**
	 * 非对称加密密钥公钥
	 */
	private PublicKey publicKey;

	/**
	 * 非对称加密密钥私钥
	 */
	private PrivateKey privateKey;

	/**
	 * 非对称加密密钥
	 */
	private Key key;

	/**
	 * 加密/密钥协商算法
	 */
	private String algorithm;

	/**
	 * 支持的非对称加密密钥协商算法
	 */
	private String[] algorithms = {"DH","ECDH","ECDHE"};

	/**
	 * 加解密对象
	 */
	private Cipher cipher;

	/**
	 * @param asymmetricEncryptionkey 非对称加密密钥对象
	 * @throws NoSuchAlgorithmException
	 */
	public AsymmetricEncryption(AsymmetricEncryptionKey asymmetricEncryptionkey) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		this.algorithm = asymmetricEncryptionkey.getAlgorithm();
		KeyPair keyPair = asymmetricEncryptionkey.getKeyPair();
		// 生成公钥
		publicKey = keyPair.getPublic();
		// 生成私钥
		privateKey = keyPair.getPrivate();
		initCipher(asymmetricEncryptionkey);
	}

	/**
	 * 获取非对称加密算法密钥对
	 * @param algorithm 算法
	 * @param publicKey 公钥
	 * @param privateKey 私钥
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @since 1.1.0
	 */
	public AsymmetricEncryption(String algorithm, byte[] publicKey, byte[] privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		// 创建指定非对称加密算法的密钥工厂
		KeyFactory kf = KeyFactory.getInstance(algorithm);
		// 创建已编码的私钥规格
		this.publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKey));
		// 创建已编码的公钥规格
		this.privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
		initCipher(new AsymmetricEncryptionKey.AsymmetricEncryptionKeyBuilder(algorithm).build());
	}

	/**
	 * 初始化加解密对象
	 * @param asymmetricEncryptionkey 非对称加密密钥对象
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 */
	private void initCipher(AsymmetricEncryptionKey asymmetricEncryptionkey) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
		// 非密钥协商算法实例化加解密对象
		for (String algorithm : algorithms) {
			if (asymmetricEncryptionkey.getAlgorithm().equals(algorithm)) return;
		}
		if (StringUtil.isNull(asymmetricEncryptionkey.getProvider())) {
			cipher = Cipher.getInstance(asymmetricEncryptionkey.getAlgorithm());
		} else {
			cipher = Cipher.getInstance(asymmetricEncryptionkey.getAlgorithm(),asymmetricEncryptionkey.getProvider());
		}
	}

	public void setKey(Key key) {
		this.key = key;
	}

	/**
	 * 密钥协商实现密钥管理
	 * @param algorithm 算法
	 * @param publicKey 他人的公钥
	 * @return 会话密钥
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 */
	public byte[] getSecretKey(String algorithm,byte[] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
		// 生成本地密钥
		KeyAgreement keyAgreement = KeyAgreement.getInstance(algorithm);
		// 自己的PrivateKey
		keyAgreement.init(this.privateKey);
		// 对方的PublicKey:实例化密钥工厂将已编码公钥规格对象密钥恢复为PublicKey对象
		keyAgreement.doPhase(KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(publicKey)), true);
		// 生成SecretKey密钥
		return keyAgreement.generateSecret();
	}
}