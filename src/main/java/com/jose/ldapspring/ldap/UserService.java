package com.jose.ldapspring.ldap;

import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import javax.naming.Name;

@Service
public class UserService {
    private LdapUserRepository ldapUserRepository;

    public UserService(LdapUserRepository ldapUserRepository) {
        this.ldapUserRepository = ldapUserRepository;
    }

    public Iterable<User> findAll() {
        return ldapUserRepository.findAll();
    }

    public void create(String username, String password) {
        User newUser = new User(username, password);
        Name dn = LdapNameBuilder
                .newInstance()/*
                .add("dc", "com")
                .add("dc", "sgr")
                .add("ou", "sgrusers")*/
                .add("cn", username)
                .add("sn", password)
                .build();
        newUser.setDn(dn);
        ldapUserRepository.save(newUser);
    }
}
