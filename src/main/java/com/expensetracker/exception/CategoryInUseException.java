package com.expensetracker.exception;

public class CategoryInUseException extends RuntimeException {

    public CategoryInUseException(String categoryName, long count) {
        super(String.format(
                "Cannot delete category '%s' because it has %d associated expense(s). "
                        + "Please reassign or delete those expenses first.",
                categoryName, count));
    }
}