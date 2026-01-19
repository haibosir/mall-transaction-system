package com.mall.job;

import com.mall.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 商家结算定时任务
 * 每天凌晨2点执行商家结算
 *
 * @author mall
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementJob {

    private final SettlementService settlementService;

    /**
     * 每天凌晨2点执行商家结算
     * cron表达式：秒 分 时 日 月 周
     * 0 0 2 * * ? 表示每天凌晨2点执行
     */
    @Scheduled(cron = "${settlement.job.cron:0 0 2 * * ?}")
    public void executeSettlement() {
        log.info("开始执行商家结算定时任务");
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            settlementService.settleAllMerchants(yesterday);
            log.info("商家结算定时任务执行完成");
        } catch (Exception e) {
            log.error("商家结算定时任务执行失败", e);
        }
    }
}
