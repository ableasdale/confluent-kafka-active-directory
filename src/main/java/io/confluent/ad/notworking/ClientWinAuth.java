package io.confluent.ad.notworking;

import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.win.WinHttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * This example demonstrates how to create HttpClient pre-configured
 * with support for integrated Windows authentication.
 */
public class ClientWinAuth {

    // https://stackoverflow.com/questions/5804314/simple-kerberos-client-in-java

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static void main(String[] args) throws Exception {

        System.setProperty("java.security.krb5.conf", "src/main/resources/krb5.conf");
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("javax.security.auth.useSubjectCredsOnly","false");
        System.setProperty("java.security.debug", "gssloginconfig,configfile,configparser,logincontext");
        System.setProperty("java.security.auth.login.config", "src/main/resources/login.conf");

        if (!WinHttpClients.isWinAuthAvailable()) {
            LOG.warn("Integrated Win auth is not supported.");
        }

        CloseableHttpClient httpclient = WinHttpClients.createDefault();
        //AuthUtils.securityLogging(SecurityLogType.KERBEROS,true);
       //CredentialsUtils.setKerberosCredentials(httpclient, new UsernamePasswordCredentials("xxx", "xxx"), "domain", "kdc");
        //httpclient.executeMethod(httpget);
        // There is no need to provide user credentials
        // HttpClient will attempt to access current user security context through
        // Windows platform specific methods via JNI.
        try {
            HttpGet httpget = new HttpGet("http://192.168.1.77:389");

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                EntityUtils.consume(response.getEntity());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

}