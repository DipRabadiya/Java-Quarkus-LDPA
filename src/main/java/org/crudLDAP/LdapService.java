package org.crudLDAP;


import com.unboundid.ldap.sdk.*;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class LdapService {

    private static final Logger LOG = Logger.getLogger(MainResource.class);

    private final String ldapUrl = "localhost";

    private final int ldapPort = 10389;
    private final String bindDN = "uid=admin,ou=system";
    private final String bindPassword = "secret";

    private LDAPConnection getConnection() throws LDAPException {
        LOG.info("Attempting to establish LDAP connection to " + ldapUrl + ":" + ldapPort);
        try {
            LDAPConnection connection = new LDAPConnection(ldapUrl, ldapPort, bindDN, bindPassword);
            LOG.info("LDAP connection established successfully.");
            return connection;
        } catch (LDAPException e) {
            LOG.error("Failed to establish LDAP connection: " + e.getMessage(), e);
            throw e;
        }
    }

    public void addUser(String cn, String sn,String Password,String EmployeeId) throws LDAPException {
        try (LDAPConnection connection = getConnection()) {
            LOG.info("Adding user: cn=" + cn + ", sn=" + sn);
            Entry entry = new Entry("cn=" + cn + ",ou=users,ou=system");
            entry.addAttribute("objectClass", "inetOrgPerson");
            entry.addAttribute("cn", cn);
            entry.addAttribute("sn", sn);
            entry.addAttribute("userPassword", Password);
            entry.addAttribute("uid", EmployeeId);
            LOG.info("User added successfully: cn=" + cn);
            connection.add(entry);
        }catch (LDAPException e) {
            LOG.error("Failed to add user: cn=" + cn, e);
            throw e;
        }
    }

    public void deleteUser(String cn) throws LDAPException {
        try (LDAPConnection connection = getConnection()) {
            LOG.info("Deleting user: cn=" + cn);
            connection.delete("cn=" + cn + ",ou=users,ou=system");
            LOG.info("User deleted successfully: cn=" + cn);
        }catch (LDAPException e) {
            LOG.error("Failed to delete user: cn=" + cn, e);
            throw e;
        }
    }

    public List<SearchResultEntry> getAllUsers() throws LDAPException {
        try (LDAPConnection connection = getConnection()) {
            SearchResult searchResult = connection.search("ou=users,ou=system", SearchScope.SUB, "(objectClass=inetOrgPerson)");
            return searchResult.getSearchEntries();
        }
    }

    public void addUserToGroup(String username, String groupName) throws LDAPException {
        try (LDAPConnection connection = getConnection()) {
            ModifyRequest modifyRequest = new ModifyRequest(
                    "cn=" + groupName + ",ou=groups,ou=system",
                    new Modification(ModificationType.ADD, "uniqueMember", "cn=" + username + ",ou=users,ou=system")
            );
            connection.modify(modifyRequest);
        }
    }

    public void deleteUserFromGroup(String username, String groupName) throws LDAPException {
        try (LDAPConnection connection = getConnection()) {
            ModifyRequest modifyRequest = new ModifyRequest(
                    "cn=" + groupName + ",ou=groups,ou=system",
                    new Modification(ModificationType.DELETE, "uniqueMember", "cn=" + username + ",ou=users,ou=system")
            );
            connection.modify(modifyRequest);
        }
    }

    public boolean authUser(String username, String password) {
        try (LDAPConnection connection = new LDAPConnection(ldapUrl, 10389, "cn=" + username + ",ou=users,ou=system", password)) {
            return true;
        } catch (LDAPException e) {
            return false;
        }
    }

    public void updateUserPassword(String username, String password) throws LDAPException {
        try (LDAPConnection connection = getConnection()) {
            ModifyRequest modifyRequest = new ModifyRequest(
                    "cn=" + username + ",ou=users,ou=system",
                    new Modification(ModificationType.REPLACE, "userPassword", password)
            );
            connection.modify(modifyRequest);
        }
    }

    public void updateUserDetails(String username, String employeeNumber) throws LDAPException {
        try (LDAPConnection connection = getConnection()) {
            ModifyRequest modifyRequest = new ModifyRequest(
                    "cn=" + username + ",ou=users,ou=system",
                    new Modification(ModificationType.REPLACE, "employeeNumber", employeeNumber)
            );
            connection.modify(modifyRequest);
        }
    }

    public void checkConnection() throws LDAPException {
        try (LDAPConnection connection = getConnection()) {
            LOG.info("LDAP connection check successful.");
        } catch (LDAPException e) {
            LOG.error("Failed to connect to LDAP for health check: " + e.getMessage(), e);
            throw e;
        }
    }
}
