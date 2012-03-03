package org.jdatamig.utilities.enums;

/**
 * 'Language Options Enum' for McLink App.
 *
 * @author Babji, Chetty
 */
public enum LanguageOptions {
    ENGLISH ("en"),
    GERMAN ("de"),
    FRENCH ("fr");

    private String optionName; //key in message-bundle

    LanguageOptions(String optionName) {
        this.optionName = optionName;
    }

    public String getOptionName() {
        return optionName;
    }
}
