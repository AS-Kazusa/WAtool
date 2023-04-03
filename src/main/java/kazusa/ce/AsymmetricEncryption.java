package kazusa.ce;

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
	 * @param algorithm 非对称加密算法
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 */
	public AsymmetricEncryption(String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException {
		this(algorithm,-1);
	}

	/**
	 * @param algorithm 非对称加密算法
	 * @param keySize 密钥长度
	 * @throws NoSuchAlgorithmException
	 */
	public AsymmetricEncryption(String algorithm,int keySize) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.algorithm = algorithm;
		initKey(keySize);
		// 非密钥协商算法实例化加解密对象
		for (String s : algorithms) {
			if (algorithm.equals(s)) return;
		}
		cipher = Cipher.getInstance(algorithm);
	}

	/**
	 * 获取非对称加密密钥
	 * @param keySize 密钥长度
	 * @throws NoSuchAlgorithmException
	 */
	public void initKey(int keySize) throws NoSuchAlgorithmException {
		// 实例化密钥对生成器
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
		// 传入密钥长度,根据密钥长度选择算法版本
		if (keySize != -1) keyPairGenerator.initialize(keySize);
		// 生成密钥对
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		// 生成私钥
		privateKey = keyPair.getPrivate();
		// 生成公钥
		publicKey = keyPair.getPublic();
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
		// 实例化密钥工厂将包装成X509EncodedKeySpec对象密钥恢复为PublicKey对象
		PublicKey receivedPublicKey = KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(publicKey));
		// 生成本地密钥
		KeyAgreement keyAgreement = KeyAgreement.getInstance(algorithm);
		// 自己的PrivateKey
		keyAgreement.init(this.privateKey);
		// 对方的PublicKey
		keyAgreement.doPhase(receivedPublicKey, true);
		// 生成SecretKey密钥
		return keyAgreement.generateSecret();
	}

	@Deprecated
	private void type() throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] privateKeyBytes = new byte[0];
		byte[] publicKeyBytes = new byte[0];
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
		PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
	}
}