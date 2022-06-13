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
        //System.setProperty("javax.security.auth.useSubjectCredsOnly","false");
        System.setProperty("java.security.debug", "gssloginconfig,configfile,configparser,logincontext");
        System.setProperty("java.security.auth.login.config", "src/main/resources/login.conf");

        SpnegoClient spnegoClient = SpnegoClient.loginWithKeyTab("kafka/WIN-7F40HNU7OPJ@AD-TEST.CONFLUENT.IO", "src/main/resources/kafka.keytab");
                //loginWithUsernamePassword(Config.USERNAME, Config.PASSWORD);
        URL url = null;
        try {
            url = new URL("http://www.example.com");
            /* At the moment, I think this one doesn't really work because we're creating a context for
             * something that doesn't really exist - looking through the output, it does look like the pre-auth
             * does take place with the KDC - but the subsequent request to example.com seems to be where the
             * failure happens (example.com has no understanding of the pre-auth context).
             * I think this example could be made to work given a legitimate endpoint
             */
            /* Update
            Confirmed this is likely to be the problem:
            >>>KRBError:
                 sTime is Mon Jun 13 16:12:00 BST 2022 1655133120000
                 suSec is 884952
                 error code is 7
                 error Message is Server not found in Kerberos database
                 sname is HTTP/www.example.com@AD-TEST.CONFLUENT.IO

	            In my case, there's no www.example.com in my domain, so the request is rejected.
	            If this were a real resource in the domain, this process should work as expected.
             */
            SpnegoContext context = spnegoClient.createContext(url);
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
