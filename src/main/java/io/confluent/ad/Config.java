package io.confluent.ad;

public class Config {
    public static final String IP_ADDR = "192.168.1.77";
    public static final String PORT = "389";
    public static final String LDAP_FULL_URL = "ldap://"+IP_ADDR+":"+PORT;
    public static final String IP_PORT = IP_ADDR+":"+PORT;
    public static String USERNAME = "Kafka_test";
    public static String PASSWORD = "C0nflu3nt%K4fk4@2022!";

    public static String REALM = "@ad-test.confluent.io";
    public static String AD_SEARCH_BASE = "CN=Users,dc=ad-test,dc=confluent,dc=io";
}
