package org.jdatamig.utilities.enums;

/**
 * 'Link Options Enum' for McLink App.
 * 
 * @author Babji, Chetty
 */
public enum DataMigrationOptions {
    EXPORT ("export"),    
    IMPORT ("import");

    private String optionName; //key in message-bundle

    DataMigrationOptions(String optionName) {
        this.optionName = optionName;
    }
    
    public String getOptionName() {
        return optionName;
    }
}
