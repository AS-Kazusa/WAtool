package kazusa.ce.utils;

import kazusa.common.SyntacticSugar;
import kazusa.string.StringUtil;
import lombok.Getter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * 非对称加密密钥对象
 * @author kazusa
 */
@Getter
public class AsymmetricEncryptionKey {

	/**
	 * 算法
	 */
	private String algorithm;

	/**
	 * 密钥长度
	 */
	private int keySize;

	/**
	 * 供应者
	 */
	private String provider;

	/**
	 * 随机源
	 */
	private SecureRandom random;

	public AsymmetricEncryptionKey(String algorithm, int keySize, String provider, SecureRandom random) {
		this.algorithm = algorithm;
		this.keySize = keySize;
		this.provider = provider;
		this.random = random;
	}

	public static class AsymmetricEncryptionKeyBuilder {

		private String algorithm;

		private int keySize = -1;

		private String provider;

		private SecureRandom random;

		public AsymmetricEncryptionKeyBuilder(String algorithm) {
			this.algorithm = algorithm;
		}

		public AsymmetricEncryptionKeyBuilder keySize(int keySize) {
			this.keySize = keySize;
			return this;
		}

		public AsymmetricEncryptionKeyBuilder provider(String provider) {
			this.provider = provider;
			return this;
		}

		public AsymmetricEncryptionKeyBuilder random(SecureRandom random) {
			this.random = random;
			return this;
		}

		public AsymmetricEncryptionKey build() {
		    return new AsymmetricEncryptionKey(algorithm, keySize,provider,random);
		}
	}

	/**
	 * @return 生成密钥对
	 */
	public KeyPair getKeyPair() {
		KeyPairGenerator keyPairGenerator = SyntacticSugar.tryCatch(() ->
			StringUtil.isNull(this.provider) ? KeyPairGenerator.getInstance(this.algorithm) : KeyPairGenerator.getInstance(this.algorithm, this.provider));
		if (keySize != -1) {
			if (Objects.isNull(random)) {
				keyPairGenerator.initialize(keySize);
			} else {
				keyPairGenerator.initialize(keySize,random);
			}
		}
		return keyPairGenerator.generateKeyPair();
	}
}