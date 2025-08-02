package service;

import model.*;
import util.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing household chores and tasks
 */
public class ChoreService {
    private static final String CHORES_FILE = "chores.dat";
    
    private Map<String, Chore> chores;
    private int nextId;
    
    public ChoreService() {
        this.chores = new HashMap<>();
        this.nextId = 1;
        loadData();
    }
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            Map<String, Chore> loadedChores = FileOperations.readObjectFromFile(CHORES_FILE, Map.class);
            if (loadedChores != null) {
                this.chores = loadedChores;
                // Calculate next ID
                this.nextId = chores.keySet().stream()
                    .mapToInt(key -> Integer.parseInt(key.split("-")[1]))
                    .max().orElse(0) + 1;
            }
            ExceptionLogger.getInstance().logInfo("Chores data loaded successfully");
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to load chores data", e);
        }
    }
    
    public void saveAllData() {
        try {
            FileOperations.writeObjectToFile(chores, CHORES_FILE);
            ExceptionLogger.getInstance().logInfo("Chores data saved successfully");
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to save chores data", e);
        }
    }
    
    // Chore management
    public String addChore(Chore chore) {
        if (chore == null) {
            return null;
        }
        
        String choreId = "CHORE-" + nextId++;
        chores.put(choreId, chore);
        saveAllData();
        ExceptionLogger.getInstance().logInfo("Added chore: " + chore.getDescription() + " (ID: " + choreId + ")");
        return choreId;
    }
    
    public boolean updateChore(String choreId, Chore updatedChore) {
        if (chores.containsKey(choreId) && updatedChore != null) {
            chores.put(choreId, updatedChore);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Updated chore: " + choreId);
            return true;
        }
        return false;
    }
    
    public boolean deleteChore(String choreId) {
        Chore removed = chores.remove(choreId);
        if (removed != null) {
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Deleted chore: " + choreId);
            return true;
        }
        return false;
    }
    
    public Chore getChore(String choreId) {
        return chores.get(choreId);
    }
    
    public List<Chore> getAllChores() {
        return new ArrayList<>(chores.values());
    }
    
    // Chore filtering and searching
    public List<Chore> getChoresByAssignee(String assignee) {
        return chores.values().stream()
            .filter(chore -> assignee.equals(chore.getAssignee()))
            .collect(Collectors.toList());
    }
    
    public List<Chore> getChoresByStatus(Chore.Status status) {
        return chores.values().stream()
            .filter(chore -> chore.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    public List<Chore> getChoresByPriority(Chore.Priority priority) {
        return chores.values().stream()
            .filter(chore -> chore.getPriority() == priority)
            .collect(Collectors.toList());
    }
    
    public List<Chore> getOverdueChores() {
        return chores.values().stream()
            .filter(Chore::isOverdue)
            .collect(Collectors.toList());
    }
    
    public List<Chore> getChoresDueToday() {
        LocalDate today = LocalDate.now();
        return chores.values().stream()
            .filter(chore -> today.equals(chore.getDueDate()))
            .collect(Collectors.toList());
    }
    
    public List<Chore> getChoresDueWithinDays(int days) {
        LocalDate cutoffDate = LocalDate.now().plusDays(days);
        return chores.values().stream()
            .filter(chore -> chore.getDueDate() != null && 
                           !chore.getDueDate().isAfter(cutoffDate) &&
                           !chore.getDueDate().isBefore(LocalDate.now()))
            .collect(Collectors.toList());
    }
    
    public List<Chore> getRecurringChores() {
        return chores.values().stream()
            .filter(Chore::isRecurring)
            .collect(Collectors.toList());
    }
    
    public List<Chore> searchChores(String searchTerm) {
        String lowerTerm = searchTerm.toLowerCase();
        return chores.values().stream()
            .filter(chore -> chore.getDescription().toLowerCase().contains(lowerTerm) ||
                           (chore.getNotes() != null && chore.getNotes().toLowerCase().contains(lowerTerm)) ||
                           (chore.getAssignee() != null && chore.getAssignee().toLowerCase().contains(lowerTerm)))
            .collect(Collectors.toList());
    }
    
    // Chore actions
    public boolean markChoreAsCompleted(String choreId) {
        Chore chore = chores.get(choreId);
        if (chore != null) {
            chore.markAsCompleted();
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Marked chore as completed: " + choreId);
            return true;
        }
        return false;
    }
    
    public boolean markChoreAsInProgress(String choreId) {
        Chore chore = chores.get(choreId);
        if (chore != null) {
            chore.setStatus(Chore.Status.IN_PROGRESS);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Marked chore as in progress: " + choreId);
            return true;
        }
        return false;
    }
    
    public boolean markChoreAsPending(String choreId) {
        Chore chore = chores.get(choreId);
        if (chore != null) {
            chore.setStatus(Chore.Status.PENDING);
            chore.setCompletedDate(null);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Marked chore as pending: " + choreId);
            return true;
        }
        return false;
    }
    
    public boolean cancelChore(String choreId) {
        Chore chore = chores.get(choreId);
        if (chore != null) {
            chore.setStatus(Chore.Status.CANCELLED);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Cancelled chore: " + choreId);
            return true;
        }
        return false;
    }
    
    // Chore assignment and rotation
    public boolean assignChore(String choreId, String assignee) {
        Chore chore = chores.get(choreId);
        if (chore != null) {
            chore.setAssignee(assignee);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Assigned chore " + choreId + " to " + assignee);
            return true;
        }
        return false;
    }
    
    public Map<String, Integer> getChoreDistributionByAssignee() {
        Map<String, Integer> distribution = new HashMap<>();
        
        for (Chore chore : chores.values()) {
            if (chore.getAssignee() != null) {
                distribution.merge(chore.getAssignee(), 1, Integer::sum);
            }
        }
        
        return distribution;
    }
    
    public List<String> suggestChoreRotation(List<String> users) {
        Map<String, Integer> currentDistribution = getChoreDistributionByAssignee();
        List<String> suggestions = new ArrayList<>();
        
        // Find users with fewer chores assigned
        int minChores = currentDistribution.values().stream().mapToInt(Integer::intValue).min().orElse(0);
        
        for (String user : users) {
            int userChores = currentDistribution.getOrDefault(user, 0);
            if (userChores <= minChores) {
                suggestions.add(user);
            }
        }
        
        return suggestions;
    }
    
    // Sorting
    public List<Chore> sortChoresByDueDate() {
        return chores.values().stream()
            .filter(chore -> chore.getDueDate() != null)
            .sorted(Comparator.comparing(Chore::getDueDate))
            .collect(Collectors.toList());
    }
    
    public List<Chore> sortChoresByPriority() {
        return chores.values().stream()
            .sorted(Comparator.comparing(Chore::getPriority).reversed())
            .collect(Collectors.toList());
    }
    
    public List<Chore> sortChoresByAssignee() {
        return chores.values().stream()
            .filter(chore -> chore.getAssignee() != null)
            .sorted(Comparator.comparing(Chore::getAssignee))
            .collect(Collectors.toList());
    }
    
    // Statistics
    public Map<String, Object> getChoreStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalChores", chores.size());
        stats.put("pendingChores", getChoresByStatus(Chore.Status.PENDING).size());
        stats.put("inProgressChores", getChoresByStatus(Chore.Status.IN_PROGRESS).size());
        stats.put("completedChores", getChoresByStatus(Chore.Status.COMPLETED).size());
        stats.put("cancelledChores", getChoresByStatus(Chore.Status.CANCELLED).size());
        stats.put("overdueChores", getOverdueChores().size());
        stats.put("choresDueToday", getChoresDueToday().size());
        stats.put("recurringChores", getRecurringChores().size());
        
        // Priority distribution
        Map<Chore.Priority, Long> priorityDistribution = chores.values().stream()
            .collect(Collectors.groupingBy(Chore::getPriority, Collectors.counting()));
        stats.put("priorityDistribution", priorityDistribution);
        
        // Assignee distribution
        stats.put("assigneeDistribution", getChoreDistributionByAssignee());
        
        return stats;
    }
    
    // Batch operations
    public int markOverdueChoresAsUrgent() {
        List<Chore> overdueChores = getOverdueChores();
        int updated = 0;
        
        for (Chore chore : overdueChores) {
            if (chore.getPriority() != Chore.Priority.URGENT) {
                chore.setPriority(Chore.Priority.URGENT);
                updated++;
            }
        }
        
        if (updated > 0) {
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Marked " + updated + " overdue chores as urgent");
        }
        
        return updated;
    }
    
    public int cancelExpiredChores(int daysOverdue) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysOverdue);
        int cancelled = 0;
        
        for (Chore chore : chores.values()) {
            if (chore.getDueDate() != null && 
                chore.getDueDate().isBefore(cutoffDate) && 
                chore.getStatus() != Chore.Status.COMPLETED &&
                chore.getStatus() != Chore.Status.CANCELLED) {
                chore.setStatus(Chore.Status.CANCELLED);
                chore.setNotes((chore.getNotes() != null ? chore.getNotes() + " " : "") + 
                             "Auto-cancelled after " + daysOverdue + " days overdue");
                cancelled++;
            }
        }
        
        if (cancelled > 0) {
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Auto-cancelled " + cancelled + " expired chores");
        }
        
        return cancelled;
    }
}