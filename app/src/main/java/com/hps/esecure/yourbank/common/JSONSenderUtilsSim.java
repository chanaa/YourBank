package com.hps.esecure.yourbank.common;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * The Class JSONSenderUtilsSim.
 */
public class JSONSenderUtilsSim {

	/** The Constant LOGGER. */

	public static final String	ESECURE_SIM_AREQ_URL	= "esecure.simulator.areq.url";

	/**
	 * Instantiates a new json sender utils.
	 */
	public JSONSenderUtilsSim() {
	}

	/**
	 * Send post request.
	 * 
	 * @param urlString
	 *            the url string
	 * @param jsonParam
	 *            the json message
	 * @return the string
	 */
	public String sendPostRequest(final String urlString, final String jsonParam) {
		HttpsURLConnection conn = null;
		String result = null;
		try {
			final byte[] postParameters = jsonParam.getBytes("UTF-8");

			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					// TODO Auto-generated method stub

				}
			} };

			SSLContext sc = SSLContext.getInstance("TLSv1"); // Create empty
																// HostnameVerifier
			System.setProperty("https.protocols", "TLSv1");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			HostnameVerifier hv = new HostnameVerifier() {
				@Override
				public boolean verify(String urlHostName, SSLSession session) {
					// LOGGER.warn("Warning: URL host '" + urlHostName +
					// "' is different to SSLSession host '" +
					// session.getPeerHost() + "'.");
					return true;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(hv);

			conn = (HttpsURLConnection) new URL(urlString).openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(postParameters.length));

			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.connect();
			// Send request
			final OutputStream outputStream = conn.getOutputStream();
			outputStream.write(postParameters);
			outputStream.close();

			// Get Response
			final InputStream is = conn.getInputStream();
			final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			final StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append("\n");
			}
			rd.close();
			is.close();

			result = response.toString();
			// LOGGER.debug("Response : {}", result);

		} catch (final IOException e) {
			Log.e("YOUR_BANK","Fail to send message", e);
		} catch (final Exception ex) {
			Log.e("YOUR_BANK","Fail to send message", ex);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}

}
