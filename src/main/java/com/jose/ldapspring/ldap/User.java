package com.jose.ldapspring.ldap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

@Entry(objectClasses = { "person", "top" }, base="ou=sgrusers")
public final class User {
    @Id
    @JsonIgnore
    //TODO jose parece que necesitaremos un serializer para Name(LdapName) si queremos traer este campo
    private Name dn;

    private @Attribute(name = "cn") String username;
    private @Attribute(name = "sn") String password;

    // for spring ldap
    public User(){}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Name getDn() {
        return dn;
    }

    public void setDn(Name dn) {
        this.dn = dn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // No @Attribute annotation means this will be bound to the LDAP attribute
    // with the same value
/*    private String firstName;

    @DnAttribute(value="ou", index=0)
    @Transient
    private String company;

    @Transient
    private String someUnmappedField;
    // ...more attributes below*/
    /*@Entry(
            base = "ou=users",
            objectClasses = { "person", "inetOrgPerson", "top" })
    public class User {
        @Id
        private Name dn;

        private @Attribute(name = "cn") String username;
        private @Attribute(name = "sn") String password;

        // standard getters/setters
    }*/
}
