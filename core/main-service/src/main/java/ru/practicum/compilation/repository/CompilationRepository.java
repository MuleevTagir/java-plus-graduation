package ru.practicum.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.compilation.model.Compilation;

import java.util.List;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM compilation AS c" +
            " WHERE c.pinned = ?1" +
            " LIMIT ?3" +
            " OFFSET ?2")
    List<Compilation> getCompilationsByPinned(Boolean pinned, Integer from, Integer size);

    @Query(nativeQuery = true, value = "SELECT * FROM compilation AS c" +
            " LIMIT ?1" +
            " OFFSET ?2")
    List<Compilation> getCompilations(Integer size, Integer offset);
}
