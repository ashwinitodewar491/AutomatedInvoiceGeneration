package models;

public class LeaveRow {
    public String employeeId;
    public String employee;
    public double days;

    public LeaveRow(String employeeId,String employee, double days) {
        this.employeeId=employeeId;
        this.employee = employee;
        this.days = days;
    }
}
