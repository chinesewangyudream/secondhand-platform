package com.secondhand.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AiPricingService {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.api-url}")
    private String apiUrl;

    @Value("${ai.model}")
    private String model;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * AI估价：调用大语言模型
     */
    public BigDecimal estimate(String title, String description,
                               Integer categoryId, Integer condition) {
        String prompt = buildPrompt(title, description, categoryId, condition);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);

        JSONArray messages = new JSONArray();
        JSONObject sysMsg = new JSONObject();
        sysMsg.put("role", "system");
        sysMsg.put("content",
                "你是一个二手物品估价专家，熟悉闲鱼、转转等二手平台的市场行情。" +
                        "请根据商品名称判断其在市场上的实际二手价格，" +
                        "成色仅用于微调价格，不要让成色成为定价的主要因素。" +
                        "只需要返回一个合理的估价数字（人民币），不需要任何解释。");
        messages.add(sysMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 50);
        requestBody.put("temperature", 0.3);

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(RequestBody.create(
                        requestBody.toJSONString(),
                        MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String body = response.body().string();
                JSONObject json = JSON.parseObject(body);
                String content = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                // 提取数字
                String numStr = content.trim().replaceAll("[^0-9.]", "");
                return new BigDecimal(numStr);
            }
        } catch (Exception e) {
            // 如无API Key，使用规则估价
            return ruleBasedEstimate(title, categoryId, condition);
        }
        return ruleBasedEstimate(title, categoryId, condition);
    }

    /**
     * AI生成商品描述和估价
     */
    public Map<String, Object> generateDescription(String title, Integer categoryId, Integer condition) {
        Map<String, Object> result = new HashMap<>();

        String categoryName = getCategoryName(categoryId);
        String conditionStr = condition != null ? condition + "成新" : "7成新";

        String prompt = String.format(
                "请为以下二手商品生成详细的商品描述和建议售价：\n" +
                        "商品名称：%s\n分类：%s\n成色：%s\n\n" +
                        "描述要求：\n" +
                        "1. 描述字数100-150字，突出商品特点和卖点\n" +
                        "2. 包含商品的基本功能、适用场景、品牌优势等\n" +
                        "3. 语气亲切自然，像朋友推荐一样\n" +
                        "4. 避免夸大宣传，真实描述商品状况\n\n" +
                        "估价要求：\n" +
                        "1. 请根据该商品在当前二手市场上的实际价格进行估价\n" +
                        "2. 参考闲鱼、转转等主流二手平台的价格\n" +
                        "3. 成色因素仅作为价格调整参考，不是主要决定因素\n" +
                        "4. 如果是知名品牌或热门商品，请参考其实际二手成交价\n\n" +
                        "请按以下JSON格式返回：{\"description\": \"详细描述内容\", \"price\": 价格数字}",
                title, categoryName, conditionStr
        );

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);

        JSONArray messages = new JSONArray();
        JSONObject sysMsg = new JSONObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", "你是一个专业的二手物品描述撰写专家，擅长写出吸引买家的商品描述。" +
                "你同时熟悉闲鱼、转转等二手平台的市场行情，能给出合理的估价建议。" +
                "请根据商品名称判断其在市场上的实际二手价格。" +
                "只返回JSON格式结果。");
        messages.add(sysMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(RequestBody.create(
                        requestBody.toJSONString(),
                        MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String body = response.body().string();
                JSONObject json = JSON.parseObject(body);
                String content = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                // 尝试解析JSON
                try {
                    // 提取JSON部分
                    String jsonStr = content;
                    if (content.contains("{")) {
                        jsonStr = content.substring(content.indexOf("{"));
                        if (content.contains("}")) {
                            jsonStr = jsonStr.substring(0, jsonStr.lastIndexOf("}") + 1);
                        }
                    }
                    JSONObject parsed = JSON.parseObject(jsonStr);
                    result.put("description", parsed.getString("description"));
                    String priceStr = parsed.getString("price");
                    if (priceStr != null) {
                        result.put("price", new BigDecimal(priceStr.replaceAll("[^0-9.]", "")));
                    }
                } catch (Exception e) {
                    // 解析失败，使用规则生成
                    result.put("description", generateRuleDescription(title, categoryName, conditionStr));
                    result.put("price", ruleBasedEstimate(title, categoryId, condition));
                }
                return result;
            }
        } catch (Exception e) {
            // 使用规则生成
        }

        // 使用规则生成
        result.put("description", generateRuleDescription(title, categoryName, conditionStr));
        result.put("price", ruleBasedEstimate(title, categoryId, condition));
        return result;
    }

    private String generateRuleDescription(String title, String categoryName, String conditionStr) {
        return String.format("%s，%s，%s，品质保证，欢迎选购。", title, categoryName, conditionStr);
    }

    private String getCategoryName(Integer categoryId) {
        if (categoryId == null) return "其他";
        return switch (categoryId) {
            case 1 -> "电子数码";
            case 2 -> "服装鞋帽";
            case 3 -> "图书文具";
            case 4 -> "家居生活";
            case 5 -> "运动户外";
            case 6 -> "游戏动漫";
            case 7 -> "美妆护肤";
            default -> "其他";
        };
    }

    private String buildPrompt(String title, String desc,
                               Integer categoryId, Integer condition) {
        String categoryName = getCategoryName(categoryId);
        return String.format(
                "请估算这个二手物品的合理售价（人民币）：\n" +
                        "物品名称：%s\n分类：%s\n描述：%s\n成色（1-10，10最新）：%d\n\n" +
                        "估价要求：\n" +
                        "1. 请根据该商品在当前二手市场上的实际价格进行估价\n" +
                        "2. 参考闲鱼、转转等主流二手平台的价格\n" +
                        "3. 成色因素仅作为价格调整参考，不是主要决定因素\n" +
                        "只返回数字，不需要单位和解释。",
                title, categoryName, desc != null ? desc : "无", condition != null ? condition : 7
        );
    }

    /**
     * 规则估价（无AI API时的备选方案）
     * 根据商品名称关键词和分类给出更贴近实际的估价
     */
    private BigDecimal ruleBasedEstimate(String title, Integer categoryId, Integer condition) {
        if (title == null) title = "";

        // 根据商品名称关键词判断常见商品的价格区间
        String lowerTitle = title.toLowerCase();
        int basePrice = 100;

        // 电子数码类 (categoryId = 1)
        if (categoryId != null && categoryId == 1) {
            if (lowerTitle.contains("iphone") || lowerTitle.contains("苹果手机")) {
                basePrice = 2500;
            } else if (lowerTitle.contains("macbook") || lowerTitle.contains("苹果电脑")) {
                basePrice = 4000;
            } else if (lowerTitle.contains("ipad") || lowerTitle.contains("平板")) {
                basePrice = 1500;
            } else if (lowerTitle.contains("华为") && (lowerTitle.contains("手机") || lowerTitle.contains("mate"))) {
                basePrice = 2000;
            } else if (lowerTitle.contains("小米") && lowerTitle.contains("手机")) {
                basePrice = 1200;
            } else if (lowerTitle.contains("笔记本") || lowerTitle.contains("电脑")) {
                basePrice = 2000;
            } else if (lowerTitle.contains("耳机") || lowerTitle.contains("airpods")) {
                basePrice = 300;
            } else if (lowerTitle.contains("相机") || lowerTitle.contains("单反")) {
                basePrice = 2500;
            } else if (lowerTitle.contains("键盘") || lowerTitle.contains("鼠标")) {
                basePrice = 150;
            } else if (lowerTitle.contains("充电器") || lowerTitle.contains("数据线")) {
                basePrice = 30;
            } else {
                basePrice = 500;
            }
        }
        // 服装鞋帽类 (categoryId = 2)
        else if (categoryId != null && categoryId == 2) {
            if (lowerTitle.contains("羽绒服") || lowerTitle.contains("大衣")) {
                basePrice = 200;
            } else if (lowerTitle.contains("运动鞋") || lowerTitle.contains("耐克") || lowerTitle.contains("阿迪")) {
                basePrice = 250;
            } else if (lowerTitle.contains("连衣裙") || lowerTitle.contains("外套")) {
                basePrice = 120;
            } else if (lowerTitle.contains("牛仔裤") || lowerTitle.contains("裤子")) {
                basePrice = 80;
            } else if (lowerTitle.contains("t恤") || lowerTitle.contains("衬衫")) {
                basePrice = 50;
            } else if (lowerTitle.contains("包") || lowerTitle.contains("包包")) {
                basePrice = 150;
            } else {
                basePrice = 80;
            }
        }
        // 图书文具类 (categoryId = 3)
        else if (categoryId != null && categoryId == 3) {
            if (lowerTitle.contains("教材") || lowerTitle.contains("课本")) {
                basePrice = 15;
            } else if (lowerTitle.contains("小说") || lowerTitle.contains("文学")) {
                basePrice = 20;
            } else if (lowerTitle.contains("考研") || lowerTitle.contains("考试")) {
                basePrice = 30;
            } else if (lowerTitle.contains("漫画") || lowerTitle.contains("绘本")) {
                basePrice = 25;
            } else {
                basePrice = 30;
            }
        }
        // 家居生活类 (categoryId = 4)
        else if (categoryId != null && categoryId == 4) {
            if (lowerTitle.contains("沙发") || lowerTitle.contains("床") || lowerTitle.contains("床垫")) {
                basePrice = 800;
            } else if (lowerTitle.contains("桌") || lowerTitle.contains("椅")) {
                basePrice = 150;
            } else if (lowerTitle.contains("冰箱") || lowerTitle.contains("洗衣机")) {
                basePrice = 600;
            } else if (lowerTitle.contains("微波炉") || lowerTitle.contains("电饭煲")) {
                basePrice = 100;
            } else if (lowerTitle.contains("台灯") || lowerTitle.contains("收纳")) {
                basePrice = 40;
            } else {
                basePrice = 150;
            }
        }
        // 运动户外类 (categoryId = 5)
        else if (categoryId != null && categoryId == 5) {
            if (lowerTitle.contains("自行车") || lowerTitle.contains("电动车")) {
                basePrice = 500;
            } else if (lowerTitle.contains("跑步机") || lowerTitle.contains("健身器材")) {
                basePrice = 400;
            } else if (lowerTitle.contains("篮球") || lowerTitle.contains("足球")) {
                basePrice = 60;
            } else if (lowerTitle.contains("瑜伽") || lowerTitle.contains("瑜伽垫")) {
                basePrice = 50;
            } else if (lowerTitle.contains("帐篷") || lowerTitle.contains("露营")) {
                basePrice = 200;
            } else {
                basePrice = 200;
            }
        }
        // 游戏动漫类 (categoryId = 6)
        else if (categoryId != null && categoryId == 6) {
            if (lowerTitle.contains("ps5") || lowerTitle.contains("switch") || lowerTitle.contains("xbox")) {
                basePrice = 1500;
            } else if (lowerTitle.contains("手办") || lowerTitle.contains("模型")) {
                basePrice = 200;
            } else if (lowerTitle.contains("游戏") && lowerTitle.contains("卡")) {
                basePrice = 80;
            } else if (lowerTitle.contains("漫画") || lowerTitle.contains("周边")) {
                basePrice = 50;
            } else {
                basePrice = 150;
            }
        }
        // 美妆护肤类 (categoryId = 7)
        else if (categoryId != null && categoryId == 7) {
            if (lowerTitle.contains("口红") || lowerTitle.contains("香水")) {
                basePrice = 150;
            } else if (lowerTitle.contains("精华") || lowerTitle.contains("面霜")) {
                basePrice = 200;
            } else if (lowerTitle.contains("面膜")) {
                basePrice = 50;
            } else if (lowerTitle.contains("粉底") || lowerTitle.contains("眼影")) {
                basePrice = 100;
            } else {
                basePrice = 80;
            }
        }
        // 其他类
        else {
            basePrice = 100;
        }

        // 成色调整：成色只影响最终价格的30%
        double conditionRate = condition != null ? condition / 10.0 : 0.7;
        double adjustedPrice = basePrice * (0.7 + 0.3 * conditionRate);

        return BigDecimal.valueOf(Math.round(adjustedPrice));
    }

    /**
     * 规则估价（兼容旧接口）
     */
    private BigDecimal ruleBasedEstimate(Integer categoryId, Integer condition) {
        return ruleBasedEstimate("", categoryId, condition);
    }

    /**
     * AI商品推荐：根据用户需求和预算推荐商品
     */
    public Map<String, Object> recommendProducts(String requirement, BigDecimal minBudget, BigDecimal maxBudget) {
        Map<String, Object> result = new HashMap<>();

        // 构建推荐提示词
        String prompt = String.format(
                "用户需求：%s\n预算范围：%s - %s 元\n\n" +
                "请根据用户需求和预算，推荐适合的二手商品类别和具体建议。\n" +
                "要求：\n" +
                "1. 分析用户的核心需求\n" +
                "2. 推荐适合的商品类别\n" +
                "3. 给出具体商品建议（3-5个）\n" +
                "4. 每个建议需包含：商品名称、推荐理由、预期价格范围\n" +
                "5. 价格应在用户预算范围内\n\n" +
                "请按以下JSON格式返回：\n" +
                "{\"analysis\": \"需求分析\", \"category\": \"推荐类别名称\", \"recommendations\": [{\"name\": \"商品名称\", \"reason\": \"推荐理由\", \"priceRange\": \"价格范围\"}]}",
                requirement,
                minBudget != null ? minBudget.toString() : "不限",
                maxBudget != null ? maxBudget.toString() : "不限"
        );

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);

        JSONArray messages = new JSONArray();
        JSONObject sysMsg = new JSONObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", "你是一个专业的二手商品推荐顾问，熟悉各种品类的二手商品，" +
                "能根据用户需求和预算给出合理的购买建议。只返回JSON格式结果。");
        messages.add(sysMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 800);
        requestBody.put("temperature", 0.7);

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(RequestBody.create(
                        requestBody.toJSONString(),
                        MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String body = response.body().string();
                JSONObject json = JSON.parseObject(body);
                String content = json.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                // 尝试解析JSON
                try {
                    String jsonStr = content;
                    if (content.contains("{")) {
                        jsonStr = content.substring(content.indexOf("{"));
                        if (content.contains("}")) {
                            jsonStr = jsonStr.substring(0, jsonStr.lastIndexOf("}") + 1);
                        }
                    }
                    JSONObject parsed = JSON.parseObject(jsonStr);
                    result.put("analysis", parsed.getString("analysis"));
                    result.put("category", parsed.getString("category"));
                    result.put("recommendations", parsed.getJSONArray("recommendations"));
                    result.put("success", true);
                } catch (Exception e) {
                    // 解析失败，使用规则推荐
                    return ruleBasedRecommend(requirement, minBudget, maxBudget);
                }
                return result;
            }
        } catch (Exception e) {
            // 使用规则推荐
        }

        // 使用规则推荐
        return ruleBasedRecommend(requirement, minBudget, maxBudget);
    }

    /**
     * 规则推荐（无AI API时的备选方案）
     */
    private Map<String, Object> ruleBasedRecommend(String requirement, BigDecimal minBudget, BigDecimal maxBudget) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> recommendations = new ArrayList<>();

        String lowerReq = requirement.toLowerCase();
        String category = "其他";
        String analysis = "根据您的需求，为您推荐以下商品类别。";

        double min = minBudget != null ? minBudget.doubleValue() : 0;
        double max = maxBudget != null ? maxBudget.doubleValue() : Double.MAX_VALUE;

        // 根据关键词匹配推荐
        if (lowerReq.contains("手机") || lowerReq.contains("iphone") || lowerReq.contains("安卓")) {
            category = "电子数码";
            if (max >= 2000) {
                recommendations.add(createRec("iPhone 二手手机", "苹果手机保值性好，二手性价比高", "2000-4000元"));
            }
            if (max >= 1000) {
                recommendations.add(createRec("华为/小米二手手机", "国产旗舰机性能强劲，二手价格实惠", "1000-2500元"));
            }
            if (min < 500) {
                recommendations.add(createRec("千元机二手手机", "满足日常使用，价格亲民", "300-800元"));
            }
        } else if (lowerReq.contains("电脑") || lowerReq.contains("笔记本") || lowerReq.contains("游戏")) {
            category = "电子数码";
            if (max >= 3000) {
                recommendations.add(createRec("游戏本二手电脑", "性能强劲，适合游戏和工作", "3000-6000元"));
            }
            if (max >= 2000) {
                recommendations.add(createRec("轻薄本二手电脑", "便携办公首选，续航出色", "2000-4000元"));
            }
            if (min < 1500) {
                recommendations.add(createRec("办公本二手电脑", "满足日常办公需求", "800-1500元"));
            }
        } else if (lowerReq.contains("相机") || lowerReq.contains("摄影") || lowerReq.contains("单反")) {
            category = "电子数码";
            if (max >= 3000) {
                recommendations.add(createRec("二手单反相机", "专业画质，适合摄影爱好者", "2500-5000元"));
            }
            if (max >= 1500) {
                recommendations.add(createRec("二手微单相机", "轻便易携，画质出色", "1500-3500元"));
            }
            if (min < 800) {
                recommendations.add(createRec("二手卡片相机", "入门首选，简单易用", "500-1000元"));
            }
        } else if (lowerReq.contains("衣服") || lowerReq.contains("鞋") || lowerReq.contains("包") || lowerReq.contains("穿搭")) {
            category = "服装鞋帽";
            if (max >= 300) {
                recommendations.add(createRec("品牌运动鞋", "正品保证，舒适耐穿", "200-400元"));
            }
            if (max >= 200) {
                recommendations.add(createRec("品牌羽绒服/外套", "保暖时尚，品质保证", "150-350元"));
            }
            if (min < 100) {
                recommendations.add(createRec("日常休闲服饰", "款式多样，价格实惠", "50-100元"));
            }
        } else if (lowerReq.contains("书") || lowerReq.contains("教材") || lowerReq.contains("考研") || lowerReq.contains("学习")) {
            category = "图书文具";
            if (max >= 100) {
                recommendations.add(createRec("考研复习资料", "正版教材，笔记详细", "50-150元"));
            }
            recommendations.add(createRec("专业教材书籍", "大学教材，价格低廉", "20-60元"));
            recommendations.add(createRec("文学作品小说", "丰富精神生活", "15-40元"));
        } else if (lowerReq.contains("家具") || lowerReq.contains("家电") || lowerReq.contains("居家")) {
            category = "家居生活";
            if (max >= 800) {
                recommendations.add(createRec("二手小家电", "实用家电，价格实惠", "300-600元"));
            }
            if (max >= 500) {
                recommendations.add(createRec("二手家具", "品质家具，搬家转让", "200-500元"));
            }
            recommendations.add(createRec("居家收纳用品", "整理收纳，生活必备", "30-100元"));
        } else if (lowerReq.contains("运动") || lowerReq.contains("健身") || lowerReq.contains("户外")) {
            category = "运动户外";
            if (max >= 500) {
                recommendations.add(createRec("健身器材", "居家健身，价格实惠", "300-800元"));
            }
            if (max >= 300) {
                recommendations.add(createRec("户外露营装备", "帐篷睡袋，户外必备", "200-500元"));
            }
            recommendations.add(createRec("运动球类/器材", "锻炼身体，健康生活", "50-150元"));
        } else {
            // 通用推荐
            category = "全部分类";
            if (max >= 1000) {
                recommendations.add(createRec("电子产品", "手机电脑平板，应有尽有", "500-3000元"));
            }
            if (max >= 200) {
                recommendations.add(createRec("服装鞋帽", "品牌好物，价格实惠", "100-300元"));
            }
            recommendations.add(createRec("图书文具", "知识无价，二手更划算", "20-80元"));
        }

        result.put("analysis", analysis);
        result.put("category", category);
        result.put("recommendations", recommendations);
        result.put("success", true);
        return result;
    }

    private Map<String, String> createRec(String name, String reason, String priceRange) {
        Map<String, String> rec = new HashMap<>();
        rec.put("name", name);
        rec.put("reason", reason);
        rec.put("priceRange", priceRange);
        return rec;
    }
}