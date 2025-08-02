package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Chain of Responsibility pattern for input validation
 */
public class ValidationChain {
    private List<ValidationRule> rules;
    private List<String> errors;
    
    public ValidationChain() {
        this.rules = new ArrayList<>();
        this.errors = new ArrayList<>();
    }
    
    public static class ValidationRule {
        private final Predicate<String> validator;
        private final String errorMessage;
        
        public ValidationRule(Predicate<String> validator, String errorMessage) {
            this.validator = validator;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid(String input) {
            return validator.test(input);
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    public ValidationChain addRule(Predicate<String> validator, String errorMessage) {
        rules.add(new ValidationRule(validator, errorMessage));
        return this;
    }
    
    public ValidationChain notEmpty(String fieldName) {
        return addRule(input -> input != null && !input.trim().isEmpty(), 
                      fieldName + " cannot be empty");
    }
    
    public ValidationChain minLength(int length, String fieldName) {
        return addRule(input -> input != null && input.length() >= length, 
                      fieldName + " must be at least " + length + " characters");
    }
    
    public ValidationChain maxLength(int length, String fieldName) {
        return addRule(input -> input != null && input.length() <= length, 
                      fieldName + " must be no more than " + length + " characters");
    }
    
    public ValidationChain isNumeric(String fieldName) {
        return addRule(input -> {
            try {
                Double.parseDouble(input);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }, fieldName + " must be a valid number");
    }
    
    public ValidationChain isPositive(String fieldName) {
        return addRule(input -> {
            try {
                return Double.parseDouble(input) > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }, fieldName + " must be a positive number");
    }
    
    public ValidationChain isValidDate(String fieldName) {
        return addRule(input -> {
            try {
                LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        }, fieldName + " must be a valid date (YYYY-MM-DD)");
    }
    
    public ValidationChain isPinFormat() {
        return addRule(input -> input != null && input.matches("\\d{4,8}"), 
                      "PIN must be 4-8 digits");
    }
    
    public boolean validate(String input) {
        errors.clear();
        boolean isValid = true;
        
        for (ValidationRule rule : rules) {
            if (!rule.isValid(input)) {
                errors.add(rule.getErrorMessage());
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public String getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }
}