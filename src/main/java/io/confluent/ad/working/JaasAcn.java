package io.confluent.ad.working;

import javax.security.auth.login.*;

import com.sun.security.auth.callback.TextCallbackHandler;
import io.confluent.ad.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * This io.confluent.ad.working.JaasAcn application attempts to authenticate a user
 * and reports whether or not the authentication was successful.
 */
public class JaasAcn {

    /**
     * java -Djava.security.krb5.realm=<your_realm>
     * -Djava.security.krb5.kdc=<your_kdc>
     * -Djava.security.auth.login.config=jaas.conf io.confluent.ad.working.JaasAcn
     *
     * @param args
     */

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        System.setProperty("java.security.krb5.realm", "AD-TEST.CONFLUENT.IO");
        System.setProperty("java.security.krb5.kdc", Config.IP_ADDR);
        System.setProperty("java.security.auth.login.config", "src/main/resources/jaas.conf");

        // Obtain a LoginContext, needed for authentication. Tell
        // it to use the LoginModule implementation specified by
        // the entry named "JaasSample" in the JAAS login
        // configuration file and to also use the specified
        // CallbackHandler.
        LoginContext lc = null;
        try {
            lc = new LoginContext("JaasSample", new TextCallbackHandler());
        } catch (LoginException le) {
            LOG.error("Cannot create LoginContext. " + le.getMessage());
            System.exit(-1);
        } catch (SecurityException se) {
            LOG.error("Cannot create LoginContext. " + se.getMessage());
            System.exit(-1);
        }

        try {
            lc.login();
        } catch (LoginException le) {
            LOG.error("Authentication failed: " + le.getMessage());
            System.exit(-1);
        }
        LOG.info("Authentication success...");
    }
}