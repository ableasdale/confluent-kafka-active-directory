package io.confluent.ad.working;

import io.confluent.ad.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.*;
import javax.naming.ldap.*;
import javax.naming.directory.*;
import java.lang.invoke.MethodHandles;
import java.util.Hashtable;

public class Ldap
{
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static void main(String[]args)
    {
        Hashtable<String, String> environment = new Hashtable<String, String>();

        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, Config.LDAP_FULL_URL);
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.SECURITY_PRINCIPAL, Config.USERNAME+Config.REALM);
        environment.put(Context.SECURITY_CREDENTIALS, Config.PASSWORD);

        try
        {
            DirContext context = new InitialDirContext(environment);
            LOG.info("Connected..");
            LOG.info("Env: "+context.getEnvironment());
            context.close();
        }
        catch (AuthenticationNotSupportedException exception)
        {
            System.out.println("The authentication is not supported by the server");
        }

        catch (AuthenticationException exception)
        {
            System.out.println("Incorrect password or username");
        }

        catch (NamingException exception)
        {
            System.out.println("Error when trying to create the context");
        }
    }
}
