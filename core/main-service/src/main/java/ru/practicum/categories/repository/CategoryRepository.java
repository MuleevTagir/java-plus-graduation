package ru.practicum.categories.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.categories.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT c FROM Category c ORDER BY c.id LIMIT ?1 OFFSET ?2")
    List<Category> findAllOrderById(int size, int from);
}
