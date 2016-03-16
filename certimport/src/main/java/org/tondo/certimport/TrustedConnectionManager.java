package org.tondo.certimport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

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

/**
 * TrustConnectionManager is used for querying trust state of SSL locations and adding
 * sites certificate to trustore. This manager initialize internal SSL context with
 * trust managers loaded from provided KeyStore. Changes to internal state of trustore is not
 * reflected directly in file, but are only kept in memory while exlicit save is invoked (TODO).
 * 
 * @author TondoDev
 *
 */
public class TrustedConnectionManager {
	
	/** Socket factory initialized by provided truststore */
	private SSLSocketFactory socketFactory;
	
	/** reference to loaded trustsore */
	private KeyStore trustStore;
	
	/** Tool for plug our custom certificate chain handler*/
	private InterceptingX509Manager interceptor;
	
	/** internal sockect factory for creating socket which trust all target locations */
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
	
	/**
	 * Add root certificate of ssl location from provided certificate chain. Root certificate
	 * is usually self signed. Trusting root certificate means also trusting all other certificates
	 * which are signed by root. If site is already trusted, new certificate is not added again.
	 * For different behavior use {@link #addCertificate(URL, StoringConfiguration)} with requested configuration.
	 * 
	 * @param location
	 * 	url location of ssl site
	 * @param alias
	 * 	used for storing this certificate in trustore. If same alias is found in trusted certificate
	 * 	section, it is overwritten, but if same alias exists in other section (for example secret key) exception is thrown.
	 * @return
	 * 	returns summary about adding process
	 * @throws IOException
	 */
	public CertStoreResult addRootCertificate(URL location, String alias) throws IOException {
		StoringConfiguration conf = StoringConfiguration
			.builder()
				.setAlias(alias)
				.setAddEvenIfTrusted(false)
				.setOption(CertStoringOption.ROOT)
			.create();
		
		return addCertificate(location, conf);
	}
	
	/**
	 * Add lef certificate of ssl location from provided certificate chain.
	 * Leaf certificate belongs to specific organization or site which is issued by some of
	 * higher level CA. 
	 * If site is already trusted, new certificate is not added again.
	 * For different behavior use {@link #addCertificate(URL, StoringConfiguration)} with requested configuration.
	 * 
	 * @param location
	 * 	url location of ssl site
	 * @param alias
	 * 	used for storing this certificate in trustore. If same alias is found in trusted certificate
	 * 	section, it is overwritten, but if same alias exists in other section (for example secret key) exception is thrown.
	 * @return
	 * 	returns summary about adding process
	 * @throws IOException
	 */
	public CertStoreResult addLeafCertificate(URL location, String alias) throws IOException {
		StoringConfiguration conf = StoringConfiguration
			.builder()
				.setAlias(alias)
				.setAddEvenIfTrusted(false)
				.setOption(CertStoringOption.LEAF)
			.create();
		
		return addCertificate(location, conf);
	}
	
	/**
	 * Add certificate of provided https location to trustore. Added certificate is not persisted but
	 * it is keep in context of this trust manager.
	 * @param location
	 * 	location of site which certificate we want to add
	 * @param conf
	 * 	settings for adding process. With this settings can be configured alias, or forced add if certificate is already trusted etc. See
	 * 	{@link StoringConfiguration}} for more details
	 * @return
	 * 	result summary about adding process.
	 * @throws IOException
	 */
	public CertStoreResult addCertificate(URL location, StoringConfiguration conf) throws IOException {
		StoreCertificateChainHandler handler = new StoreCertificateChainHandler(conf, trustStore);
		this.interceptor.setTrustedHandler(handler);
		
		HostResult hostAddress = new HostParser().parserHost(location);
		SSLSession session = null;
		// works only with IPv4??
		try (SSLSocket socket = (SSLSocket) this.socketFactory.createSocket( 
				Inet4Address.getByName(hostAddress.getHost()), hostAddress.getPort())) {
			socket.setSoTimeout(10000);
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
	
	/**
	 * Check if given location is trusted by this trust connection manager (or underlying trustore).
	 * Trust internal state of trust mananger is not modified by this call.
	 * @param location
	 * 	location of site to investigate
	 * @return
	 * 	summary about verification process. If site is trusted then {@link CertStoreResult#getMatchingAlias()} and {@link CertStoreResult#getMatchingCertificate()}
	 *  returns corresponding values which entry in trustore make this location as trusted. It both these values are null (should never happend
	 *  that one value is null and one not),
	 *  site is not trusted
	 * @throws IOException 
	 */
	public CertStoreResult checkIfTrusted(URL location) throws IOException {
		StoringConfiguration conf = StoringConfiguration
				.builder()
					.setOption(CertStoringOption.DONT_ADD)
				.create();
			
		return addCertificate(location, conf);
	}
	
	/**
	 * Saves trustore used by this trust manager. After this call, all added
	 * certificates are passed to provided output stream.
	 * @param output
	 * 	stream where trustore will be written
	 * @param pwd
	 * 	password for check integrity (same as {@link KeyStore#store(OutputStream, char[])}}
	 */
	public void save(OutputStream output, char[] pwd){
		try {
			this.trustStore.store(output, pwd);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new CertimportException("Trust store can't be saved!", e);
		}
	}
	
	/**
	 * Reload internal SSL context with provided keystore.
	 * Context is initialized only with trustManagers, random generator and key managers are set as null.
	 * @param store
	 * 	keystore containing trusted certificates
	 */
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
