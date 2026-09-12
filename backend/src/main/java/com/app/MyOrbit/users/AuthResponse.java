package com.app.MyOrbit.users;

public record AuthResponse(String token, String id, String name, String email) {
}