package rbac.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import rbac.filters.UserFilter;
import rbac.model.User;

public class UserManager implements Repository<User> {
    private final Map<String, User> users = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void add(User item) {
        if (item == null) {
            throw new IllegalArgumentException("user is null");
        }
        String username = item.username();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is empty");
        }
        lock.writeLock().lock();
        try {
            if (users.containsKey(username)) {
                throw new IllegalStateException("User with username '" + username + "' already exists");
            }
            users.put(username, item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(User item) {
        if (item == null) return false;
        lock.writeLock().lock();
        try {
            return users.remove(item.username(), item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        lock.readLock().lock();
        try {
            return Optional.ofNullable(users.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<User> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(users.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return users.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            users.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<User> findByUsername(String username) {
        return findById(username);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        List<User> snapshot;
        lock.readLock().lock();
        try {
            snapshot = new ArrayList<>(users.values());
        } finally {
            lock.readLock().unlock();
        }
        return snapshot.stream()
            .filter(u -> u.email().equals(email))
            .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        List<User> snapshot;
        lock.readLock().lock();
        try {
            snapshot = new ArrayList<>(users.values());
        } finally {
            lock.readLock().unlock();
        }
        List<User> result = new ArrayList<>();
        for (User u : snapshot) {
            if (filter.test(u)) {
                result.add(u);
            }
        }
        return result;
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        List<User> result = findByFilter(filter);
        result.sort(sorter);
        return result;
    }

    public boolean exists(String username) {
        lock.readLock().lock();
        try {
            return users.containsKey(username);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void update(String username, String newFullName, String newEmail) {
        lock.writeLock().lock();
        try {
            User existing = users.get(username);
            if (existing == null) {
                throw new IllegalStateException("User '" + username + "' does not exist");
            }
            User updated = User.create(username, newFullName, newEmail);
            users.put(username, updated);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserManager that)) return false;
        lock.readLock().lock();
        that.lock.readLock().lock();
        try {
            return Objects.equals(users, that.users);
        } finally {
            that.lock.readLock().unlock();
            lock.readLock().unlock();
        }
    }

    @Override
    public int hashCode() {
        lock.readLock().lock();
        try {
            return Objects.hash(users);
        } finally {
            lock.readLock().unlock();
        }
    }
}

