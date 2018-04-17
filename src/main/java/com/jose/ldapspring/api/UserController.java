package com.jose.ldapspring.api;

import com.jose.ldapspring.ldap.User;
import com.jose.ldapspring.ldap.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Iterable<User> findAll() {
        return userService.findAll();
    }

    @PostMapping
    public void createUser(@RequestParam String username, @RequestParam String password) {
        userService.create(username, password);
    }
}
