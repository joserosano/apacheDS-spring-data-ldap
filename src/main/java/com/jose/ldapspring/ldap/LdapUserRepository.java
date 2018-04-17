package com.jose.ldapspring.ldap;

import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LdapUserRepository extends LdapRepository<User> {
    LdapUserRepository findByUsername(String username);
    List<LdapUserRepository> findByUsernameLikeIgnoreCase(String username);
}
