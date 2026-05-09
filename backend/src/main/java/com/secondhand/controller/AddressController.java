package com.secondhand.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.dto.Result;
import com.secondhand.entity.Address;
import com.secondhand.mapper.AddressMapper;
import com.secondhand.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {
    private final AddressMapper addressMapper;

    @GetMapping("/list")
    public Result<?> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(
                addressMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Address>()
                                .eq(Address::getUserId, userId)
                )
        );
    }

    @PostMapping("/save")
    public Result<?> save(@RequestBody Address address) {
        address.setUserId(SecurityUtils.getCurrentUserId());
        if (address.getId() == null) {
            addressMapper.insert(address);
        } else {
            addressMapper.updateById(address);
        }
        return Result.success("保存成功", address.getId());
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        addressMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}