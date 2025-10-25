package com.apple.salesassistant.auth.model;

import java.util.Set;
public record User(String id, String email, String passwordHash, Set<String> roles) {}
