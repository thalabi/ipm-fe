package com.kerneldc.ipm.batch.pricing;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.Instrument;
import com.kerneldc.ipm.domain.InstrumentTypeEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentMutualFund;
import com.kerneldc.ipm.repository.PriceRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MutualFundPriceService implements ITradingInstrumentPricingService<InstrumentMutualFund> {

	private static final String THE_GLOBE_AND_MAIL_BASE_URL = "https://www.theglobeandmail.com/investing/markets/funds/";
	
	private final PriceRepository priceRepository;
	
	public MutualFundPriceService(PriceRepository priceRepository) {
		this.priceRepository = priceRepository;
	}

	@Override
	public PriceQuote quote(OffsetDateTime snapshotDateTime, Instrument instrument, InstrumentMutualFund instrumentDetail) throws ApplicationException {
		return theGlobeAndMailQuoteService(instrument);
	}

	private PriceQuote theGlobeAndMailQuoteService(Instrument instrument) throws ApplicationException {
		var ticker = instrument.getTicker();
		Document document;
		var url = THE_GLOBE_AND_MAIL_BASE_URL + ticker; 
		try {
			document = Jsoup
				    .connect(url)
				    .get();
		} catch (IOException e) {
			var message = String.format("Exception while getting price quote for mutual fund %s from url %s", ticker, url); 
			LOGGER.error(message, e);
			if (e instanceof UnknownHostException) {
				message += " (UnknownHostException)"; 
			} else {
				message += " (" + e.getMessage() + ")";
			}
			throw new ApplicationException(message);
		}

		var tradeTimeElements = document.selectXpath("//barchart-field[@name='tradeTime']");
		OffsetDateTime tradeTime = null;
		if (tradeTimeElements.size() == 1) {
			String tradeTimeString = tradeTimeElements.get(0).attr("value");
			System.out.println("tardeTimeString: "+tradeTimeString);
			if (StringUtils.isNotEmpty(tradeTimeString)) {
				try {
					var ldt = LocalDateTime.parse(tradeTimeString+"000000", DateTimeFormatter.ofPattern("MM/dd/yyHHmmss"));
					tradeTime = OffsetDateTime.of(ldt, ZoneId.of("Canada/Eastern").getRules().getOffset(ldt));
				} catch (DateTimeParseException e) {
					System.err.println(tradeTimeString+"000000" + " can not be parsed using pattern " + "MM/dd/yyHHmmss");
				}
			}
		}
		
		var lastPriceElements = document.selectXpath("//barchart-field[@name='lastPrice']");
		BigDecimal lastPrice = null;
		if (lastPriceElements.size() == 1) {
			String lastPriceString = lastPriceElements.get(0).attr("value");
			System.out.println(lastPriceString);
			try {
				lastPrice = new BigDecimal(lastPriceString);
			} catch (NumberFormatException e) {
				System.err.println(lastPriceString + " can not be parsed as a number");
			}
		}

		return new PriceQuote(lastPrice, tradeTime);
	}
	@Override
	public Collection<InstrumentTypeEnum> canHandle() {
		return List.of(InstrumentTypeEnum.MUTUAL_FUND);
	}

	@Override
	public PriceRepository priceRepository() {
		return priceRepository;
	}

}
