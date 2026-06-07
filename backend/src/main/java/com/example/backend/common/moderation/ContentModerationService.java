package com.example.backend.common.moderation;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.ReportCategory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ContentModerationService {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<!\\d)(13\\d|14[5-9]|15\\d|16[5-7]|17[0-8]|18\\d|19\\d)\\d{8}(?!\\d)");
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "(https?://|www\\.|[a-z0-9][a-z0-9-]{1,63}\\.(com|cn|net|org|io|cc|top|xyz|vip|me|info|edu)(/|\\b))");
    private static final Pattern OBFUSCATED_DOMAIN_PATTERN = Pattern.compile(
            "[a-z0-9][a-z0-9-]{1,63}\\s*(\\.|。|点)\\s*(com|cn|net|org|io|cc|top|xyz|vip|me|info|edu)");
    private static final Pattern WECHAT_PATTERN = Pattern.compile("(微信|wechat|vx|wx)[号:]?[a-z0-9]{5,20}");
    private static final Pattern QQ_PATTERN = Pattern.compile("qq[号:]?\\d{5,12}");

    private final Set<String> sexualWords = new HashSet<String>();
    private final Set<String> politicalWords = new HashSet<String>();
    private final Set<String> suspiciousDomains = new HashSet<String>();
    private final Set<String> heavyAbuseWords = new HashSet<String>();
    private final Set<String> lightAbuseWords = new HashSet<String>();

    public ContentModerationService() {
        loadDictionary("色情类.txt", sexualWords);
        loadDictionary("政治类.txt", politicalWords);
        loadDictionary("网址.txt", suspiciousDomains);
        loadBuiltInWords();
    }

    public ModerationResult reviewUsername(String username) {
        ModerationResult result = review(username);
        if (containsContactInfo(username)) {
            result.addIssue(new ModerationIssue(ReportCategory.CONTACT_INFO,
                    ModerationIssue.Severity.INFO, "用户名不得包含联系方式"));
        }
        return result;
    }

    public ModerationResult reviewMessage(String content) {
        ModerationResult result = review(content);
        if (containsContactInfo(content)) {
            result.addIssue(new ModerationIssue(ReportCategory.CONTACT_INFO,
                    ModerationIssue.Severity.INFO, "消息包含联系方式"));
            result.addWarning("你正在发送手机号、微信号或QQ号，请注意隐私和线下交易风险");
        }
        return result;
    }

    public ModerationResult reviewDemandContent(String content) {
        ModerationResult result = review(content);
        if (containsContactInfo(content)) {
            result.addIssue(new ModerationIssue(ReportCategory.CONTACT_INFO,
                    ModerationIssue.Severity.INFO, "需求内容包含联系方式"));
            result.addWarning("你正在发布手机号、微信号或QQ号，请注意隐私和线下交易风险");
        }
        return result;
    }

    public void assertUsernameAllowed(String username) {
        ModerationResult result = reviewUsername(username);
        if (result.blocksUsername()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    "用户名包含不允许使用的敏感内容");
        }
    }

    public ModerationResult assertMessageAllowed(String content) {
        ModerationResult result = reviewMessage(content);
        if (result.blocksMessage()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    "私信包含不允许发送的敏感内容");
        }
        return result;
    }

    public ModerationResult assertDemandContentAllowed(String fieldName, String content) {
        ModerationResult result = reviewDemandContent(content);
        if (result.blocksMessage()) {
            throw new BusinessException(ErrorCode.INVALID_PARAM,
                    fieldName + "包含不允许发布的敏感内容");
        }
        return result;
    }

    String normalizeForModeration(String text) {
        if (text == null) {
            return "";
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (isZeroWidth(ch) || Character.isWhitespace(ch) || ch == '_' || ch == '-') {
                continue;
            }
            int type = Character.getType(ch);
            if (type == Character.CONNECTOR_PUNCTUATION
                    || type == Character.DASH_PUNCTUATION
                    || type == Character.START_PUNCTUATION
                    || type == Character.END_PUNCTUATION
                    || type == Character.INITIAL_QUOTE_PUNCTUATION
                    || type == Character.FINAL_QUOTE_PUNCTUATION
                    || type == Character.OTHER_PUNCTUATION) {
                continue;
            }
            builder.append(ch);
        }
        return builder.toString();
    }

    private ModerationResult review(String content) {
        ModerationResult result = new ModerationResult();
        String normalized = normalizeForModeration(content);
        String raw = Normalizer.normalize(content == null ? "" : content, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);

        if (containsAny(normalized, sexualWords)) {
            result.addIssue(new ModerationIssue(ReportCategory.SEXUAL,
                    ModerationIssue.Severity.HEAVY, "包含色情或擦边内容"));
        }
        if (containsAny(normalized, politicalWords)) {
            result.addIssue(new ModerationIssue(ReportCategory.POLITICAL,
                    ModerationIssue.Severity.HEAVY, "包含涉政讨论"));
        }
        if (containsAny(normalized, heavyAbuseWords)) {
            result.addIssue(new ModerationIssue(ReportCategory.ABUSE,
                    ModerationIssue.Severity.HEAVY, "包含重度辱骂"));
        } else if (containsAny(normalized, lightAbuseWords)) {
            result.addIssue(new ModerationIssue(ReportCategory.ABUSE,
                    ModerationIssue.Severity.LIGHT, "包含轻度辱骂"));
        }
        if (containsLinkOrQr(raw, normalized)) {
            result.addIssue(new ModerationIssue(ReportCategory.LINK_OR_QR,
                    ModerationIssue.Severity.HEAVY, "包含外链或二维码"));
        }
        return result;
    }

    private boolean containsLinkOrQr(String raw, String normalized) {
        if (LINK_PATTERN.matcher(raw).find()) {
            return true;
        }
        if (OBFUSCATED_DOMAIN_PATTERN.matcher(raw).find()) {
            return true;
        }
        if (normalized.contains("二维码")
                || normalized.contains("qrcode")
                || normalized.contains("扫码")
                || normalized.contains("扫我")) {
            return true;
        }
        return containsAny(normalized, suspiciousDomains);
    }

    private boolean containsContactInfo(String text) {
        String normalized = normalizeForModeration(text);
        return PHONE_PATTERN.matcher(normalized).find()
                || WECHAT_PATTERN.matcher(normalized).find()
                || QQ_PATTERN.matcher(normalized).find();
    }

    private boolean containsAny(String normalized, Set<String> words) {
        if (normalized == null || normalized.isEmpty()) {
            return false;
        }
        for (String word : words) {
            if (!word.isEmpty() && normalized.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private void loadDictionary(String fileName, Set<String> target) {
        List<String> values = new ArrayList<String>();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("sensitive-words/" + fileName)) {
            if (in != null) {
                values.addAll(parseWords(new String(in.readAllBytes(), StandardCharsets.UTF_8)));
            }
        } catch (IOException ignored) {
        }
        for (String value : values) {
            String normalized = normalizeForModeration(value);
            if (normalized.length() >= 2) {
                target.add(normalized);
            }
        }
    }

    private List<String> parseWords(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        List<String> words = new ArrayList<String>();
        for (String value : content.split("[,\\r\\n]+")) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                words.add(trimmed);
            }
        }
        return words;
    }

    private boolean isZeroWidth(char ch) {
        return ch == '\u200B'
                || ch == '\u200C'
                || ch == '\u200D'
                || ch == '\u200E'
                || ch == '\u200F'
                || ch == '\u2060'
                || ch == '\uFEFF';
    }

    private void loadBuiltInWords() {
        addWords(sexualWords, List.of("擦边", "约炮", "看片", "裸聊", "卖身", "色情", "成人片", "开房"));
        addWords(politicalWords, List.of("政治讨论", "涉政", "政变", "台独", "港独", "疆独", "藏独"));
        addWords(heavyAbuseWords, List.of("去死", "死全家", "畜生", "杂种", "贱人", "傻逼", "sb", "nmsl"));
        addWords(lightAbuseWords, List.of("笨蛋", "傻子", "白痴", "蠢", "菜鸡", "垃圾"));
    }

    private void addWords(Set<String> target, List<String> words) {
        for (String word : words) {
            target.add(normalizeForModeration(word));
        }
    }
}
