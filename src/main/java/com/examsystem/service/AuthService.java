package main.java.com.examsystem.service;

import main.java.com.examsystem.model.User;

import java.util.*;

public class AuthService {

    private static final Map<String, User> users = new HashMap<>();
    private static final Map<String, User> tokens = new HashMap<>();

    static {
        users.put("user1", new User(1, "user1", "1111"));
        users.put("user2", new User(2, "user2", "2222"));
    }

    public Optional<User> authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, user);
        return token;
    }

    public Optional<User> validateToken(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

}