package com.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.secondhand.dto.FavoriteDTO;
import com.secondhand.dto.Result;
import com.secondhand.entity.Favorite;
import com.secondhand.entity.Goods;
import com.secondhand.mapper.FavoriteMapper;
import com.secondhand.mapper.GoodsMapper;
import com.secondhand.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteMapper favoriteMapper;
    private final GoodsMapper goodsMapper;

    /**
     * 添加收藏
     */
    @PostMapping("/add/{goodsId}")
    public Result<?> addFavorite(@PathVariable Long goodsId) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 检查商品是否存在
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            return Result.error("商品不存在");
        }

        // 不能收藏自己的商品
        if (goods.getSellerId().equals(userId)) {
            return Result.error("不能收藏自己发布的商品");
        }

        // 检查是否已收藏
        Favorite existing = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getGoodsId, goodsId)
        );

        if (existing != null) {
            return Result.error("已经收藏过该商品");
        }

        // 添加收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favoriteMapper.insert(favorite);

        // 更新商品收藏数
        goods.setFavoriteCount(goods.getFavoriteCount() == null ? 1 : goods.getFavoriteCount() + 1);
        goodsMapper.updateById(goods);

        return Result.success("收藏成功");
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/remove/{goodsId}")
    public Result<?> removeFavorite(@PathVariable Long goodsId) {
        Long userId = SecurityUtils.getCurrentUserId();

        Favorite favorite = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getGoodsId, goodsId)
        );

        if (favorite == null) {
            return Result.error("未收藏该商品");
        }

        favoriteMapper.deleteById(favorite.getId());

        // 更新商品收藏数
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods != null && goods.getFavoriteCount() != null && goods.getFavoriteCount() > 0) {
            goods.setFavoriteCount(goods.getFavoriteCount() - 1);
            goodsMapper.updateById(goods);
        }

        return Result.success("已取消收藏");
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{goodsId}")
    public Result<?> checkFavorite(@PathVariable Long goodsId) {
        Long userId = SecurityUtils.getCurrentUserId();

        Favorite favorite = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getGoodsId, goodsId)
        );

        return Result.success(favorite != null);
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/my")
    public Result<?> getMyFavorites() {
        Long userId = SecurityUtils.getCurrentUserId();

        List<Favorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreatedAt)
        );

        if (favorites.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 获取商品ID列表
        List<Long> goodsIds = favorites.stream()
                .map(Favorite::getGoodsId)
                .collect(Collectors.toList());

        // 批量查询商品
        List<Goods> goodsList = goodsMapper.selectBatchIds(goodsIds);

        // 构建返回结果
        List<FavoriteDTO> result = favorites.stream().map(fav -> {
            FavoriteDTO dto = new FavoriteDTO();
            dto.setId(fav.getId());
            dto.setGoodsId(fav.getGoodsId());
            dto.setCreatedAt(fav.getCreatedAt());

            // 找到对应的商品
            Goods goods = goodsList.stream()
                    .filter(g -> g.getId().equals(fav.getGoodsId()))
                    .findFirst()
                    .orElse(null);
            dto.setGoods(goods);

            return dto;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 切换收藏状态（保留原接口兼容）
     */
    @PostMapping("/toggle")
    public Result<?> toggle(@RequestBody java.util.Map<String, Long> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long goodsId = body.get("goodsId");

        Favorite fav = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getGoodsId, goodsId)
        );

        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            return Result.error("商品不存在");
        }

        if (fav != null) {
            favoriteMapper.deleteById(fav.getId());

            // 更新商品收藏数
            if (goods.getFavoriteCount() != null && goods.getFavoriteCount() > 0) {
                goods.setFavoriteCount(goods.getFavoriteCount() - 1);
                goodsMapper.updateById(goods);
            }

            return Result.success("已取消收藏", false);
        } else {
            Favorite newFav = new Favorite();
            newFav.setUserId(userId);
            newFav.setGoodsId(goodsId);
            favoriteMapper.insert(newFav);

            // 更新商品收藏数
            goods.setFavoriteCount(goods.getFavoriteCount() == null ? 1 : goods.getFavoriteCount() + 1);
            goodsMapper.updateById(goods);

            return Result.success("收藏成功", true);
        }
    }
}
