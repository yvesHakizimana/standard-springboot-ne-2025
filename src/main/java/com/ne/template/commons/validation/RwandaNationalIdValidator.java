package com.ne.template.commons.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;
import java.util.regex.Pattern;

/**
 * Validator for Rwandan National ID numbers.
 *
 * This validator implements the Jakarta {@link ConstraintValidator} interface to validate
 * Rwandan National ID numbers according to the official format specifications.
 *
 * A valid Rwandan National ID consists of 16 digits divided into 6 distinct functional groups:
 *
 * <pre>
 * Format: GYYYY#NNNNNNNIFCC
 *
 * Where:
 * G    - National Identifier (1 digit)
 * YYYY - Year of Birth (4 digits)
 * #    - Gender Identifier (1 digit)
 * NNNNNNN - Birth Order Number (7 digits)
 * I    - Issue Frequency (1 digit)
 * CC   - Security Code (2 digits)
 * </pre>
 *
 * @see ValidRwandaId
 */
public class RwandaNationalIdValidator implements ConstraintValidator<ValidRwandaId, String> {

    /**
     * Regular expression pattern for validating the structure of a Rwandan National ID.
     * The pattern captures 6 groups corresponding to the 6 functional components of the ID.
     */
    private static final String ID_PATTERN = "^(\\d)(\\d{4})([78])(\\d{7})(\\d)(\\d{2})$";
    private static final Pattern PATTERN = Pattern.compile(ID_PATTERN);

    /** Current year used for validating the birth year */
    private static final int CURRENT_YEAR = Year.now().getValue();


    private static final int MIN_ISSUANCE_AGE = 16;
    private static final int MAX_ISSUANCE_AGE = 120;

    /**
     * Validates a Rwandan National ID string.
     *
     * @param value The ID string to validate
     * @param context The constraint validator context
     * @return true if the ID is valid, false otherwise
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        if (value.length() != 16)
            return false;

        var matcher = PATTERN.matcher(value);
        // Checking the Structure of the NationalId.
        if (!matcher.matches())
            return false;

        // Extracting the groups
        String nationalityDigit = matcher.group(1);
        String birthYearStr = matcher.group(2);
        String genderDigit = matcher.group(3);
        String birthOrderDigits = matcher.group(4);
        String issueFrequencyDigit = matcher.group(5);
        String securityCodeDigits = matcher.group(6);

        // Validate Nationality
        if(!isValidNationality(nationalityDigit)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid nationality on the ID").addConstraintViolation();
            return false;
        }

        // Validate the birth year.
        if(!isValidBirthYear(birthYearStr)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid birth year on the ID").addConstraintViolation();
            return false;
        }

        // Validate the gender digit.
        if(!isValidGender(genderDigit)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid gender on the ID").addConstraintViolation();
            return false;
        }

        // Validate the birth order digits.
        if(!isValidBirthOrder(birthOrderDigits)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid birth order on the ID").addConstraintViolation();
            return false;
        }

        // Validate the security code digits.
        if(!isValidIssueFrequency(issueFrequencyDigit)){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid issue frequency on the ID").addConstraintViolation();
            return false;
        }

        return true;
    }

    /**
     * Validates the National Identifier digit (Group 1).
     *
     * The first group consists of a single digit that identifies the holder's status:
     * - 1 for Rwandan citizens
     * - 2 for refugees who have been issued an ID
     * - 3 for foreigners
     *
     * @param digit The nationality digit to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidNationality(String digit){
        return digit.matches("[123]"); // 1=Rwandan 2=Refugee 3=Foreigner
    }

    /**
     * Validates the Year of Birth (Group 2).
     * The second group comprises four digits that correspond to the year of birth of the ID holder.
     * The age at issuance must be greater than 16 and less than 120 reasonably.
     *
     * @param yearStr The birth year strings to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidBirthYear(String yearStr){
        try {
            int birthYear = Integer.parseInt(yearStr);
            int ageAtIssuance = CURRENT_YEAR - birthYear;

            return ageAtIssuance >= MIN_ISSUANCE_AGE && ageAtIssuance <= MAX_ISSUANCE_AGE;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates the Gender Identifier (Group 3).
     * The third group is a single digit indicating the gender of the ID holder:
     * - 8 for males
     * - 7 for females
     *
     * @param digit The gender digit to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidGender(String digit){
        return digit.matches("[78]"); // 7=Female 8=Male
    }

    /**
     * Validates the Birth Order Number (Group 4).
     * The fourth group consists of seven digits that represent the sequential order
     * in which the ID was issued to individuals born in the same year.
     * This number indicates how many men or women born in that specific year
     * have registered in the country.
     *
     * @param digits The birth order digits to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidBirthOrder(String digits){
        // Must be seven digits not all zeros.
        return digits.matches("\\d{7}") && !digits.matches("0+");
    }

    /**
     * Validates the Issue Frequency (Group 5).
     * The fifth group contains a single digit indicating the number of times the ID
     * has been issued. If it is the first time the ID is issued to the holder,
     * this digit is 0. This helps track the issuance history of the ID card.
     *
     * @param digit The issue frequency digit to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidIssueFrequency(String digit){
        // Single digit
        return digit.matches("\\d"); // 0-9
    }

    /**
     * Validates the Security Code (Group 6).
     *
     * The sixth and final group is composed of two digits. These digits form a unique
     * check sum that serves as a security feature to prevent counterfeiting.
     * The specific meaning of this group is known only to the National Identification Agency (NIDA).
     *
     * @param digits The security code digits to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidSecurityCode(String digits){
        // Two digits
        return digits.matches("\\d{2}"); // 00-99
    }
}