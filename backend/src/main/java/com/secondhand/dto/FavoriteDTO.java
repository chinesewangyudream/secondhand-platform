package com.secondhand.dto;

import com.secondhand.entity.Goods;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavoriteDTO {
    private Long id;
    private Long goodsId;
    private LocalDateTime createdAt;
    private Goods goods;
}
