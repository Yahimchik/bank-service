package com.example.bankcards.data;

import com.example.bankcards.entities.Role;

import java.util.UUID;

public class RoleTestData {
    public static final Role ROLE_USER = new Role(UUID.randomUUID(), "USER");
    public static final Role ROLE_ADMIN = new Role(UUID.randomUUID(), "ADMIN");
}
