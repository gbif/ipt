package org.gbif.ipt.validation;

import java.util.HashMap;
import java.util.Map;

public final class EmailValidationMessageTranslator {

    private EmailValidationMessageTranslator() {
    }

    public static final Map<String, String> EMAIL_ERROR_TRANSLATIONS = new HashMap<>();

    static {
        EMAIL_ERROR_TRANSLATIONS.put("Missing final '@domain'", "validation.email.error.domain.missingFinal");
        EMAIL_ERROR_TRANSLATIONS.put("Missing domain", "validation.email.error.domain.missing");
        EMAIL_ERROR_TRANSLATIONS.put("Domain starts with dot", "validation.email.error.domain.startsWithDot");
        EMAIL_ERROR_TRANSLATIONS.put("Domain contains control or whitespace", "validation.email.error.domain.containsControl");
        EMAIL_ERROR_TRANSLATIONS.put("Domain contains illegal character", "validation.email.error.domain.containsIllegal");
        EMAIL_ERROR_TRANSLATIONS.put("Domain contains dot-dot", "validation.email.error.domain.containsDotDot");
        EMAIL_ERROR_TRANSLATIONS.put("Domain ends with dot", "validation.email.error.domain.endsWithDot");
        EMAIL_ERROR_TRANSLATIONS.put("Missing local name", "validation.email.error.localAddress.missing");
        EMAIL_ERROR_TRANSLATIONS.put("Local address contains control or whitespace", "validation.email.error.localAddress.containsControl");
        EMAIL_ERROR_TRANSLATIONS.put("Local address contains illegal character", "validation.email.error.localAddress.containsIllegal");
    }
}
