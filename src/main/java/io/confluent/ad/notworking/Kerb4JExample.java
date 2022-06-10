package io.confluent.ad.notworking;

import com.kerb4j.client.SpnegoClient;
import com.kerb4j.client.SpnegoContext;
import io.confluent.ad.Config;
import org.ietf.jgss.GSSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivilegedActionException;

public class Kerb4JExample {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

       System.setProperty("java.security.krb5.conf", "src/main/resources/krb5.conf");
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("javax.security.auth.useSubjectCredsOnly","false");
        System.setProperty("java.security.debug", "gssloginconfig,configfile,configparser,logincontext");
        System.setProperty("java.security.auth.login.config", "src/main/resources/login.conf");

        SpnegoClient spnegoClient = SpnegoClient.loginWithUsernamePassword(Config.USERNAME+ Config.REALM, Config.PASSWORD);
        URL url = null;
        try {
            url = new URL("http://192.168.1.98:389");
            SpnegoContext context = spnegoClient.createContext(new URL(Config.LDAP_FULL_URL));
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestProperty("Authorization", context.createTokenAsAuthroizationHeader());
            LOG.info("Response code: " + huc.getResponseCode());
            LOG.info("Response message: " + huc.getResponseMessage());
            BufferedReader br;
            LOG.info("here");
            if (200 <= huc.getResponseCode() && huc.getResponseCode() <= 299) {
                br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(huc.getErrorStream()));
            }

            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            LOG.info(sb.toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (GSSException e) {
            throw new RuntimeException(e);
        } catch (PrivilegedActionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
