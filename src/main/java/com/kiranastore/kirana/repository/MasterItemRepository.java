package com.kiranastore.kirana.repository;

import com.kiranastore.kirana.entity.MasterItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasterItemRepository extends JpaRepository<MasterItem, Long> {

    List<MasterItem> findByNameEnglishContainingIgnoreCase(String name);

    List<MasterItem> findByNameHindiContainingIgnoreCase(String name);

    List<MasterItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<MasterItem> findByUserIdAndProductLiveOrderByCreatedAtDesc(Long userId, Boolean productLive);

    @Query("select m from MasterItem m where m.userId = :userId and " +
            "(lower(m.nameEnglish) like lower(concat('%', :keyword, '%')) or " +
            "lower(m.nameHindi) like lower(concat('%', :keyword, '%'))) " +
            "order by m.createdAt desc")
    List<MasterItem> searchSellerItems(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Query("select m from MasterItem m where m.userId = :userId and m.productLive = :productLive and " +
            "(lower(m.nameEnglish) like lower(concat('%', :keyword, '%')) or " +
            "lower(m.nameHindi) like lower(concat('%', :keyword, '%'))) " +
            "order by m.createdAt desc")
    List<MasterItem> searchSellerItemsByLiveStatus(
            @Param("userId") Long userId,
            @Param("productLive") Boolean productLive,
            @Param("keyword") String keyword
    );

    @Query("select m from MasterItem m where m.productLive = true or m.productLive is null")
    List<MasterItem> findBuyerVisibleItems();
}
