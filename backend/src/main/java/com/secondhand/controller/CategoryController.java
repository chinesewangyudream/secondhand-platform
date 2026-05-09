package com.secondhand.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.dto.Result;
import com.secondhand.entity.*;
import com.secondhand.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;

    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(
                categoryMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Category>()
                                .eq(Category::getStatus, 1)
                                .orderByAsc(Category::getSortOrder)
                )
        );
    }
}