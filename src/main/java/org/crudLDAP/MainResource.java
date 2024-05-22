package org.crudLDAP;


import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResultEntry;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@Path("/ldap")
public class MainResource {

    @Inject
    LdapService ldapService;

    private static final Logger LOG = Logger.getLogger(MainResource.class);

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        try {
            List<SearchResultEntry> users = ldapService.getAllUsers();
            List<User> userList = users.stream().map(entry -> {
                User user = new User();
                user.setCn(entry.getAttributeValue("cn"));
                user.setSn(entry.getAttributeValue("sn"));
                user.setPassword(entry.getAttributeValue("userPassword"));
                user.setEmployeeNumber(entry.getAttributeValue("uid"));
                return user;
            }).collect(Collectors.toList());
            return Response.ok(userList).build();
        } catch (LDAPException e) {
            LOG.error("Failed to retrieve users", e);
            return Response.serverError().entity("Failed to retrieve users").build();
        }
    }

    @POST
    @Path("/user")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUser(User user) {
        try {
            ldapService.addUser(user.getCn(), user.getSn(),user.getPassword(),user.getEmployeeNumber());
            return Response.ok("User added successfully").build();
        } catch (LDAPException e) {
            LOG.error("Failed to add user", e);
            return Response.serverError().entity("Failed to add user").build();
        }
    }

    @DELETE
    @Path("/user/{cn}")
    public Response deleteUser(@PathParam("cn") String cn) {
        try {
            ldapService.deleteUser(cn);
            return Response.ok("User deleted successfully").build();
        } catch (LDAPException e) {
            LOG.error("Failed to delete user", e);
            return Response.serverError().entity("Failed to delete user").build();
        }
    }

    @POST
    @Path("/user/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authUser(User user) {
        boolean authenticated = ldapService.authUser(user.getCn(), user.getPassword());
        if (authenticated) {
            return Response.ok("User authenticated successfully").build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentication failed").build();
        }
    }

    @PUT
    @Path("/user/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserPassword(User user) {
        try {
            ldapService.updateUserPassword(user.getCn(), user.getPassword());
            return Response.ok("User password updated successfully").build();
        } catch (LDAPException e) {
            LOG.error("Failed to update user password", e);
            return Response.serverError().entity("Failed to update user password").build();
        }
    }

    @PUT
    @Path("/user/details")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserDetails(User user) {
        try {
            ldapService.updateUserDetails(user.getCn(), user.getEmployeeNumber());
            return Response.ok("User details updated successfully").build();
        } catch (LDAPException e) {
            LOG.error("Failed to update user details", e);
            return Response.serverError().entity("Failed to update user details").build();
        }
    }

    @GET
    @Path("/check")
    @Produces(MediaType.TEXT_PLAIN)
    public Response checkConnection() {
        try {
            ldapService.checkConnection();
            return Response.ok("LDAP connection successful").build();
        } catch (LDAPException e) {
            LOG.error("Failed to connect to LDAP", e);
            return Response.serverError().entity("Failed to connect to LDAP").build();
        }
    }
}
