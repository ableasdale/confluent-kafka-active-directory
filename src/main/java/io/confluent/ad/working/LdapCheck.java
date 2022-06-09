package io.confluent.ad.working;

import io.confluent.ad.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.lang.invoke.MethodHandles;
import java.util.Hashtable;

public class LdapCheck {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) throws NamingException {

        LdapContext ldapctx;
        DirContext ldapDirContext;
        String searchBase;
        //String adminName = "CN=bind u. user,CN=Users,DC=domain,DC=com";

        //String adminPassword = "Password1";

//create an initial directory context

        Hashtable<String,String> env = new Hashtable<String,String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.REFERRAL, "ignore");
// If you omit this property, the application will wait indefinitely.
        env.put("com.sun.jndi.ldap.connect.timeout", "300000000");
        env.put(Context.PROVIDER_URL, "ldap://192.168.1.98:389");
//For password change use env.put(Context.PROVIDER_URL, "ldaps://adserver:636");
       // env.put(Context.SECURITY_PRINCIPAL, Config.USERNAME);
        env.put(Context.SECURITY_CREDENTIALS, Config.PASSWORD);
        env.put(Context.SECURITY_PRINCIPAL, Config.USERNAME+Config.REALM);
        //env.put("java.naming.ldap.attributes.binary", "tokenGroups objectSid objectGUID");
        ldapctx = new InitialLdapContext(env, null);
        ldapDirContext = new InitialDirContext(env);

// Create the search controls
        SearchControls searchCtls = new SearchControls();

//Specify the attributes to return
        String[] returnedAtts ={"sn","givenName","samAccountName","accountExpires","badPwdCount","userAccountControl","objectGUID","lockoutThreshold","lockoutDuration","AccountExpirationDate"};
            searchCtls.setReturningAttributes(returnedAtts);
//Specify the search scope
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    //specify the LDAP search filter
//String searchFilter = "(&(objectClass=user))";   (or)
    String searchFilter = "cn=*";

//Specify the Base for the search
    searchBase = "cn=Users,dc=ad-test,dc=confluent,dc=io";
    //initialize counter to total the results
    int totalResults = 0;
    // Search for objects using the filter
    NamingEnumeration<SearchResult> answer = ldapDirContext.search(searchBase, searchFilter, searchCtls);

//  If connection is successful is prints the returned attributes
while (answer.hasMoreElements())
    {
        SearchResult sr = answer.next();
        totalResults++;
        LOG.info(">>>" + sr.getName());
        Attributes attrs = sr.getAttributes();
        LOG.info("User is   :" + attrs.get("samAccountName"));
        LOG.info("Bad Password Count is     :" + attrs.get("badPwdCount"));
        LOG.info("Account Expires on    :" + attrs.get("accountExpires"));
        LOG.info("Password Never Expires    :" + attrs.get("userAccountControl"));
    }
    }
}
