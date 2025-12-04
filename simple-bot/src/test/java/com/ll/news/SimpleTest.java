package com.ll.news;

import com.ll.news.entity.UserPreference;

import java.time.LocalTime;

/**
 * 简单的手动测试验证
 * 遵循KISS原则：简单、直接、有效
 */
public class SimpleTest {

    public static void main(String[] args) {
        System.out.println("🚀 开始简单测试验证");

        // 测试1: 实体创建
        testEntityCreation();

        // 测试2: 关键词管理
        testKeywordManagement();

        // 测试3: 推送逻辑
        testPushLogic();

        System.out.println("✅ 所有简单测试通过！");
    }

    private static void testEntityCreation() {
        System.out.println("\n📋 测试1: 实体创建");

        UserPreference preference = UserPreference.builder()
                .userId(123456789L)
                .keywords("比特币,以太坊")
                .pushFrequency(30)
                .pushStartTime(LocalTime.of(9, 0))
                .pushEndTime(LocalTime.of(22, 0))
                .enabled(true)
                .pushCount(0)
                .build();

        assert preference.getUserId() == 123456789L;
        assert preference.getKeywords().equals("比特币,以太坊");
        assert preference.getPushFrequency() == 30;
        assert preference.getEnabled() == true;

        System.out.println("✅ 实体创建测试通过");
    }

    private static void testKeywordManagement() {
        System.out.println("\n🔑 测试2: 关键词管理");

        UserPreference preference = UserPreference.builder()
                .userId(123456789L)
                .keywords("")
                .build();

        // 测试添加关键词
        boolean result1 = preference.addKeyword("比特币");
        assert result1 == true;
        assert preference.getKeywords().equals("比特币");

        // 测试重复添加
        boolean result2 = preference.addKeyword("比特币");
        assert result2 == false; // 不应该重复添加

        // 测试移除关键词
        boolean result3 = preference.removeKeyword("比特币");
        assert result3 == true;
        assert preference.getKeywords().equals("");

        System.out.println("✅ 关键词管理测试通过");
    }

    private static void testPushLogic() {
        System.out.println("\n📊 测试3: 推送逻辑");

        UserPreference preference = UserPreference.builder()
                .userId(123456789L)
                .enabled(true)
                .build();

        // 测试是否应该推送（从未推送过）
        assert preference.shouldPush() == true;

        // 测试记录推送
        preference.recordPush();
        assert preference.getPushCount() == 1;
        assert preference.getLastPushAt() != null;

        // 测试禁用状态
        preference.setEnabled(false);
        assert preference.shouldPush() == false;

        System.out.println("✅ 推送逻辑测试通过");
    }
}