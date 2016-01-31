package org.tondo.certimport;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.tondo.certimport.handlers.CertStoreResult;
import org.tondo.certimport.handlers.StoreCertificateChainHandler;
import org.tondo.certimport.handlers.StoringConfiguration;
import org.tondo.certimport.managers.AllTrustManager;
import org.tondo.certimport.managers.InterceptingX509Manager;

public class TrustedConnectionManager {
	
	/** Socket factory initialized by provided truststore */
	private SSLSocketFactory socketFactory;
	
	/** reference to loaded trustsore */
	private KeyStore trustStore;
	
	/** Tool for plug our custom certificate chain handler*/
	private InterceptingX509Manager interceptor;
	
	private static final SSLSocketFactory reliantSocketFactory;
	
	static {
		try {
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null, new TrustManager[] {new AllTrustManager()}, null);
			reliantSocketFactory = context.getSocketFactory();
		} catch (NoSuchAlgorithmException e) {
			throw new CertimportException("No such algorithm for SSL context", e);
		} catch (KeyManagementException e) {
			throw new CertimportException("SSL context initialization failed!", e);
		}
	}
	
	/**
	 * Creates https connection to URL location. Connection trust all remote sites, so it should 
	 * be used onlu for testing purposes or when you know what you are doing.
	 * @param location
	 * 	URL of resource to be manipulated
	 * @return
	 * 	{@link HttpsURLConnection} whit trust manager that accept all servers certificates
	 * 
	 * @throws CertimportException when cant create connection to specified location or if it is not 
	 * https connection
	 */
	public static HttpsURLConnection getTrustAllConnection(URL location) {
		// URLConnection objects go through two phases: first they are created, 
		// then they are connected. After being created, and before being connected, 
		// various options can be specified (e.g., doInput and UseCaches). After connecting, 
		// it is an error to try to set them. Operations that depend on being connected, 
		// like getContentLength, will implicitly perform the connection, if necessary.
		return getConnection(location, reliantSocketFactory);
	}
	
	/**
	 * Opens URL connection and enrich it with SSL socket factory.
	 * @param location
	 * 	should be https url of desired location to connect
	 * @param socketFactory
	 * 	ssl socket factory for encrich HTTPS connection
	 * @return connection initialized with trust materials from socket factory
	 * @throws HttpsURLConnection when something go wrong
	 * 
	 */
	private static HttpsURLConnection getConnection(URL location, SSLSocketFactory socketFactory) {
		URLConnection conn = null;
		try {
			conn = location.openConnection();
		} catch (IOException e) {
			throw new CertimportException("Can't open connection to " + location, e);
		}
		if (!(conn instanceof HttpsURLConnection)) {
			throw new CertimportException("Not HTTPS connection!");
		}
		
		HttpsURLConnection httpsConnection = (HttpsURLConnection)conn;
		httpsConnection.setSSLSocketFactory(socketFactory);
		return httpsConnection;
	}
	
	/**
	 * Construct trust connection manager initialized with keystore.
	 * This keystore will be used for initializing https connections.
	 * @param intput
	 * 	stream representing keystore
	 * @param password
	 * 	keystore password
	 */
	public TrustedConnectionManager(InputStream intput, char[] password) {
		try {
			this.trustStore = KeyStore.getInstance("jks");
			trustStore.load(intput, password);
			reloadContext(trustStore);
		} catch (Exception e) {
			throw new CertimportException("Trust Connection Manager can't be instantiated!", e);
		}
	}
	
	/**
	 * Create URL connection based on trustore used when this manager was created.
	 * @param location
	 * @return
	 * 	https connection for provided resource URL
	 */
	public HttpsURLConnection getConnection(URL location) {
		this.interceptor.setTrustedHandler(null);
		return getConnection(location, this.socketFactory);
	}
	
	
	public CertStoreResult addRootCertificate(URL location, String alias) throws IOException {
		StoringConfiguration conf = StoringConfiguration
			.builder()
				.setAlias(alias)
				.setOnlyIfMissing(true)
				.setOption(CertStoringOption.ROOT)
			.create();
		
		return addCertificate(location, conf);
	}
	
	public CertStoreResult addLeafCertificate(URL location, String alias) throws IOException {
		StoringConfiguration conf = StoringConfiguration
			.builder()
				.setAlias(alias)
				.setOnlyIfMissing(true)
				.setOption(CertStoringOption.LEAF)
			.create();
		
		return addCertificate(location, conf);
	}
	
	public CertStoreResult addCertificate(URL location, StoringConfiguration conf) throws IOException {
		StoreCertificateChainHandler handler = new StoreCertificateChainHandler(conf, trustStore);
		this.interceptor.setTrustedHandler(handler);
		
		HostResult hostAddress = new HostParser().parserHost(location);
		SSLSession session = null;
		// works only with IPv4??
		try (SSLSocket socket = (SSLSocket) this.socketFactory.createSocket( 
				Inet4Address.getByName(hostAddress.getHost()), hostAddress.getPort())) {
			socket.startHandshake();
			session = socket.getSession();
		}
		
		CertStoreResult result = handler.getResult();
		// handshake doesn't occurred because of ssl session
		// so invalidate session and try again
		if (result == null) {
			session.invalidate();
			try (SSLSocket socket = (SSLSocket) this.socketFactory.createSocket( 
					Inet4Address.getByName(hostAddress.getHost()), hostAddress.getPort())) {
				socket.startHandshake();
			}
			
			result = handler.getResult();
		}
		this.interceptor.setTrustedHandler(null);
		
		// if also second attempt  fails
		if (result == null) {
			throw new CertimportException("Can't initiate SSL handshake!");
		}
		
		if (result.getCertificatesAdded() > 0) {
			reloadContext(this.trustStore);
		}
		
		return result;
	}
	
	private void reloadContext(KeyStore store) {
		try {
			TrustManagerFactory managerFactory = TrustManagerFactory.getInstance("PKIX");
			managerFactory.init(store);
			TrustManager[] managers = managerFactory.getTrustManagers();

			// intercepting original X509 trust manager for possibility to
			// inject our code here
			for (int i = 0; i < managers.length; i++) {
				if (managers[i] instanceof X509TrustManager) {
					this.interceptor = new InterceptingX509Manager((X509TrustManager) managers[i]);
					managers[i] = this.interceptor;
				}
			}

			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null, managers, null);
			this.socketFactory = context.getSocketFactory();
		} catch (Exception e) {
			throw new CertimportException("Trust Connection Manager can't be instantiated!", e);
		}
	}

}
