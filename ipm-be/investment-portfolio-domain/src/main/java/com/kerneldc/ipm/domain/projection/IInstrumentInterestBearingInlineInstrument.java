package com.kerneldc.ipm.domain.projection;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.data.rest.core.config.Projection;

import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.TermEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;

@Projection(name = "instrumentInterestBearingInlineInstrument", types = { InstrumentInterestBearing.class })
public interface IInstrumentInterestBearingInlineInstrument {

	Long getId();
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
    Long getRowVersion();

	Instrument getInstrument();
}
