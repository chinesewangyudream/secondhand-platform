package com.secondhand.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.dto.PageDTO;
import com.secondhand.dto.ReportDTO;
import com.secondhand.entity.Goods;
import com.secondhand.entity.Report;
import com.secondhand.mapper.ReportMapper;
import com.secondhand.mapper.GoodsMapper;
import com.secondhand.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService extends ServiceImpl<ReportMapper, Report> {

    private final GoodsMapper goodsMapper;
    private final GoodsService goodsService;

    /**
     * 举报商品
     */
    @Transactional
    public void reportGoods(ReportDTO dto) {
        Long reporterId = SecurityUtils.getCurrentUserId();

        // 检查商品是否存在
        Goods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        // 不能举报自己的商品
        if (goods.getSellerId().equals(reporterId)) {
            throw new RuntimeException("不能举报自己的商品");
        }

        // 检查是否已经举报过
        Long existCount = lambdaQuery()
                .eq(Report::getGoodsId, dto.getGoodsId())
                .eq(Report::getReporterId, reporterId)
                .eq(Report::getStatus, 0)  // 待处理
                .count();
        if (existCount > 0) {
            throw new RuntimeException("您已举报过该商品，请等待处理");
        }

        // 创建举报记录
        Report report = new Report();
        report.setGoodsId(dto.getGoodsId());
        report.setReporterId(reporterId);
        report.setReason(dto.getReason());
        report.setStatus(0);  // 待处理
        save(report);
    }

    /**
     * 获取举报列表（审核员使用）
     */
    public Page<Map<String, Object>> getReportList(PageDTO dto, Integer status) {
        Page<Report> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        wrapper.orderByDesc(Report::getCreatedAt);

        page(page, wrapper);

        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(),
                page.getSize(), page.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();

        for (Report report : page.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", report.getId());
            map.put("goodsId", report.getGoodsId());
            map.put("reporterId", report.getReporterId());
            map.put("reason", report.getReason());
            map.put("status", report.getStatus());
            map.put("handleResult", report.getHandleResult());
            map.put("createdAt", report.getCreatedAt());
            map.put("updatedAt", report.getUpdatedAt());

            // 附带商品信息
            Goods goods = goodsMapper.selectById(report.getGoodsId());
            if (goods != null) {
                Map<String, Object> goodsInfo = new HashMap<>();
                goodsInfo.put("id", goods.getId());
                goodsInfo.put("title", goods.getTitle());
                goodsInfo.put("description", goods.getDescription());
                goodsInfo.put("price", goods.getPrice());
                goodsInfo.put("originalPrice", goods.getOriginalPrice());
                goodsInfo.put("conditionLevel", goods.getConditionLevel());
                goodsInfo.put("location", goods.getLocation());
                goodsInfo.put("status", goods.getStatus());
                goodsInfo.put("sellerId", goods.getSellerId());
                goodsInfo.put("viewCount", goods.getViewCount());
                goodsInfo.put("createdAt", goods.getCreatedAt());
                // 解析图片
                List<String> images = new ArrayList<>();
                if (goods.getImages() != null && !goods.getImages().isEmpty()) {
                    try {
                        images = JSON.parseArray(goods.getImages(), String.class);
                    } catch (Exception e) {
                        images.add(goods.getImages());
                    }
                }
                goodsInfo.put("images", images);
                map.put("goods", goodsInfo);
            }

            records.add(map);
        }

        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 处理举报（审核员使用）
     */
    @Transactional
    public void handleReport(Long reportId, boolean isViolation, String handleResult) {
        Report report = getById(reportId);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        if (report.getStatus() != 0) {
            throw new RuntimeException("该举报已处理");
        }

        Long handlerId = SecurityUtils.getCurrentUserId();
        report.setHandlerId(handlerId);
        report.setHandleResult(handleResult);

        if (isViolation) {
            // 确认违规，下架商品
            report.setStatus(1);
            Goods goods = goodsMapper.selectById(report.getGoodsId());
            if (goods != null) {
                goods.setStatus(5);  // 违规状态
                goods.setViolationReason("经用户举报确认违规：" + report.getReason());
                goodsMapper.updateById(goods);
            }
        } else {
            // 不违规
            report.setStatus(2);
        }

        updateById(report);
    }

    /**
     * 获取商品的举报次数
     */
    public long getReportCount(Long goodsId) {
        return lambdaQuery()
                .eq(Report::getGoodsId, goodsId)
                .eq(Report::getStatus, 0)  // 只统计待处理的
                .count();
    }
}
