package com.kerneldc.ipm.domain.projection;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.data.rest.core.config.Projection;

import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.PaymentFrequencyEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentBond;

@Projection(name = "instrumentBondInlineInstrument", types = { InstrumentBond.class })
public interface IInstrumentBondInlineInstrument {

	Long getId();
	String getIssuer();
	String getCusip();
    BigDecimal getPrice();
    BigDecimal getCoupon();
    OffsetDateTime getIssueDate();
    OffsetDateTime getMaturityDate();
    PaymentFrequencyEnum getPaymentFrequency();
    OffsetDateTime getNextPaymentDate();
    Boolean getEmailNotification();
        
    Long getRowVersion();

	Instrument getInstrument();
}
