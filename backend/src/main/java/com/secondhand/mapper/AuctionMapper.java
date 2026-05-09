package com.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.entity.Auction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuctionMapper extends BaseMapper<Auction> {}
