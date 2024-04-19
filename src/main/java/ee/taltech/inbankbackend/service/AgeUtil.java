package ee.taltech.inbankbackend.service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class AgeUtil {

    private AgeUtil() { }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static int calculateAgeFromPersonalCode(String personalCode) {
        String birthDateStr = "19" + personalCode.substring(1, 7);
        LocalDate birthDate = LocalDate.parse(birthDateStr, formatter);
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}

