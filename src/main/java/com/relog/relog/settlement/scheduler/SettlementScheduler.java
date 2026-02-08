package com.relog.relog.settlement.scheduler;

import com.relog.relog.member.entity.RelogMember;
import com.relog.relog.member.repository.RelogMemberRepository;
import com.relog.relog.settlement.service.SettlementService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final SettlementService settlementService;
    private final RelogMemberRepository relogMemberRepository;

    @Scheduled(cron = "0 0 2 1 * *")
    public void generateMonthlySettlements() {
        LocalDate previousMonth = LocalDate.now().minusMonths(1);
        int year = previousMonth.getYear();
        int month = previousMonth.getMonthValue();

        log.info("[Scheduler] Starting monthly settlement for {}-{}", year, month);

        List<RelogMember> members = relogMemberRepository.findAll();
        int successCount = 0;
        int failCount = 0;

        for (RelogMember member : members) {
            try {
                settlementService.getMonthlySettlement(member.getId(), year, month);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("[Scheduler] Monthly settlement failed for memberId={}: {}", member.getId(), e.getMessage());
            }
        }

        log.info("[Scheduler] Monthly settlement completed. success={}, failed={}", successCount, failCount);
    }

    @Scheduled(cron = "0 0 3 1 1,4,7,10 *")
    public void generateQuarterlySettlements() {
        LocalDate previousQuarterDate = LocalDate.now().minusMonths(3);
        int year = previousQuarterDate.getYear();
        int quarter = (previousQuarterDate.getMonthValue() - 1) / 3 + 1;

        log.info("[Scheduler] Starting quarterly settlement for {}-Q{}", year, quarter);

        List<RelogMember> members = relogMemberRepository.findAll();
        int successCount = 0;
        int failCount = 0;

        for (RelogMember member : members) {
            try {
                settlementService.getQuarterlySettlement(member.getId(), year, quarter);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("[Scheduler] Quarterly settlement failed for memberId={}: {}", member.getId(), e.getMessage());
            }
        }

        log.info("[Scheduler] Quarterly settlement completed. success={}, failed={}", successCount, failCount);
    }
}
