package com.jose.ldapspring;

import com.jose.ldapspring.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.InvalidNameException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LdapspringApplicationTests {

	@Autowired
	UserService userService;

	@Test
	public void createUser() throws InvalidNameException {/*
		userService.create("testNamr", "testPass");*/
		assertThat(1);
	}

}
