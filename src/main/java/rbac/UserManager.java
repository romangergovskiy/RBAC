package rbac;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class UserManager implements Repository<User> {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void add(User item) {
        if (item == null) {
            throw new IllegalArgumentException("user is null");
        }
        String username = item.username();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is empty");
        }
        if (users.containsKey(username)) {
            throw new IllegalStateException("User with username '" + username + "' already exists");
        }
        users.put(username, item);
    }

    @Override
    public boolean remove(User item) {
        if (item == null) return false;
        return users.remove(item.username(), item);
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }

    public Optional<User> findByUsername(String username) {
        return findById(username);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return users.values().stream()
            .filter(u -> u.email().equals(email))
            .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        List<User> result = new ArrayList<>();
        for (User u : users.values()) {
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
        return users.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        User existing = users.get(username);
        if (existing == null) {
            throw new IllegalStateException("User '" + username + "' does not exist");
        }
        User updated = User.create(username, newFullName, newEmail);
        users.put(username, updated);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserManager that)) return false;
        return Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
}

