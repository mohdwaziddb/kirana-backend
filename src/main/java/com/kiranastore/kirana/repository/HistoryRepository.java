package com.kiranastore.kirana.repository;

import com.kiranastore.kirana.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    
    @Query("SELECT h FROM History h WHERE h.userId = :userId ORDER BY h.timestamp DESC")
    List<History> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId);
    
    @Query("SELECT h FROM History h WHERE h.id = :id AND h.userId = :userId")
    History findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
