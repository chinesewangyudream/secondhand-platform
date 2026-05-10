package com.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    List<Map<String, Object>> searchGoods(@Param("keywords") List<String> keywords,
                                          @Param("categoryId") Integer categoryId,
                                          @Param("minPrice") BigDecimal minPrice,
                                          @Param("maxPrice") BigDecimal maxPrice,
                                          @Param("conditionLevel") Integer conditionLevel,
                                          @Param("offset") long offset,
                                          @Param("size") long size);

    long searchGoodsCount(@Param("keywords") List<String> keywords,
                          @Param("categoryId") Integer categoryId,
                          @Param("minPrice") BigDecimal minPrice,
                          @Param("maxPrice") BigDecimal maxPrice,
                          @Param("conditionLevel") Integer conditionLevel);
}
