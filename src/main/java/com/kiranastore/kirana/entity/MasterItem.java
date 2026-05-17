package com.kiranastore.kirana.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "master_items")
@Getter
@Setter
public class MasterItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nameEnglish;

    @Column(nullable = false)
    private String nameHindi;

    private Double pricePerUnit;

    private String unit;

    @Lob
    @Column(name = "product_image", columnDefinition = "LONGTEXT")
    private String productImage;

    @Column(name = "user_id")
    private Long userId;

    @JsonProperty("isProductLive")
    @Column(name = "is_product_live")
    private Boolean productLive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.productLive == null) {
            this.productLive = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.productLive == null) {
            this.productLive = true;
        }
    }
}
