### Manual Check

WIN+R -> **dsa.msc**

```bash
ldapsearch -x -b "dc=ad-test,dc=confluent,dc=io" -H ldap://192.168.1.98 -D "cn=testuser,cn=Users,dc=ad-test,dc=confluent,dc=io" -W
```

- https://docs.microsoft.com/en-us/sharepoint/troubleshoot/security/configuration-to-support-kerberos-aes-encryption
- https://docs.microsoft.com/en-us/previous-versions/windows/it-pro/windows-server-2008-r2-and-2008/cc753104(v=ws.11)
- https://docs.microsoft.com/en-us/windows-server/identity/ad-ds/active-directory-functional-levels
- https://anexinet.com/blog/the-time-zone-bug-in-windows-server-2019/
- https://www.stigviewer.com/stig/windows_10/2017-04-28/finding/V-63795
- 