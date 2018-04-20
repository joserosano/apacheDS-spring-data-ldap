package com.jose.ldapspring.service;

import com.jose.ldapspring.domain.core.user.LdapUserRepository;
import com.jose.ldapspring.domain.core.user.User;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
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
