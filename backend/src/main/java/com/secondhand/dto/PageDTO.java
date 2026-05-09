package com.secondhand.dto;

import lombok.Data;

@Data
public class PageDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private Integer categoryId;
    private String sortBy;    // price_asc, price_desc, newest, popular
    private Double minPrice;
    private Double maxPrice;
    private Integer conditionLevel;
}