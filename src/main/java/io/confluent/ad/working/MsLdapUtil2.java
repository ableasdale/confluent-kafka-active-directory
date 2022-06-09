package io.confluent.ad.working;

import io.confluent.ad.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

public class MsLdapUtil2 {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        MsLdapUtil2 util = new MsLdapUtil2();
        LOG.info("Starting");
        util.getAllUsers("cn=testuser,cn=Users,dc=ad-test,dc=confluent,dc=io", Config.PASSWORD);
    }

    public void getAllUsers(String user, String password) {

        try {
            String domainName = "confluent.io";
            String serverName = "192.168.1.98:389";
            String rootContext = "cn=Users,dc=ad-test,dc=confluent,dc=io";



            //create an initial directory context
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, "ldap://" + serverName);
//      env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, Config.USERNAME+Config.REALM);
           // env.put(Context.SECURITY_PRINCIPAL, Config.USERNAME);
            env.put(Context.SECURITY_CREDENTIALS, Config.PASSWORD);

            // Create the initial directory context
            DirContext ctx = new InitialDirContext(env);

            //get all the users list and their group memberships
            listSubContext(ctx, rootContext);

        } catch (NamingException e) {
            e.printStackTrace();
            return;
        }
    }

    private void listSubContext(DirContext ctx, String nm) throws NamingException {
        String[] attributeNames = { "memberOf", "userAccountControl", "mail",
                "name", "sAMAccountName" };
        NamingEnumeration contentsEnum = ctx.list(nm);
        while (contentsEnum.hasMore()) {
            NameClassPair ncp = (NameClassPair) contentsEnum.next();
            String userName = ncp.getName();
            Attributes attr1 = ctx.getAttributes(userName + "," + nm,
                    new String[] { "objectcategory" });
            if (attr1.get("objectcategory").toString().indexOf("CN=Person") == -1) {
            //if (true==false) {
                // Recurse sub-contexts
                listSubContext(ctx, userName + "," + nm);
            } else {
                UserRec rec = new UserRec();
                try {
                    Attributes attrs = ctx.getAttributes(userName + "," + nm,
                            attributeNames);
                    Attribute groupsAttribute = attrs.get("memberOf");
                    Attribute bitsAttribute = attrs.get("userAccountControl");
                    Attribute mailAttribute = attrs.get("mail");
                    Attribute nameAttribute = attrs.get("name");
                    Attribute accountAttribute = attrs.get("sAMAccountName");
                    System.out.println("ATTRIBUTES  ::  "+nameAttribute);
                    if (accountAttribute != null) {
                        for (int i = 0; i < accountAttribute.size(); i++) {
                            rec.account = (String) accountAttribute.get(i);
                        }
                    }
                    if (groupsAttribute != null) {
                        for (int i = 0; i < groupsAttribute.size(); i++) {
                            rec.groups.add(groupsAttribute.get(i));
                        }
                    }
                    if (bitsAttribute != null) {
                        long lng = Long.parseLong(bitsAttribute.get(0).toString());
                        long secondBit = lng & 2; // get bit 2
                        if (secondBit == 0) {
                            rec.enabled = true;
                        }
                    }
                    if (mailAttribute != null) {
                        for (int i = 0; i < mailAttribute.size(); i++) {
                            rec.email = (String) mailAttribute.get(i);
                        }
                    }
                    if (nameAttribute != null) {
                        for (int i = 0; i < nameAttribute.size(); i++) {
                            rec.name = (String) nameAttribute.get(i);
                        }
                    }

                } catch (NamingException ne) {
                    ne.printStackTrace();
                }

                System.out.println("----\nUser: " + rec.name);
                System.out.println("Enabled: " + rec.enabled);
                System.out.println("Email: " + rec.email);
                System.out.println("Account: " + rec.account);
                for (Iterator iterator = rec.groups.iterator(); iterator.hasNext();) {
                    String groupName = (String) iterator.next();
                    System.out.println(rec.name + " is a member of: " + groupName);
                }
            }
        }
    }

    class UserRec {
        String account;
        String name;
        List groups;
        String email;
        boolean enabled;
        public UserRec() {
            groups = new ArrayList();
        }
    }
}