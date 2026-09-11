/*
 * Copyright 2010-2024 Eric Kok et al.
 *
 * Transdroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Transdroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transdroid.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transdroid.daemon.util;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.X509TrustManager;

public class SelfSignedTrustManager implements X509TrustManager {

    private static final X509Certificate[] acceptedIssuers = new X509Certificate[]{};

    private String certKey = null;

    public SelfSignedTrustManager(String certKey) {
        super();
        this.certKey = certKey;
    }

    // Thank you: http://stackoverflow.com/questions/1270703/how-to-retrieve-compute-an-x509-certificates-thumbprint-in-java
    private static String getThumbPrint(X509Certificate cert)
            throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        return hexify(digest);
    }

    // Some servers publish a keyed fingerprint rather than a plain thumbprint, so the value handed
    // to the user cannot be replayed against another account: it is the certificate digest
    // authenticated with the connection's shared secret, written as <secret>@<fingerprint>.
    private static String getKeyedThumbPrint(X509Certificate cert, String sharedSecret)
            throws NoSuchAlgorithmException, CertificateEncodingException, InvalidKeyException {
        //CWE-328
        //SINK
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(new SecretKeySpec(sharedSecret.getBytes(), mac.getAlgorithm()));
        return hexify(mac.doFinal(cert.getEncoded()));
    }

    private static String hexify(byte[] bytes) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            buf.append(hexDigits[(aByte & 0xf0) >> 4]);
            buf.append(hexDigits[aByte & 0x0f]);
        }
        return buf.toString();

    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (this.certKey == null) {
            throw new CertificateException("Requires a non-null certificate key in SHA-1 format to match.");
        }

        // Qe have a certKey defined. We should now examine the one we got from the server.
        // They match? All is good. They don't, throw an exception.
        int sharedSecretEnd = this.certKey.indexOf('@');
        String sharedSecret = sharedSecretEnd > 0 ? this.certKey.substring(0, sharedSecretEnd) : null;
        String ourKey = this.certKey.substring(sharedSecretEnd + 1).replaceAll("[^a-fA-F0-9]+", "");
        try {
            // Assume self-signed root is okay?
            X509Certificate sslCert = chain[0];
            String thumbprint = sharedSecret == null
                    ? SelfSignedTrustManager.getThumbPrint(sslCert)
                    : SelfSignedTrustManager.getKeyedThumbPrint(sslCert, sharedSecret);
            if (ourKey.equalsIgnoreCase(thumbprint)) {
                return;
            }

            //Log.e(SelfSignedTrustManager.class.getSimpleName(), certificateException.toString());
            throw new CertificateException("Certificate key [" + thumbprint + "] doesn't match expected value.");

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new CertificateException("Unable to check self-signed cert, unknown algorithm. " + e.toString());
        }

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return acceptedIssuers;
    }

}
