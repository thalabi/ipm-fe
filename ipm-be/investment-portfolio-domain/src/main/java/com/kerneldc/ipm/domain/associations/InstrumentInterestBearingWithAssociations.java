package com.kerneldc.ipm.domain.associations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.data.rest.core.config.Projection;

import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.TermEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;

@Projection(name = "interestBearingWithAssociations", types = { InstrumentInterestBearing.class })
public interface InstrumentInterestBearingWithAssociations {

    String getType();
    FinancialInstitutionEnum getFinancialInstitution();
    BigDecimal getPrice();
    Float getInterestRate();
    TermEnum getTerm();
    OffsetDateTime getMaturityDate();
    OffsetDateTime getNextPaymentDate();
    Float getPromotionalInterestRate();
    OffsetDateTime getPromotionEndDate();
    Boolean getEmailNotification();

	Instrument getInstrument();
}
