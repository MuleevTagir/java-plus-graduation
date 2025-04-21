package ru.practicum.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.users.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u " +
            "FROM User u " +
            "ORDER BY u.id " +
            "LIMIT ?1 " +
            "OFFSET ?2")
    List<User> findAllOrderById(int size, int from);

    Boolean existsByEmail(String email);

    User getUserById(Long userId);
}
