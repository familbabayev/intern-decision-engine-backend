package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.exceptions.*;
import org.springframework.stereotype.Service;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier,
 * which is determined by the last four digits of their ID code.
 */
@Service
public class DecisionEngine {

    // Used to check for the validity of the presented ID code.
    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private int creditModifier = 0;

    /**
     * Calculates the maximum loan amount and period for the customer based on their ID code,
     * the requested loan amount and the loan period.
     * The loan period must be between 12 and 60 months (inclusive).
     * The loan amount must be between 2000 and 10000€ months (inclusive).
     *
     * @param personalCode ID code of the customer that made the request.
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @return A Decision object containing the approved loan amount and period, and an error message (if any)
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * @throws NoValidLoanException If there is no valid loan found for the given ID code, loan amount and loan period
     */
    public Decision calculateApprovedLoan(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException,
            NoValidLoanException, InvalidAgeException {

        verifyInputs(personalCode, loanAmount, loanPeriod);

        int age = AgeUtil.calculateAgeFromPersonalCode(personalCode);
        String countryCode = personalCode.substring(0, 2);
        int ageLimit = CountryAgeLimit.getAgeLimit(countryCode);

        if (age < 18 || age > ageLimit) {
            throw new InvalidAgeException("Customer age " + age + " is outside of the approved range.");
        }

        creditModifier = getCreditModifier(personalCode);
        if (creditModifier == 0) {
            throw new NoValidLoanException("No valid loan found!");
        }

        int approvedLoanAmount = highestValidLoanAmount(loanPeriod);
        while (approvedLoanAmount == 0 && loanPeriod < DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            loanPeriod++;
            approvedLoanAmount = highestValidLoanAmount(loanPeriod);
        }

        if (approvedLoanAmount == 0) {
            throw new NoValidLoanException("No valid loan found!");
        }

        return new Decision(approvedLoanAmount, loanPeriod, null);
    }

    /**
     * Calculates the credit score based on the credit modifier, loan amount, and loan period.
     *
     * @param  creditModifier  the credit modifier value
     * @param  loanAmount      the requested loan amount
     * @param  loanPeriod      the requested loan period
     * @return                the calculated credit score
     */
    private double calculateCreditScore(int creditModifier, int loanAmount, int loanPeriod) {
        return (double) creditModifier / loanAmount * loanPeriod;
    }


    /**
     * Finds the highest valid loan amount for a given loan period.
     *
     * @param  loanPeriod  the loan period for which to find the highest valid loan amount
     * @return             the highest valid loan amount for the given loan period
     */
    private int highestValidLoanAmount(int loanPeriod) {
        for (int amount = DecisionEngineConstants.MINIMUM_LOAN_AMOUNT; amount <= DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT; amount++) {
            if (calculateCreditScore(creditModifier, amount, loanPeriod) >= 1) {
                return amount;
            }
        }
        return 0;
    }


    /**
     * Calculates the credit modifier of the customer to according to the last four digits of their ID code.
     * Debt - 0000...2499
     * Segment 1 - 2500...4999
     * Segment 2 - 5000...7499
     * Segment 3 - 7500...9999
     *
     * @param personalCode ID code of the customer that made the request.
     * @return Segment to which the customer belongs.
     */
    private int getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));

        if (segment < 2500) {
            return 0;
        } else if (segment < 5000) {
            return DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER;
        } else if (segment < 7500) {
            return DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER;
        }

        return DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER;
    }

    /**
     * Verify that all inputs are valid according to business rules.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     */
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod)
            throws InvalidPersonalCodeException, InvalidLoanAmountException, InvalidLoanPeriodException {

        if (!validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
        if ((DecisionEngineConstants.MINIMUM_LOAN_AMOUNT > loanAmount)
                || (loanAmount > DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT)) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }
        if ((DecisionEngineConstants.MINIMUM_LOAN_PERIOD > loanPeriod)
                || (loanPeriod > DecisionEngineConstants.MAXIMUM_LOAN_PERIOD)) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }

    }
}
