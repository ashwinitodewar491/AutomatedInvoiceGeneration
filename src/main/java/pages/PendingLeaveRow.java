package pages;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingLeaveRow {
    public final String employee;
    public final String startDate;
    public final String endDate;
    public final double days;
    public final String type;
    public final String reason;

    public PendingLeaveRow(String employee, String startDate,
                           String endDate, double days,
                           String type, String reason) {
        this.employee = employee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.days = days;
        this.type = type;
        this.reason = reason;
    }
}
