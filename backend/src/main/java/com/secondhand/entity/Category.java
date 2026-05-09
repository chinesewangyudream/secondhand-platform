package com.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("categories")
public class Category {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String icon;
    private Integer parentId;
    private Integer sortOrder;
    private Integer status;
}