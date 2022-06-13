package io.confluent.ad.working;

import io.confluent.ad.Config;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Set;

public class HttpClientKerberosDoAsTwo {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) throws Exception {

        System.setProperty("java.security.auth.login.config", "src/main/resources/login.conf");
        System.setProperty("java.security.krb5.kdc", Config.IP_ADDR);
        System.setProperty("java.security.krb5.realm", "AD-TEST.CONFLUENT.IO");
        String user = Config.USERNAME;
        String password = Config.PASSWORD;
        String url = "http://www.example.com";

        HttpClientKerberosDoAsTwo kcd = new HttpClientKerberosDoAsTwo();

        LOG.info("Logging in with user [" + user + "]");
        kcd.test(user, password, url);

    }

    public void test(String user, String password, final String url) {
        try {

            LoginContext loginCOntext = new LoginContext("KrbLogin", new KerberosCallBackHandler(user, password));
            loginCOntext.login();

            PrivilegedAction sendAction = new PrivilegedAction() {

                @Override
                public Object run() {
                    try {

                        Subject current = Subject.getSubject(AccessController.getContext());
                        LOG.info("----------------------------------------");
                        Set<Principal> principals = current.getPrincipals();
                        for (Principal next : principals) {
                            LOG.info("Principal: " + next.getName());
                        }
                        LOG.info("----------------------------------------");

                        call(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            };

            Subject.doAs(loginCOntext.getSubject(), sendAction);

        } catch (LoginException le) {
            le.printStackTrace();
        }
    }

    private void call(String url) throws IOException {
        HttpClient httpclient = getHttpClient();

        try {

            HttpUriRequest request = new HttpGet(url);
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            LOG.info("----------------------------------------");

            LOG.info("STATUS >> " + response.getStatusLine());

            if (entity != null) {
                LOG.info("RESULT >> " + EntityUtils.toString(entity));
            }

            LOG.info("----------------------------------------");

            EntityUtils.consume(entity);

        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

    private HttpClient getHttpClient() {

        Credentials use_jaas_creds = new Credentials() {
            public String getPassword() {
                return null;
            }

            public Principal getUserPrincipal() {
                return null;
            }
        };

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(null, -1, null), use_jaas_creds);
        Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create().register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultAuthSchemeRegistry(authSchemeRegistry).setDefaultCredentialsProvider(credsProvider).build();

        return httpclient;
    }

    class KerberosCallBackHandler implements CallbackHandler {

        private final String user;
        private final String password;

        public KerberosCallBackHandler(String user, String password) {
            this.user = user;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

            for (Callback callback : callbacks) {

                if (callback instanceof NameCallback) {
                    NameCallback nc = (NameCallback) callback;
                    nc.setName(user);
                } else if (callback instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback) callback;
                    pc.setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callback, "Unknown Callback");
                }

            }
        }
    }

}