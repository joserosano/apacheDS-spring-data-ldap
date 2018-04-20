package com.jose.ldapspring.ldap.flowable;

import org.flowable.ldap.LDAPConfiguration;
import org.flowable.ldap.LDAPConfigurator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowableLDAPConfigurator {
    @Bean
    LDAPConfiguration ldapConfiguration() {
        LDAPConfiguration ldapConfiguration = new LDAPConfiguration();
        ldapConfiguration.setServer("ldap://127.0.0.1");
        ldapConfiguration.setPort(10412);
        ldapConfiguration.setUser("uid=admin,ou=system");
        ldapConfiguration.setPassword("secret");

        return ldapConfiguration;
    }

    @Bean
    LDAPConfigurator ldapConfigurator(LDAPConfiguration ldapConfiguration) {
        LDAPConfigurator ldapConfigurator = new LDAPConfigurator();
        ldapConfigurator.setLdapConfiguration(ldapConfiguration);

        return ldapConfigurator;
    }

}
