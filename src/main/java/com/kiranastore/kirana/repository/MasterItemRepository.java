package com.kiranastore.kirana.repository;

import com.kiranastore.kirana.entity.MasterItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterItemRepository extends JpaRepository<MasterItem, Long> {

    List<MasterItem> findByNameEnglishContainingIgnoreCase(String name);

    List<MasterItem> findByNameHindiContainingIgnoreCase(String name);
}