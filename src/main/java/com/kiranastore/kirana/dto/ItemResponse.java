package com.kiranastore.kirana.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemResponse {

    private String name;
    private String hindiName;
    private String quantity;
    private Double price;
    private Double total;
    private boolean matched;
}