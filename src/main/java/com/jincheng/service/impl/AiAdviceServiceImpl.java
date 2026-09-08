package com.jincheng.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jincheng.entity.Member;
import com.jincheng.mapper.ClockInMapper;
import com.jincheng.service.AiAdviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAdviceServiceImpl implements AiAdviceService {

    private static final String SYSTEM_PROMPT =
            "你是健身房管理系统内置的 AI 健身顾问。请遵守：\n" +
            "1. 只回答健身、训练、营养、体态与运动恢复相关问题；\n" +
            "2. 给出可执行的建议，尽量分点、具体到组数/次数/时长/心率/克数；\n" +
            "3. 结合用户给出的身体数据与训练数据个性化回答；\n" +
            "4. 不做疾病诊断，涉及疼痛、慢病、孕期等情况必须建议就医或咨询线下教练；\n" +
            "5. 使用简体中文，控制在 400 字以内。";

    @Autowired
    private ClockInMapper clockInMapper;

    @Value("${ai.provider:local}")
    private String provider;

    @Value("${ai.base-url:}")
    private String baseUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:}")
    private String model;

    @Value("${ai.timeout-ms:20000}")
    private long timeoutMs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> advise(Member member, String question) {
        Map<String, Object> context = buildContext(member);
        String q = question == null ? "" : question.trim();
        if (q.isEmpty()) {
            q = "请给我一份通用的健身建议";
        }

        if (isLlmEnabled()) {
            try {
                String answer = callChatModel(q, context);
                if (answer != null && !answer.isBlank()) {
                    return buildResult(answer, "llm", "AI 大模型", context, followUpTips());
                }
            } catch (Exception e) {
                // 大模型调用失败时静默回退到本地知识库，保证功能可用
            }
        }

        String answer = localAnswer(q, context);
        return buildResult(answer, "local", "本地知识库", context, followUpTips());
    }

    @Override
    public Map<String, Object> engineInfo() {
        Map<String, Object> info = new HashMap<>();
        boolean llm = isLlmEnabled();
        info.put("provider", llm ? "llm" : "local");
        info.put("label", llm ? "AI 大模型" : "本地知识库");
        info.put("hasApiKey", apiKey != null && !apiKey.isBlank());
        return info;
    }

    // ------------------------------------------------------------------
    // 会员数据上下文
    // ------------------------------------------------------------------
    private Map<String, Object> buildContext(Member member) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("hasMetrics", false);
        ctx.put("weightKg", null);
        ctx.put("bodyFatPct", null);
        ctx.put("bmi", null);
        ctx.put("bmiCategory", "未知");
        ctx.put("recordDate", null);
        ctx.put("monthSessions", 0);
        ctx.put("monthCalories", 0);
        ctx.put("age", null);
        ctx.put("sex", null);
        ctx.put("memberName", member == null ? null : member.getMemberName());

        if (member == null) {
            return ctx;
        }
        ctx.put("age", member.getMemberAge());
        ctx.put("sex", member.getMemberSex());

        try {
            Map<String, Object> latest = clockInMapper.selectLatestMetricsByMemberId(member.getMemberId());
            if (latest != null && !latest.isEmpty()) {
                Double weight = toDouble(latest.get("weightKg"));
                Double bodyFat = toDouble(latest.get("bodyFatPct"));
                Double bmi = toDouble(latest.get("bmi"));
                ctx.put("hasMetrics", weight != null || bmi != null);
                ctx.put("weightKg", weight);
                ctx.put("bodyFatPct", bodyFat);
                ctx.put("bmi", bmi);
                ctx.put("bmiCategory", bmiCategory(bmi));
                ctx.put("recordDate", latest.get("recordDate"));
            }
            ctx.put("monthSessions", clockInMapper.countThisMonthByMemberId(member.getMemberId()));
            Integer calories = clockInMapper.sumThisMonthCaloriesByMemberId(member.getMemberId());
            ctx.put("monthCalories", calories == null ? 0 : calories);
        } catch (Exception e) {
            // 数据表异常不阻断问答
        }
        return ctx;
    }

    // ------------------------------------------------------------------
    // 本地健身知识库规则引擎
    // ------------------------------------------------------------------
    private String localAnswer(String question, Map<String, Object> ctx) {
        String q = question.toLowerCase();
        List<String> lines = new ArrayList<>();

        lines.add("【结合你的数据】" + describeContext(ctx));

        String intent = detectIntent(q);

        if (containsAny(q, "减脂", "减肥", "瘦身", "掉秤", "瘦下来", "脂肪")) {
            lines.add("【减脂方案】");
            lines.add("· 热量缺口：每日比消耗少 300~500 kcal，不建议超过 500 kcal，否则容易掉肌肉。");
            lines.add("· 有氧安排：每周 3~5 次，每次 30~45 分钟，心率控制在最大心率的 60%~70%" + heartRateHint(ctx) + "。");
            lines.add("· 力量训练：每周 2~3 次（深蹲、硬拉、卧推、划船），保留肌肉、提高基础代谢。");
            lines.add("· 饮食重点：蛋白质 " + proteinHint(ctx) + "，蔬菜每日 300~500 g，控油控糖控酒。");
            lines.add("· 节奏：每周减重 0.5%~1% 体重最稳妥，固定时间称重看周平均值。");
        } else if (containsAny(q, "增肌", "肌肉", "变壮", "纬度", "力量")) {
            lines.add("【增肌方案】");
            lines.add("· 热量盈余：每日比消耗多 200~300 kcal，避免脂肪增长过快。");
            lines.add("· 蛋白质：" + proteinHint(ctx) + "，碳水训练前后重点补充。");
            lines.add("· 训练：以复合动作为主（深蹲、硬拉、卧推、划船、肩推），6~12RM，每个大肌群每周 10~20 组。");
            lines.add("· 渐进超负荷：同样组数下，每周重量或次数提升 2.5%~5%，记录训练日志。");
            lines.add("· 恢复：同一肌群间隔 48 小时，睡眠 7~9 小时。");
        } else if (containsAny(q, "吃", "饮食", "营养", "热量", "蛋白", "三餐", "卡路里", "食谱")) {
            lines.add("【饮食建议】");
            lines.add("· 估算总消耗 TDEE ≈ 体重(kg) × 28~35（按活动量取高取低），减脂在此基础上 -300~500 kcal，增肌 +200~300 kcal。");
            lines.add("· 三大营养素：蛋白质 " + proteinHint(ctx) + "，脂肪 0.8~1.0 g/kg，其余用碳水补足。");
            lines.add("· 示例三餐：早餐 鸡蛋2个+燕麦50g+牛奶250ml；午餐 米饭150g+鸡胸/牛肉150g+蔬菜300g；晚餐 红薯150g+鱼虾/豆腐150g+蔬菜300g。");
            lines.add("· 饮水：每日 30~35 ml/kg" + waterHint(ctx) + "，训练中每 15~20 分钟补 150~200 ml。");
        } else if (containsAny(q, "计划", "安排", "课表", "怎么练", "训练表", "一周")) {
            lines.add("【每周 3 天训练模板】");
            lines.add("· D1 下肢+核心：深蹲 4×8-10、罗马尼亚硬拉 3×10、箭步蹲 3×10/侧、平板支撑 3×40 秒。");
            lines.add("· D2 上肢推：卧推/俯卧撑 4×8-12、肩推 3×10、三头下压 3×12。");
            lines.add("· D3 上肢拉+有氧：高位下拉/引体 4×8-12、杠铃划船 3×10、二头弯举 3×12，再加 20 分钟有氧。");
            lines.add("· 单次结构：热身 10 分钟 → 主体 40 分钟 → 有氧/核心 15 分钟 → 拉伸 10 分钟。");
        } else if (containsAny(q, "拉伸", "放松", "酸痛", "僵硬", "柔韧", "瑜伽")) {
            lines.add("【拉伸与恢复】");
            lines.add("· 训练后静态拉伸：股四头、腘绳、胸大肌、背阔、小腿，每个动作 15~30 秒 × 2~3 组。");
            lines.add("· 泡沫轴放松 5~10 分钟，重点滚大腿外侧与背部。");
            lines.add("· 延迟性酸痛（DOMS）多在 48~72 小时缓解，低强度活动 + 充足睡眠 + 补水可加速恢复。");
        } else if (containsAny(q, "疼", "痛", "伤", "膝盖", "腰", "肩", "不舒服", "扭")) {
            lines.add("【安全提示（优先）】");
            lines.add("· 出现关节刺痛、肿胀、活动受限，请立即停止训练，冰敷并及时就医或在系统里告知你的教练。");
            lines.add("· 急性期遵循 RICE：休息 Rest、冰敷 Ice（每次 15 分钟）、加压 Compression、抬高 Elevation。");
            lines.add("· 常见诱因：膝盖内扣需加强臀中肌与核心；腰部不适先纠正含胸弓背，从髋铰链动作（硬拉模式）重新学习。");
            lines.add("· 本建议不能替代医生的诊断与治疗方案。");
        } else if (containsAny(q, "有氧", "跑步", "心肺", "心率", "慢跑", "椭圆", "单车")) {
            lines.add("【有氧训练】");
            lines.add("· 每周累计 150 分钟中等强度，或 75 分钟高强度；新手从「能说话但不能唱歌」的强度起步。");
            lines.add("· 心率区间：最大心率 ≈ 220 - 年龄" + heartRateHint(ctx) + "，减脂常用 60%~70%，间歇训练不超过每周 2 次。");
            lines.add("· 跑前动态热身 8 分钟（高抬腿、弓步走、摆腿），跑后必做小腿与大腿后侧拉伸。");
        } else if (containsAny(q, "睡眠", "恢复", "休息", "疲劳", "熬夜")) {
            lines.add("【恢复管理】");
            lines.add("· 睡眠 7~9 小时，尽量固定作息，睡前 1 小时减少屏幕时间。");
            lines.add("· 同一肌群训练间隔 48 小时，每周至少安排 1 个完全休息日。");
            lines.add("· 每训练 6~8 周安排一个减量周（训练量降低约 40%），避免过度训练。");
        } else if (containsAny(q, "bmi", "体重", "体脂", "胖", "标准", "胖吗")) {
            lines.add("【身体数据解读】");
            lines.add("· 你的 BMI：" + bmiText(ctx) + "（中国成人标准：<18.5 偏瘦，18.5~23.9 正常，24~27.9 超重，≥28 肥胖）。");
            lines.add("· 称重建议：晨起空腹、如厕后固定时间测量，看周平均值而非单日波动。");
            lines.add("· 体脂率比体重更能反映减脂效果，建议每 2~4 周用同一设备复测一次。");
        } else if (containsAny(q, "喝水", "补水", "饮水")) {
            lines.add("【补水建议】");
            lines.add("· 每日 30~35 ml/kg" + waterHint(ctx) + "，分次少量饮用。");
            lines.add("· 训练前 2 小时补 300~500 ml，训练中每 15~20 分钟补 150~200 ml，训练后补足出汗量的 1.5 倍。");
        } else if (containsAny(q, "器械", "动作", "姿势", "标准", "呼吸", "组数")) {
            lines.add("【动作质量要点】");
            lines.add("· 动作质量优先于重量：先保证关节排列与活动度，再逐步加重。");
            lines.add("· 呼吸：发力时呼气、还原时吸气，大重量不要憋气（可用瓦式呼吸需教练指导）。");
            lines.add("· 常用参数：增力 3~6RM、增肌 6~12RM、耐力 15~20RM；组间休息 60~180 秒。");
        } else {
            lines.add("【通用健身建议】");
            lines.add("· 训练：每周 3 次力量 + 2 次有氧，每次 60 分钟左右，先热身再训练、结束必拉伸。");
            lines.add("· 饮食：蛋白质 " + proteinHint(ctx) + "，多吃原型食物，控油盐糖。");
            lines.add("· 恢复：睡眠 7~9 小时，每周安排休息日，训练量循序渐进（每周增幅不超过 10%）。");
            lines.add("· 你可以继续问我：减脂怎么安排、增肌饮食、跑步心率、训练后拉伸、膝盖不舒服怎么办等。");
        }

        lines.addAll(personalLines(intent, ctx));
        lines.add("提示：以上建议由系统健身知识库生成，仅供参考，不能替代医生诊断与线下教练指导。");
        return String.join("\n", lines);
    }

    private String detectIntent(String q) {
        if (containsAny(q, "减脂", "减肥", "瘦身", "掉秤", "瘦下来", "脂肪")) return "减脂";
        if (containsAny(q, "增肌", "肌肉", "变壮", "纬度", "力量")) return "增肌";
        if (containsAny(q, "吃", "饮食", "营养", "热量", "蛋白", "三餐", "卡路里", "食谱")) return "饮食";
        if (containsAny(q, "计划", "安排", "课表", "怎么练", "训练表", "一周")) return "计划";
        if (containsAny(q, "拉伸", "放松", "酸痛", "僵硬", "柔韧", "瑜伽")) return "拉伸";
        if (containsAny(q, "疼", "痛", "伤", "膝盖", "腰", "肩", "不舒服", "扭")) return "伤痛";
        if (containsAny(q, "有氧", "跑步", "心肺", "心率", "慢跑", "椭圆", "单车")) return "有氧";
        if (containsAny(q, "睡眠", "恢复", "休息", "疲劳", "熬夜")) return "恢复";
        if (containsAny(q, "bmi", "体重", "体脂", "胖", "标准", "胖吗")) return "身体";
        if (containsAny(q, "喝水", "补水", "饮水")) return "补水";
        if (containsAny(q, "器械", "动作", "姿势", "标准", "呼吸", "组数")) return "动作";
        return "通用";
    }

    private List<String> personalLines(String intent, Map<String, Object> ctx) {
        List<String> lines = new ArrayList<>();
        String category = String.valueOf(ctx.get("bmiCategory"));
        int sessions = toInt(ctx.get("monthSessions"));

        if ("超重".equals(category) || "肥胖".equals(category)) {
            if ("增肌".equals(intent)) {
                lines.add("【针对你的情况】当前 BMI 偏高，建议先用 8~12 周控制热量把体脂降到合理区间，再进入增肌期，增肌质量更高。");
            } else {
                lines.add("【针对你的情况】BMI 偏高，建议优先选择低冲击有氧（快走、椭圆机、游泳、单车），减少跳跃类动作以保护膝踝。");
            }
        } else if ("偏瘦".equals(category)) {
            lines.add("【针对你的情况】体重偏轻，建议先把热量盈余与力量训练放在首位，有氧控制在每周 2 次以内。");
        } else if ("正常".equals(category)) {
            lines.add("【针对你的情况】BMI 处于正常区间，可按目标自由选择减脂或增肌路线，保持规律训练即可。");
        }

        if (sessions == 0) {
            lines.add("【训练频率】本月还没有打卡记录，建议从每周 2~3 次、每次 40 分钟开始，先养成习惯。");
        } else if (sessions < 4) {
            lines.add("【训练频率】本月训练不足 4 次，建议先稳定到每周 2~3 次，再逐步提高强度。");
        } else if (sessions > 12) {
            lines.add("【训练频率】本月训练已超过 12 次，注意安排恢复日与睡眠，避免累积疲劳。");
        }

        Boolean hasMetrics = (Boolean) ctx.get("hasMetrics");
        if (Boolean.FALSE.equals(hasMetrics)) {
            lines.add("【数据完善】还没有体重/BMI 记录，去「健身打卡」录入一次，我能给出更精准的建议。");
        }
        return lines;
    }

    private List<String> followUpTips() {
        List<String> tips = new ArrayList<>();
        tips.add("可以把目标说得更具体，例如「一个月减 2 公斤」");
        tips.add("告诉我每周能训练几天、每次多久，便于排课");
        tips.add("涉及伤痛或慢性疾病，请先咨询医生或线下教练");
        return tips;
    }

    // ------------------------------------------------------------------
    // 大模型（OpenAI 兼容协议）
    // ------------------------------------------------------------------
    private boolean isLlmEnabled() {
        return "llm".equalsIgnoreCase(provider)
                && baseUrl != null && !baseUrl.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && model != null && !model.isBlank();
    }

    private String callChatModel(String question, Map<String, Object> ctx) throws Exception {
        Map<String, Object> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);

        Map<String, Object> userMessage = new LinkedHashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "会员数据：" + describeContext(ctx) + "\n问题：" + question);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", List.of(systemMessage, userMessage));
        body.put("temperature", 0.7);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .timeout(Duration.ofMillis(Math.max(1000, timeoutMs)))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, timeoutMs)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("AI 服务返回 " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        return content.isMissingNode() ? null : content.asText();
    }

    // ------------------------------------------------------------------
    // 工具方法
    // ------------------------------------------------------------------
    private Map<String, Object> buildResult(String answer, String source, String sourceLabel,
                                            Map<String, Object> context, List<String> tips) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("answer", answer);
        payload.put("source", source);
        payload.put("sourceLabel", sourceLabel);
        payload.put("tips", tips);
        payload.put("context", context);
        return payload;
    }

    private String describeContext(Map<String, Object> ctx) {
        StringBuilder sb = new StringBuilder();
        Double weight = (Double) ctx.get("weightKg");
        Double bodyFat = (Double) ctx.get("bodyFatPct");
        if (weight != null) {
            sb.append("体重 ").append(weight).append(" kg");
        }
        if (bodyFat != null) {
            if (sb.length() > 0) sb.append("｜");
            sb.append("体脂 ").append(bodyFat).append(" %");
        }
        if (sb.length() > 0) sb.append("｜");
        sb.append("BMI ").append(bmiText(ctx));
        sb.append("｜本月训练 ").append(toInt(ctx.get("monthSessions"))).append(" 次");
        sb.append("｜本月消耗 ").append(toInt(ctx.get("monthCalories"))).append(" kcal");
        Object age = ctx.get("age");
        if (age != null) {
            sb.append("｜年龄 ").append(age).append(" 岁");
        }
        return sb.toString();
    }

    private String bmiText(Map<String, Object> ctx) {
        Double bmi = (Double) ctx.get("bmi");
        if (bmi == null) {
            return "暂无数据";
        }
        return String.format("%.1f（%s）", bmi, bmiCategory(bmi));
    }

    private String bmiCategory(Double bmi) {
        if (bmi == null) return "未知";
        if (bmi < 18.5) return "偏瘦";
        if (bmi < 24) return "正常";
        if (bmi < 28) return "超重";
        return "肥胖";
    }

    private String proteinHint(Map<String, Object> ctx) {
        Double weight = (Double) ctx.get("weightKg");
        if (weight == null || weight <= 0) {
            return "1.6~2.0 g/kg 体重";
        }
        int low = (int) Math.round(weight * 1.6);
        int high = (int) Math.round(weight * 2.0);
        return "1.6~2.0 g/kg 体重（约 " + low + "~" + high + " g/天）";
    }

    private String waterHint(Map<String, Object> ctx) {
        Double weight = (Double) ctx.get("weightKg");
        if (weight == null || weight <= 0) {
            return "";
        }
        return "（约 " + (int) Math.round(weight * 30 / 100) * 100 + " ml/天）";
    }

    private String heartRateHint(Map<String, Object> ctx) {
        Object age = ctx.get("age");
        if (age == null) {
            return "（可用「能说话但不能唱歌」判断强度）";
        }
        int max = 220 - toInt(age);
        return "（按你的年龄估算最大心率约 " + max + "，燃脂区间约 " + (int) (max * 0.6) + "~" + (int) (max * 0.7) + " 次/分）";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double d) return d;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
