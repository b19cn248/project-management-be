package com.ptit.projectmanagementbe.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageUtil {

    private final MessageSource messageSource;

    /**
     * Get localized message by code
     *
     * @param code   message code
     * @param locale current locale
     * @return localized message
     */
    public String getMessage(String code, Locale locale) {
        try {
            return messageSource.getMessage(code, null, locale);
        } catch (NoSuchMessageException e) {
            return code;
        }
    }

    /**
     * Get localized message by code with arguments
     *
     * @param code   message code
     * @param locale current locale
     * @param args   message arguments
     * @return localized message
     */
    public String getMessage(String code, Locale locale, Object... args) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            return code;
        }
    }

    /**
     * Get localized message by code with default message
     *
     * @param code           message code
     * @param defaultMessage default message
     * @param locale         current locale
     * @return localized message
     */
    public String getMessage(String code, String defaultMessage, Locale locale) {
        return messageSource.getMessage(code, null, defaultMessage, locale);
    }

    /**
     * Get localized message by code with arguments and default message
     *
     * @param code           message code
     * @param defaultMessage default message
     * @param locale         current locale
     * @param args           message arguments
     * @return localized message
     */
    public String getMessage(String code, String defaultMessage, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }
}
