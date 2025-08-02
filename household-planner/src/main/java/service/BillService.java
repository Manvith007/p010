package service;

import model.*;
import util.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing utility bills and payments
 */
public class BillService {
    private static final String BILLS_FILE = "bills.dat";
    
    private Map<String, BillUtility> bills;
    private int nextId;
    
    public BillService() {
        this.bills = new HashMap<>();
        this.nextId = 1;
        loadData();
    }
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            Map<String, BillUtility> loadedBills = FileOperations.readObjectFromFile(BILLS_FILE, Map.class);
            if (loadedBills != null) {
                this.bills = loadedBills;
                // Calculate next ID
                this.nextId = bills.keySet().stream()
                    .mapToInt(key -> Integer.parseInt(key.split("-")[1]))
                    .max().orElse(0) + 1;
            }
            ExceptionLogger.getInstance().logInfo("Bills data loaded successfully");
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to load bills data", e);
        }
    }
    
    public void saveAllData() {
        try {
            FileOperations.writeObjectToFile(bills, BILLS_FILE);
            ExceptionLogger.getInstance().logInfo("Bills data saved successfully");
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to save bills data", e);
        }
    }
    
    // Bill management
    public String addBill(BillUtility bill) {
        if (bill == null) {
            return null;
        }
        
        String billId = "BILL-" + nextId++;
        bills.put(billId, bill);
        saveAllData();
        ExceptionLogger.getInstance().logInfo("Added bill: " + bill.getUtilityName() + " (ID: " + billId + ")");
        return billId;
    }
    
    public boolean updateBill(String billId, BillUtility updatedBill) {
        if (bills.containsKey(billId) && updatedBill != null) {
            bills.put(billId, updatedBill);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Updated bill: " + billId);
            return true;
        }
        return false;
    }
    
    public boolean deleteBill(String billId) {
        BillUtility removed = bills.remove(billId);
        if (removed != null) {
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Deleted bill: " + billId);
            return true;
        }
        return false;
    }
    
    public BillUtility getBill(String billId) {
        return bills.get(billId);
    }
    
    public List<BillUtility> getAllBills() {
        return new ArrayList<>(bills.values());
    }
    
    // Bill filtering and searching
    public List<BillUtility> getBillsByType(String type) {
        return bills.values().stream()
            .filter(bill -> type.equalsIgnoreCase(bill.getType()))
            .collect(Collectors.toList());
    }
    
    public List<BillUtility> getUnpaidBills() {
        return bills.values().stream()
            .filter(bill -> !bill.isPaid())
            .collect(Collectors.toList());
    }
    
    public List<BillUtility> getPaidBills() {
        return bills.values().stream()
            .filter(BillUtility::isPaid)
            .collect(Collectors.toList());
    }
    
    public List<BillUtility> getOverdueBills() {
        return bills.values().stream()
            .filter(BillUtility::isOverdue)
            .collect(Collectors.toList());
    }
    
    public List<BillUtility> getBillsDueToday() {
        LocalDate today = LocalDate.now();
        return bills.values().stream()
            .filter(bill -> today.equals(bill.getDueDate()) && !bill.isPaid())
            .collect(Collectors.toList());
    }
    
    public List<BillUtility> getBillsDueSoon(int days) {
        LocalDate cutoffDate = LocalDate.now().plusDays(days);
        return bills.values().stream()
            .filter(bill -> !bill.isPaid() && 
                          bill.getDueDate() != null &&
                          !bill.getDueDate().isBefore(LocalDate.now()) &&
                          !bill.getDueDate().isAfter(cutoffDate))
            .collect(Collectors.toList());
    }
    
    public List<BillUtility> getRecurringBills() {
        return bills.values().stream()
            .filter(BillUtility::isRecurring)
            .collect(Collectors.toList());
    }
    
    public List<BillUtility> searchBills(String searchTerm) {
        String lowerTerm = searchTerm.toLowerCase();
        return bills.values().stream()
            .filter(bill -> bill.getUtilityName().toLowerCase().contains(lowerTerm) ||
                          bill.getType().toLowerCase().contains(lowerTerm) ||
                          (bill.getNotes() != null && bill.getNotes().toLowerCase().contains(lowerTerm)))
            .collect(Collectors.toList());
    }
    
    // Bill actions
    public boolean markBillAsPaid(String billId) {
        BillUtility bill = bills.get(billId);
        if (bill != null) {
            bill.markAsPaid();
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Marked bill as paid: " + billId);
            return true;
        }
        return false;
    }
    
    public boolean markBillAsUnpaid(String billId) {
        BillUtility bill = bills.get(billId);
        if (bill != null) {
            bill.setPaid(false);
            bill.setLastPaidDate(null);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Marked bill as unpaid: " + billId);
            return true;
        }
        return false;
    }
    
    // Financial calculations
    public double getTotalUnpaidAmount() {
        return bills.values().stream()
            .filter(bill -> !bill.isPaid())
            .mapToDouble(BillUtility::getAmount)
            .sum();
    }
    
    public double getTotalOverdueAmount() {
        return bills.values().stream()
            .filter(BillUtility::isOverdue)
            .mapToDouble(BillUtility::getAmount)
            .sum();
    }
    
    public double getTotalPaidAmount() {
        return bills.values().stream()
            .filter(BillUtility::isPaid)
            .mapToDouble(BillUtility::getAmount)
            .sum();
    }
    
    public double getMonthlyRecurringTotal() {
        return bills.values().stream()
            .filter(BillUtility::isRecurring)
            .mapToDouble(BillUtility::getAmount)
            .sum();
    }
    
    public Map<String, Double> getTotalAmountByType() {
        return bills.values().stream()
            .collect(Collectors.groupingBy(
                BillUtility::getType,
                Collectors.summingDouble(BillUtility::getAmount)
            ));
    }
    
    // Sorting
    public List<BillUtility> sortBillsByDueDate() {
        return bills.values().stream()
            .filter(bill -> bill.getDueDate() != null)
            .sorted(Comparator.comparing(BillUtility::getDueDate))
            .collect(Collectors.toList());
    }
    
    public List<BillUtility> sortBillsByAmount() {
        return bills.values().stream()
            .sorted(Comparator.comparing(BillUtility::getAmount).reversed())
            .collect(Collectors.toList());
    }
    
    public List<BillUtility> sortBillsByName() {
        return bills.values().stream()
            .sorted(Comparator.comparing(BillUtility::getUtilityName))
            .collect(Collectors.toList());
    }
    
    // Statistics and reporting
    public Map<String, Object> getBillStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalBills", bills.size());
        stats.put("paidBills", getPaidBills().size());
        stats.put("unpaidBills", getUnpaidBills().size());
        stats.put("overdueBills", getOverdueBills().size());
        stats.put("billsDueToday", getBillsDueToday().size());
        stats.put("recurringBills", getRecurringBills().size());
        
        stats.put("totalUnpaidAmount", getTotalUnpaidAmount());
        stats.put("totalOverdueAmount", getTotalOverdueAmount());
        stats.put("totalPaidAmount", getTotalPaidAmount());
        stats.put("monthlyRecurringTotal", getMonthlyRecurringTotal());
        
        // Type distribution
        Map<String, Long> typeDistribution = bills.values().stream()
            .collect(Collectors.groupingBy(BillUtility::getType, Collectors.counting()));
        stats.put("typeDistribution", typeDistribution);
        
        stats.put("amountByType", getTotalAmountByType());
        
        return stats;
    }
    
    // Reminders and notifications
    public List<String> getPaymentReminders() {
        List<String> reminders = new ArrayList<>();
        
        // Overdue bills
        List<BillUtility> overdueBills = getOverdueBills();
        for (BillUtility bill : overdueBills) {
            long daysOverdue = bill.getDueDate().until(LocalDate.now()).getDays();
            reminders.add("OVERDUE: " + bill.getUtilityName() + " - $" + bill.getAmount() + 
                         " was due " + daysOverdue + " days ago");
        }
        
        // Bills due soon
        List<BillUtility> upcomingBills = getBillsDueSoon(7);
        for (BillUtility bill : upcomingBills) {
            long daysUntilDue = bill.getDaysUntilDue();
            if (daysUntilDue == 0) {
                reminders.add("DUE TODAY: " + bill.getUtilityName() + " - $" + bill.getAmount());
            } else {
                reminders.add("DUE IN " + daysUntilDue + " DAYS: " + bill.getUtilityName() + " - $" + bill.getAmount());
            }
        }
        
        return reminders;
    }
    
    // Bulk operations
    public int processRecurringBills() {
        int processed = 0;
        LocalDate today = LocalDate.now();
        
        for (BillUtility bill : getRecurringBills()) {
            if (bill.isPaid() && bill.getDueDate().isBefore(today)) {
                // Reset for next cycle
                bill.setPaid(false);
                bill.setDueDate(bill.getDueDate().plus(bill.getRecurringInterval()));
                processed++;
            }
        }
        
        if (processed > 0) {
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Processed " + processed + " recurring bills");
        }
        
        return processed;
    }
    
    public int markLateFeesForOverdueBills(double lateFeePercentage) {
        int updated = 0;
        
        for (BillUtility bill : getOverdueBills()) {
            if (!bill.getNotes().contains("Late fee applied")) {
                double lateFee = bill.getAmount() * (lateFeePercentage / 100.0);
                bill.setAmount(bill.getAmount() + lateFee);
                bill.setNotes((bill.getNotes() != null ? bill.getNotes() + " " : "") + 
                             "Late fee applied: $" + String.format("%.2f", lateFee));
                updated++;
            }
        }
        
        if (updated > 0) {
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Applied late fees to " + updated + " overdue bills");
        }
        
        return updated;
    }
    
    // Export functionality
    public String exportBillsToCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("Utility Name,Type,Amount,Due Date,Paid,Last Paid Date,Recurring,Notes\n");
        
        for (BillUtility bill : bills.values()) {
            csv.append(escapeCSV(bill.getUtilityName())).append(",");
            csv.append(escapeCSV(bill.getType())).append(",");
            csv.append(bill.getAmount()).append(",");
            csv.append(bill.getDueDate() != null ? bill.getDueDate().toString() : "").append(",");
            csv.append(bill.isPaid()).append(",");
            csv.append(bill.getLastPaidDate() != null ? bill.getLastPaidDate().toString() : "").append(",");
            csv.append(bill.isRecurring()).append(",");
            csv.append(escapeCSV(bill.getNotes() != null ? bill.getNotes() : "")).append("\n");
        }
        
        return csv.toString();
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}