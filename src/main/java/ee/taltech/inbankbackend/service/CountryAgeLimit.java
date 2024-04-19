package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;

public class CountryAgeLimit {

    private CountryAgeLimit() { }

    public static final int ESTONIA_EXPECTED_LIFETIME = 81;
    public static final int LATVIA_EXPECTED_LIFETIME = 79;
    public static final int LITHUANIA_EXPECTED_LIFETIME = 77;

    public static int getAgeLimit(String countryCode) {
        return switch (countryCode) {
            case "EE" -> ESTONIA_EXPECTED_LIFETIME - DecisionEngineConstants.MAXIMUM_LOAN_PERIOD;
            case "LV" -> LATVIA_EXPECTED_LIFETIME - DecisionEngineConstants.MAXIMUM_LOAN_PERIOD;
            case "LT" -> LITHUANIA_EXPECTED_LIFETIME - DecisionEngineConstants.MAXIMUM_LOAN_PERIOD;
            default -> throw new IllegalArgumentException("Unsupported country code");
        };
    }
}