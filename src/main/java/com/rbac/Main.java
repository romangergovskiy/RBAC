package com.rbac;

import com.rbac.model.User;

public class Main {
    public static void main(String[] args) {
        try {
            var u = User.create("john_doe", "John Doe", "john@example.com");
            System.out.println("ok: " + u.format());
        } catch (IllegalArgumentException e) {
            System.out.println("fail: " + e.getMessage());
        }
        try {
            User.create("ab", "Jane", "jane@test.com");
        } catch (IllegalArgumentException e) {
            System.out.println("reject short username: " + e.getMessage());
        }
        try {
            User.create("test_user", "Test", "bad-email");
        } catch (IllegalArgumentException e) {
            System.out.println("reject bad email: " + e.getMessage());
        }
        try {
            User.create("user1", "", "a@b.com");
        } catch (IllegalArgumentException e) {
            System.out.println("reject empty fullName: " + e.getMessage());
        }
    }
}
