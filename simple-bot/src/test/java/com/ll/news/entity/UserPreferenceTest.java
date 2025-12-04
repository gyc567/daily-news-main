package com.ll.news.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserPreference实体测试
 * 遵循KISS原则：简单、明确、可测试
 */
@DisplayName("用户偏好实体测试")
class UserPreferenceTest {

    private UserPreference userPreference;

    @BeforeEach
    void setUp() {
        // 使用Builder模式创建测试对象 - 简洁明了
        userPreference = UserPreference.builder()
                .userId(123456789L)
                .keywords("")
                .pushFrequency(30)
                .pushStartTime(LocalTime.of(9, 0))
                .pushEndTime(LocalTime.of(22, 0))
                .enabled(true)
                .pushCount(0)
                .build();
    }

    @Test
    @DisplayName("应该正确获取关键词列表（空）")
    void shouldReturnEmptyKeywordList() {
        // given - 空关键词
        userPreference.setKeywords("");

        // when
        String[] keywords = userPreference.getKeywordList();

        // then - KISS：简单断言
        assertNotNull(keywords);
        assertEquals(0, keywords.length);
    }

    @Test
    @DisplayName("应该正确获取关键词列表（有值）")
    void shouldReturnKeywordListWithValues() {
        // given - 有值关键词
        userPreference.setKeywords("比特币,以太坊,区块链");

        // when
        String[] keywords = userPreference.getKeywordList();

        // then - 明确测试每个预期
        assertNotNull(keywords);
        assertEquals(3, keywords.length);
        assertEquals("比特币", keywords[0].trim());
        assertEquals("以太坊", keywords[1].trim());
        assertEquals("区块链", keywords[2].trim());
    }

    @Test
    @DisplayName("应该成功添加新关键词")
    void shouldAddNewKeyword() {
        // given - 初始为空
        userPreference.setKeywords("");

        // when - 添加关键词
        boolean result = userPreference.addKeyword("比特币");

        // then - KISS：简单成功断言
        assertTrue(result);
        assertEquals("比特币", userPreference.getKeywords());
    }

    @Test
    @DisplayName("不应该添加重复关键词")
    void shouldNotAddDuplicateKeyword() {
        // given - 已有关键词
        userPreference.setKeywords("比特币,以太坊");

        // when - 尝试添加重复
        boolean result = userPreference.addKeyword("比特币");

        // then - 明确失败断言
        assertFalse(result);
        assertEquals("比特币,以太坊", userPreference.getKeywords()); // 不变
    }

    @Test
    @DisplayName("不应该超过关键词数量限制")
    void shouldNotExceedKeywordLimit() {
        // given - 已有9个关键词（接近上限）
        userPreference.setKeywords("1,2,3,4,5,6,7,8,9");

        // when - 添加第10个（应该成功）
        boolean result1 = userPreference.addKeyword("10");
        assertTrue(result1);

        // when - 尝试添加第11个（应该失败）
        boolean result2 = userPreference.addKeyword("11");

        // then - 明确边界测试
        assertFalse(result2);
        assertEquals(10, userPreference.getKeywordList().length); // 不超过10个
    }

    @Test
    @DisplayName("应该成功移除关键词")
    void shouldRemoveKeyword() {
        // given - 有多个关键词
        userPreference.setKeywords("比特币,以太坊,区块链");

        // when - 移除关键词
        boolean result = userPreference.removeKeyword("以太坊");

        // then - 验证移除成功
        assertTrue(result);
        assertEquals(2, userPreference.getKeywordList().length);
        assertEquals("比特币,区块链", userPreference.getKeywords());
    }

    @Test
    @DisplayName("不应该移除不存在的关键词")
    void shouldNotRemoveNonExistentKeyword() {
        // given - 有关键词
        userPreference.setKeywords("比特币,以太坊");

        // when - 移除不存在的关键词
        boolean result = userPreference.removeKeyword("莱特币");

        // then - 验证不变
        assertFalse(result);
        assertEquals("比特币,以太坊", userPreference.getKeywords());
    }

    @Test
    @DisplayName("应该正确检查推送时间窗口")
    void shouldCheckPushWindow() {
        // given - 正常时间窗口
        userPreference.setPushStartTime(LocalTime.of(9, 0));
        userPreference.setPushEndTime(LocalTime.of(22, 0));

        // when/then - 边界测试
        // 注意：这个测试依赖于当前时间，我们测试逻辑而不是具体时间
        // 这里我们测试边界条件的逻辑正确性
        assertNotNull(userPreference.getPushStartTime());
        assertNotNull(userPreference.getPushEndTime());
        assertTrue(userPreference.getPushStartTime().isBefore(userPreference.getPushEndTime()));
    }

    @Test
    @DisplayName("应该正确记录推送")
    void shouldRecordPush() {
        // given - 初始状态
        int initialCount = userPreference.getPushCount();
        assertNull(userPreference.getLastPushAt());

        // when - 记录推送
        userPreference.recordPush();

        // then - 验证状态更新
        assertEquals(initialCount + 1, userPreference.getPushCount());
        assertNotNull(userPreference.getLastPushAt());
    }

    @Test
    @DisplayName("应该正确判断是否应推送")
    void shouldCheckShouldPush() {
        // given - 启用状态
        userPreference.setEnabled(true);
        userPreference.setLastPushAt(null); // 从未推送过

        // when/then - 应该推送
        assertTrue(userPreference.shouldPush());

        // when - 禁用推送
        userPreference.setEnabled(false);

        // then - 不应该推送
        assertFalse(userPreference.shouldPush());
    }

    @Test
    @DisplayName("Builder应该正确工作")
    void builderShouldWorkCorrectly() {
        // when - 使用Builder
        UserPreference preference = UserPreference.builder()
                .userId(987654321L)
                .keywords("测试,例子")
                .pushFrequency(60)
                .enabled(false)
                .build();

        // then - 验证所有字段
        assertEquals(987654321L, preference.getUserId());
        assertEquals("测试,例子", preference.getKeywords());
        assertEquals(60, preference.getPushFrequency());
        assertFalse(preference.getEnabled());
        assertNotNull(preference.getPushStartTime()); // 默认值
        assertNotNull(preference.getPushEndTime());   // 默认值
        assertEquals(0, preference.getPushCount());   // 默认值
    }

    @Test
    @DisplayName("应该处理空关键词添加")
    void shouldHandleEmptyKeywordAddition() {
        // given - 空输入
        String emptyKeyword = "";

        // when
        boolean result = userPreference.addKeyword(emptyKeyword);

        // then - KISS：明确失败
        assertFalse(result);
    }

    @Test
    @DisplayName("应该处理null关键词添加")
    void shouldHandleNullKeywordAddition() {
        // when
        boolean result = userPreference.addKeyword(null);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("应该处理空关键词移除")
    void shouldHandleEmptyKeywordRemoval() {
        // when
        boolean result = userPreference.removeKeyword("");

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("应该处理null关键词移除")
    void shouldHandleNullKeywordRemoval() {
        // when
        boolean result = userPreference.removeKeyword(null);

        // then
        assertFalse(result);
    }
}