package kazusa.ce;

import kazusa.io.IOUtil;
import lombok.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * 非对称签名加密对象,java只提供RSA签名
 * @author kazusa
 * @version 1.0.0
 */
@Data
public class DigitalCertificate {

	private String signatureAlgorithm;

	private PublicKey publicKey;

	private PrivateKey privateKey;

	/**
	 * 数字签名
	 */
	private byte[] signature;

	/**
	 * 签名算法根据非对称加密算法决定:即签名算法为指定某种哈希算法进行非对称加密签名方式
	 * @param signatureAlgorithm 签名算法
	 * @param publicKey 公钥
	 * @param privateKey 私钥
	 */
	public DigitalCertificate(String signatureAlgorithm,PublicKey publicKey, PrivateKey privateKey) {
		this.signatureAlgorithm = signatureAlgorithm;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	/**
	 * <a href="https://www.liaoxuefeng.com/wiki/1252599548343744/1304227968188450">生成数字证书</a>
	 * 解析证书
	 * @param path 证书路径
	 * @param file 证书
	 * @param password 证书密码
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 */
	public DigitalCertificate(String path,String file,String password) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
		// 如果不存在此类属性，则返回由 keystore.type安全属性指定的默认密钥库类型,或字符串"jks"("Java密钥库"的首字母缩写)
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		// 读取KeyStore
		ks.load(IOUtil.getInputStream(path),password.toCharArray());
		// 读取证书
		X509Certificate x509Certificate = (X509Certificate) ks.getCertificate(file);
		this.signatureAlgorithm = x509Certificate.getSigAlgName();
		this.publicKey = x509Certificate.getPublicKey();
		// 读取私钥
		this.privateKey = (PrivateKey) ks.getKey(file, password.toCharArray());
	}

	/**
	 * 解析证书
	 * @param certFile 证书文件对象
	 */
	@Deprecated
	public DigitalCertificate(File certFile) throws CertificateException, NoSuchProviderException, FileNotFoundException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
		X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(new FileInputStream(certFile));
		this.signatureAlgorithm = x509Certificate.getSigAlgName();
		this.publicKey = x509Certificate.getPublicKey();
		// 读取私钥
		//this.privateKey
	}

	/**
	 * @return 基于RSA的签名算法
	 */
	public static List<String> getRSA_SIGNATURE() {
		List<String> RSA_SIGNATURE = new ArrayList<>();
		RSA_SIGNATURE.add("MD5withRSA");
		RSA_SIGNATURE.add("SHA1withRSA");
		RSA_SIGNATURE.add("sha256withrsa");
		return RSA_SIGNATURE;
	}

	/**
	 * @return 基于DSA的ElGamal数字签名算法,哈希算法上只能搭配SHA使用,比RSA签名更快
	 */
	public static List<String> getDSA_SIGNATURE() {
		List<String> DSA_SIGNATURE = new ArrayList<>();
		DSA_SIGNATURE.add("SHA1withDSA");
		DSA_SIGNATURE.add("SHA256withDSA");
		DSA_SIGNATURE.add("SHA512withDSA");
		return DSA_SIGNATURE;
	}

	/**
	 * @return 基于椭圆曲线签名算法ECDSA,特点是可以从私钥推出公钥
	 */
	public static String getECDSA_SIGNATURE() {
		return "ECDSA";
	}
}
