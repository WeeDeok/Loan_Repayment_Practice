import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class LoanRepayment {

    private double loanAmount, annualInterestRate, monthlyInterestRate;
    private int loanTermMonths;

    public LoanRepayment(double loanAmount, double annualInterestRate, int loanTermMonths) {
        this.loanAmount = loanAmount;
        this.annualInterestRate = annualInterestRate;
        this.loanTermMonths = loanTermMonths;
        this.monthlyInterestRate = annualInterestRate / 100 / 12;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 

        double loanAmount = getValidInput(scanner, "대출 금액을 입력하세요 (원): ", 0);
        double annualInterestRate = getValidInput(scanner, "연 이자율을 입력하세요 (백분율): ", 0, 100);
        int loanTermMonths = (int) getValidInput(scanner, "대출 기간을 입력하세요 (개월): ", 1, Integer.MAX_VALUE);

        LoanRepayment repayment = new LoanRepayment(loanAmount, annualInterestRate, loanTermMonths);
        int method = (int) repayment.getValidInput(scanner, "상환 방식을 선택하세요 (1: 원리금 균등, 2: 만기 일시): ", 1, 2);

        if (method == 1) {
            double monthlyRepayment = repayment.calculateMonthlyRepayment();
            System.out.println("\n원리금 균등 상환 월 금액: " + String.format("%.2f", monthlyRepayment));
            repayment.printRepaymentSchedule(monthlyRepayment, sdf);
        } else {
            double monthlyInterestPayment = repayment.loanAmount * repayment.monthlyInterestRate;
            System.out.println("\n만기 일시 상환 월 이자 금액: " + String.format("%.2f", monthlyInterestPayment));
            repayment.printMaturityRepaymentSchedule(sdf);
        }

        scanner.close();
    }

    public double calculateMonthlyRepayment() {
        return this.loanAmount * (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, loanTermMonths)) / 
               (Math.pow(1 + monthlyInterestRate, loanTermMonths) - 1);
    }

    public void printRepaymentSchedule(double monthlyRepayment, SimpleDateFormat sdf) {
        double remainingLoan = loanAmount;
        Calendar calendar = Calendar.getInstance();
        HashSet<String> holidays = getHolidays();

        System.out.println("\n상환 스케줄:");
        System.out.printf("%-15s%-10s%-15s%-15s%-15s\n", "상환일", "월", "이자 (원)", "원금 (원)", "남은 대출금 (원)");

        for (int month = 1; month <= loanTermMonths; month++) {
            calendar.add(Calendar.MONTH, 1);
            String repaymentDate = getNextBusinessDay(sdf.format(calendar.getTime()), holidays, sdf, calendar);
            double interestPayment = remainingLoan * monthlyInterestRate;
            double principalPayment = monthlyRepayment - interestPayment;
            remainingLoan -= principalPayment;
            System.out.printf("%-15s%-10d%-15.2f%-15.2f%-15.2f\n", repaymentDate, month, interestPayment, principalPayment, remainingLoan);

            if (remainingLoan <= 0) break;
        }
    }

    public void printMaturityRepaymentSchedule(SimpleDateFormat sdf) {
        Calendar calendar = Calendar.getInstance();
        String repaymentDate;
        HashSet<String> holidays = getHolidays();

        System.out.println("\n상환 스케줄:");
        for (int month = 1; month <= loanTermMonths; month++) {
            calendar.add(Calendar.MONTH, 1);
            repaymentDate = getNextBusinessDay(sdf.format(calendar.getTime()), holidays, sdf, calendar);
            double interestPayment = loanAmount * monthlyInterestRate;
            System.out.printf("%-15s%-10d%-15.2f%-15.2f%-15.2f\n", repaymentDate, month, interestPayment, 0.0, loanAmount);
        }
        calendar.add(Calendar.MONTH, 1);
        repaymentDate = sdf.format(calendar.getTime());
        System.out.println("만기일에 원금을 일시 상환합니다. 만기일: " + repaymentDate);
    }
  
    public String getNextBusinessDay(String repaymentDate, HashSet<String> holidays, SimpleDateFormat sdf, Calendar calendar) { 
        calendar.setTime(parseDate(repaymentDate, sdf));
        //calendar.setTime(sdf.parse(repaymentDate));
        while (isWeekend(calendar) || isHoliday(repaymentDate, holidays)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            repaymentDate = sdf.format(calendar.getTime());
        }
        return repaymentDate;
    }

    public Date parseDate(String dateStr, SimpleDateFormat sdf) {
        try {
            return sdf.parse(dateStr);
            //return sdf;
        } catch (Exception e) {
            e.printStackTrace();
            return null;  
        }
    }

    public boolean isWeekend(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    public boolean isHoliday(String date, HashSet<String> holidays) {
        return holidays.contains(date);
    }

    public HashSet<String> getHolidays() {
        HashSet<String> holidays = new HashSet<>();
        holidays.add("2025-05-01"); // 예시 공휴일
        holidays.add("2025-12-25"); // 예시 공휴일
        return holidays;
    }

    public static double getValidInput(Scanner scanner, String prompt, double min) {
        double input;
        while ((input = scanner.nextDouble()) < min) {
            System.out.print(prompt);
        }
        return input;
    }

    public static double getValidInput(Scanner scanner, String prompt, double min, double max) {
        double input;
        while ((input = scanner.nextDouble()) < min || input > max) {
            System.out.print(prompt);
        }
        return input;
    }
}
