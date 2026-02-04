package services;

import pages.LeaveApplicationsPage;
import models.PendingLeaveRow;
import models.LeaveRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaveReportService {

    private final LeaveApplicationsPage leavePage;

    public LeaveReportService(LeaveApplicationsPage leavePage) {
        this.leavePage = leavePage;
    }

    // ===== Leave History Summary =====
    public Map<String, double[]> getLeaveHistorySummary() {

        List<LeaveRow> rows = leavePage.fetchLeaveHistoryRows();

        Map<String, double[]> summary = new HashMap<>();

        for (LeaveRow row : rows) {
            summary.putIfAbsent(row.employee, new double[]{0, 0});
            summary.get(row.employee)[0]++;       // transactions
            summary.get(row.employee)[1] += row.days;
        }
        return summary;
    }

    // ===== Pending Leave Summary =====
    public Map<String, double[]> getPendingSummary(
            List<PendingLeaveRow> pendingLeaves) {

        Map<String, double[]> summary = new HashMap<>();

        for (PendingLeaveRow row : pendingLeaves) {
            summary.putIfAbsent(row.employee, new double[]{0, 0});
            summary.get(row.employee)[0]++;
            summary.get(row.employee)[1] += row.days;
        }
        return summary;
    }
}
