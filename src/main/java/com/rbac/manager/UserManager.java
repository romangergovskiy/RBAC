package com.rbac.manager;

import com.rbac.filter.UserFilter;
import com.rbac.model.User;
import com.rbac.repository.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager implements Repository<User> {
    private final Map<String, User> byUsername = new ConcurrentHashMap<>();

    @Override
    public void add(User item) {
        if (item == null) throw new IllegalArgumentException("User cannot be null");
        if (byUsername.containsKey(item.username())) throw new IllegalArgumentException("User already exists: " + item.username());
        byUsername.put(item.username(), item);
    }

    @Override
    public boolean remove(User item) {
        return item != null && byUsername.remove(item.username(), item);
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(byUsername.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(byUsername.values());
    }

    @Override
    public int count() {
        return byUsername.size();
    }

    @Override
    public void clear() {
        byUsername.clear();
    }

    public Optional<User> findByUsername(String username) {
        return findById(username);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return byUsername.values().stream().filter(u -> email.equals(u.email())).findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        if (filter == null) return findAll();
        return byUsername.values().stream().filter(filter::test).toList();
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> list = filter == null ? findAll() : findByFilter(filter);
        if (sorter != null) {
            list = new ArrayList<>(list);
            list.sort(sorter);
        }
        return list;
    }

    public boolean exists(String username) {
        return username != null && byUsername.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        User u = byUsername.get(username);
        if (u == null) throw new IllegalArgumentException("User not found: " + username);
        byUsername.put(username, new User(username, newFullName, newEmail));
    }
}
