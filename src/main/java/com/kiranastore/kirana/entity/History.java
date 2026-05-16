package com.kiranastore.kirana.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "history")
public class History {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "table_data", columnDefinition = "TEXT")
    private String tableData;

    @Column(name = "customer_name")
    private String customerName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private Action action;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    public enum Action {
        SAVE,
        SHARE
    }
    
    // Constructors
    public History() {}
    
    public History(Long userId, String tableData, Action action, LocalDateTime timestamp) {
        this.userId = userId;
        this.tableData = tableData;
        this.action = action;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getTableData() {
        return tableData;
    }
    
    public void setTableData(String tableData) {
        this.tableData = tableData;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public Action getAction() {
        return action;
    }
    
    public void setAction(Action action) {
        this.action = action;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
