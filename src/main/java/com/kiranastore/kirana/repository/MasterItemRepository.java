package com.kiranastore.kirana.repository;

import com.kiranastore.kirana.entity.MasterItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterItemRepository extends JpaRepository<MasterItem, Long> {

    List<MasterItem> findByNameEnglishContainingIgnoreCase(String name);

    List<MasterItem> findByNameHindiContainingIgnoreCase(String name);

    List<MasterItem> findByUserId(Long userId);

    List<MasterItem> findByUserIdAndIsProductLive(Long userId, Boolean isProductLive);

    List<MasterItem> findByUserIdAndNameEnglishContainingIgnoreCase(Long userId, String name);

    List<MasterItem> findByUserIdAndNameHindiContainingIgnoreCase(Long userId, String name);

    List<MasterItem> findByUserIdAndIsProductLiveAndNameEnglishContainingIgnoreCase(Long userId, Boolean isProductLive, String name);

    List<MasterItem> findByUserIdAndIsProductLiveAndNameHindiContainingIgnoreCase(Long userId, Boolean isProductLive, String name);
}
