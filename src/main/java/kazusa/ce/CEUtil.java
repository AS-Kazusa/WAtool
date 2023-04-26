package kazusa.ce;

import cn.hutool.core.codec.Base64;
import kazusa.encoded.EncodedUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * 加密扩展
 * @author kazusa
 * @version 1.0.0
 * @see EncodedUtil
 */
public class CEUtil {

	/**
	 * 注册BouncyCastle
	 * @return 布尔值
	 */
	public static boolean addBC() {
		// -1表示依赖导入但无注册
		return Security.addProvider(new BouncyCastleProvider()) != -1;
	}

	/**
	 * 打印BouncyCastle支持算法
	 */
	public static void printProvider() {
		for (Provider.Service service : new BouncyCastleProvider().getServices()) {
			System.out.println(service.getType() + ": " + service.getAlgorithm());
		}
	}

	/**
	 * DH密钥交换
	 * @param algorithm 非对称加密密钥协商算法
	 * @param digest 数字摘要对象
	 * @param digitalCertificate 非对称加密签名对象
	 * @return ce传输对象
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 */
	public static ce keyNegotiation(AsymmetricEncryption algorithm,Digest digest,DigitalCertificate digitalCertificate) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		ce ce = new ce();
		byte[] key = algorithm.getPublicKey().getEncoded();
		ce.setEncryptionBytes(key);
		ce.setEncryptionBytesStr(Base64.encode(key));
		// 获取公钥hashCode值
		String hashCode = getDigest(new String(key),digest);
		ce.setHashCode(hashCode);
		// 数字签名
		ce hashCodeSignature = signature(hashCode.getBytes(),digitalCertificate);
		ce.setSignatureAlgorithm(hashCodeSignature.getSignatureAlgorithm());
		ce.setSignature(hashCodeSignature.getSignature());
		return ce.setDigest(digest);
	}

	/**
	 * DH密钥交换
	 * @param publicKeyBytes 他人公钥
	 * @param algorithm 非对称加密密钥协商算法
	 * @param keySize  对称加密的密钥长度
	 * @return 会话密钥
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidKeyException
	 */
	public static byte[] getSessionKey(byte[] publicKeyBytes,AsymmetricEncryption algorithm,int keySize) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
		// 当前对称加密算法支持最大密钥长度为32byte
		if (keySize > 32) throw new IllegalArgumentException("密钥过长,对称加密算法不支持");
		byte[] secretKey = algorithm.getSecretKey(algorithm.getAlgorithm(), publicKeyBytes);
		// 因为DH密钥最短512位,对称加密算法不支持如此长的密钥长度故截取一部分
		return Arrays.copyOfRange(secretKey,0,keySize);
	}

	/**
	 * 加密
	 * @param proclaimedWriting 明文/公钥
	 * @param digest 数字摘要对象
	 * @param digitalCertificate 非对称加密签名对象
	 * @param symmetry 对称加密对象
	 * @return ce传输对象
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 */
	public static ce encryption(byte[] proclaimedWriting,Digest digest,DigitalCertificate digitalCertificate,SymmetryEncryption symmetry) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		ce ce = new ce().setDigest(digest);
		// hashCode值
		ce.setHashCode(getDigest(new String(proclaimedWriting), digest));
		// 数字签名
		ce hashCodeSignature = signature(ce.getHashCode().getBytes(),digitalCertificate);
		ce.setSignatureAlgorithm(hashCodeSignature.getSignatureAlgorithm());
		ce.setSignature(hashCodeSignature.getSignature());
		byte[] bytes = symmetricEncryption(proclaimedWriting, symmetry);
		ce.setEncryptionBytes(bytes);
		ce.setEncryptionBytesStr(Base64.encode(bytes));
		return ce;
	}

	/**
	 * 解密
	 * @param ce ce传输对象
	 * @param digitalCertificate 非对称加密签名对象
	 * @param symmetry 对称加密对象
	 * @param digest 数字摘要对象
	 * @return 明文或警告
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 */
	public static byte[] deciphering(ce ce,DigitalCertificate digitalCertificate,SymmetryEncryption symmetry,Digest digest) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, SignatureException {
		// 验签
		digitalCertificate.setSignatureAlgorithm(ce.getSignatureAlgorithm());
		digitalCertificate.setSignature(ce.getSignature());
		if (!isSignature(ce.getHashCode(),digitalCertificate)) throw new IllegalBlockSizeException("数据被劫持过!!!");
		// 解密
		byte[] bytes = symmetricDeciphering(ce.getEncryptionBytes(),symmetry);
		digest.setHashCodeAlgorithm(ce.getHashCodeAlgorithm());
		digest.setType(ce.getType());
		// 校验HashCode值是否一致
		if (!ce.getHashCode().equals(getDigest(new String(bytes),digest))) throw new IllegalBlockSizeException("数据被劫持过!!!");
		return bytes;
	}

	/**
	 * 加解密对象
	 */
	private static Cipher cipher;

	/**
	 * 密钥
	 */
	private static SecretKey key;

	/**
	 * 对称加密
	 * @param proclaimedWriting 明文
	 * @param symmetry 对称加密对象
	 * @return 密文
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] symmetricEncryption(byte[] proclaimedWriting,SymmetryEncryption symmetry) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		/*
	        传入加密算法,加解密模式,填充/算法获取加密对象
	        只传入算法默认使用ECB加密模式且填充(PKCS5adding)
	        不填充使用NoPadding但明文字节数需为8的整数倍
        */
		cipher = symmetry.getCipher();
		// 传入密钥和算法生成key对象
		key = new SecretKeySpec(symmetry.getKey(), symmetry.getAlgorithm());
		// 判断所选加解密模式是否需要传入IV向量
		for (String model : symmetry.getModels()) {
			if (symmetry.getModel().equals(model)) {
				cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(symmetry.getIv()));
				return cipher.doFinal(proclaimedWriting);
			}
		}
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(proclaimedWriting);
	}

	/**
	 * 对称加密解密
	 * @param ciphertext 密文
	 * @param symmetry 对称加密对象
	 * @return 明文
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] symmetricDeciphering(byte[] ciphertext,SymmetryEncryption symmetry) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
		cipher = symmetry.getCipher();
		key = new SecretKeySpec(symmetry.getKey(),symmetry.getAlgorithm());
		// 判断所选加解密模式是否需要传入IV向量
		for (String model : symmetry.getModels()) {
			if (symmetry.getModel().equals(model)) {
				cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(symmetry.getIv()));
				return cipher.doFinal(ciphertext);
			}
		}
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(ciphertext);
	}

	/**
	 * 非对称加密
	 * @param proclaimedWriting 明文
	 * @param asymmetric 非对称加密对象
	 * @return 密文
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] asymmetricEncryption(byte[] proclaimedWriting,AsymmetricEncryption asymmetric) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		/*
	        传入加密算法,加解密模式,填充/算法获取加密对象
	        只传入算法默认使用ECB加密模式且填充(PKCS5adding)
	        不填充使用NoPadding但明文字节数需为8的整数倍
        */
		cipher = asymmetric.getCipher();
		// 加密初始化:传入模式,密钥
		cipher.init(Cipher.ENCRYPT_MODE,asymmetric.getKey());
		// 加密
		return cipher.doFinal(proclaimedWriting);
	}

	/**
	 * 非对称加密解密
	 * @param ciphertext 密文
	 * @param asymmetric 非对称加密对象
	 * @return 明文
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] asymmetricDeciphering(byte[] ciphertext,AsymmetricEncryption asymmetric) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
		// 获取同一个cipher对象
		cipher = asymmetric.getCipher();
		// 解密初始化:传入模式,密钥
		cipher.init(Cipher.DECRYPT_MODE, asymmetric.getKey());
		// 解密
		return cipher.doFinal(ciphertext);
	}

	/**
	 * 消息/数字摘要:哈希值
	 * @param proclaimedWriting 明文
	 * @param digest 数字摘要对象
	 * @throws NoSuchAlgorithmException
	 */
	public static String getDigest(String proclaimedWriting,Digest digest) throws NoSuchAlgorithmException {
		// 得到数组形式的hashCode值并根据指定编码返回字符串hashCode值
		return EncodedUtil.encoded(MessageDigest.getInstance(digest.getHashCodeAlgorithm()).digest(proclaimedWriting.getBytes()),digest);
	}

	/**
	 * hashCode加解密对象
	 */
	private static Mac mac;

	/**
	 * 消息/数字摘要:哈希值加解密
	 * @param proclaimedWriting 明文
	 * @param digest 数字摘要对象
	 */
	public static String digestEncryptionDeciphering(String proclaimedWriting,Digest digest) {
		mac = digest.getMac();
		// 调用update读入信息的字节码
		mac.update(proclaimedWriting.getBytes());
		// 加解密
		return EncodedUtil.encoded(mac.doFinal(),digest);
	}

	/**
	 * 签名对象
	 */
	private static Signature signature;

	/**
	 * @param digest 明文/密钥的hashCode
	 * @param digitalCertificate 非对称加密签名对象
	 * @return ce传输对象
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 */
	public static ce signature(byte[] digest,DigitalCertificate digitalCertificate) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		// 传入签名加密算法获取签名对象
		signature = Signature.getInstance(digitalCertificate.getSignatureAlgorithm());
		// 使用私钥初始化签名
		signature.initSign(digitalCertificate.getPrivateKey());
		// 传入明文hashCode
		signature.update(digest);
		// 生成数字签名
		digitalCertificate.setSignature(signature.sign());
		return new ce().setDigitalCertificate(digitalCertificate);
	}

	/**
	 * 验签
	 * @param hashCode 明文的hashCode值
	 * @param digitalCertificate 非对称加密签名对象
	 * @return 布尔值
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public static boolean isSignature(String hashCode,DigitalCertificate digitalCertificate) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		// 传入签名加密算法获取签名对象
		signature = Signature.getInstance(digitalCertificate.getSignatureAlgorithm());
		// 使用公钥初始化校验
		signature.initVerify(digitalCertificate.getPublicKey());
		// 传入明文的hashCode值
		signature.update(hashCode.getBytes());
		// 校验签名
		return signature.verify(digitalCertificate.getSignature());
	}
}