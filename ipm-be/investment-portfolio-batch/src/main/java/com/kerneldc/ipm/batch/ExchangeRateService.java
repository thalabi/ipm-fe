package com.kerneldc.ipm.batch;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.Month;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.ExchangeRate;
import com.kerneldc.ipm.repository.ExchangeRateRepository;
import com.kerneldc.ipm.util.AppTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

	private static final String BANK_OF_CANADA_URL_TEMPLATE = "https://www.bankofcanada.ca/valet/observations/FX%s%s/json?start_date=%s"; 
	private final ExchangeRateRepository exchangeRateRepository;
	
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withZone(ZoneId.systemDefault());

	public ExchangeRate retrieveAndPersistExchangeRate(Instant date, CurrencyEnum fromCurrency, CurrencyEnum toCurrency, boolean methodHasToReturnExchangeRate) throws ApplicationException {

		var workingBusinessDay = getWorkingBusinessDay(date);
		
		LOGGER.info("Fetching exchange rate for [{}] to [{}] on business day [{}] ...", fromCurrency, toCurrency, workingBusinessDay);
		var rate = parseRate(callApi(workingBusinessDay, fromCurrency, toCurrency));

		ExchangeRate exchangeRate;
		
		if (rate == null) {
			var noRateAvailableAtApiMessage = String.format("Exchange rate not available (at external api) for %s to %s on %s", fromCurrency, toCurrency, dateTimeFormatter.format(workingBusinessDay));
			if (! /* not */ methodHasToReturnExchangeRate) {
				LOGGER.error(noRateAvailableAtApiMessage);
				throw new ApplicationException(noRateAvailableAtApiMessage);
			} else {
				LOGGER.warn(noRateAvailableAtApiMessage);
				LOGGER.warn("Will try previous rate from database ...");
				var exchangeRateList = exchangeRateRepository.findFirstByAsOfDateLessThanEqualAndFromCurrencyAndToCurrencyOrderByAsOfDateDesc(
						AppTimeUtils.toOffsetDateTime(workingBusinessDay), fromCurrency, toCurrency);
				if (CollectionUtils.isEmpty(exchangeRateList)) {
					LOGGER.error("Non found in database.");
					var message = String.format("Exchange rate not available (at external api or in database) for %s to %s on or before %s", fromCurrency, toCurrency, dateTimeFormatter.format(workingBusinessDay));
					LOGGER.error(message);
					throw new ApplicationException(message);
				} else {
					exchangeRate = exchangeRateList.get(0);
					return exchangeRate;
				}
			}
		}
		
		var exchangeRateList = exchangeRateRepository.findByAsOfDateAndFromCurrencyAndToCurrency(
				AppTimeUtils.toOffsetDateTime(workingBusinessDay), fromCurrency, toCurrency);
		if (CollectionUtils.isEmpty(exchangeRateList)) {
			exchangeRate = new ExchangeRate();
			exchangeRate.setAsOfDate(AppTimeUtils.toOffsetDateTime(workingBusinessDay));
			exchangeRate.setFromCurrency(CurrencyEnum.USD);
			exchangeRate.setToCurrency(CurrencyEnum.CAD);
		} else {
			exchangeRate = exchangeRateList.get(0);
		}
		exchangeRate.setRate(rate); // update or set the rate
		exchangeRateRepository.save(exchangeRate);
		
		return exchangeRate;
	}
	
	public void retrieveAndPersistExchangeRate(Instant date, CurrencyEnum fromCurrency, CurrencyEnum toCurrency) throws ApplicationException {
		retrieveAndPersistExchangeRate(date, fromCurrency, toCurrency, false);
	}
	/**
	 * If the provided date falls on December 25, January 1, Saturday or a Sunday, it will return the date before.
	 * Otherwise it will return the same date. 
	 * @param instant
	 * @return
	 */
	protected Instant getWorkingBusinessDay(Instant instant) {
		var workingBusinessDay = AppTimeUtils.toLocalDate(instant);
		
		while (workingBusinessDay.getMonth().equals(Month.JANUARY) && workingBusinessDay.getDayOfMonth() == 1 ||
				workingBusinessDay.getMonth().equals(Month.DECEMBER) && workingBusinessDay.getDayOfMonth() == 25 ||
				workingBusinessDay.getDayOfWeek().equals(DayOfWeek.SATURDAY) || workingBusinessDay.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
			
			workingBusinessDay = workingBusinessDay.minus(Period.ofDays(1)); 
		}
		return AppTimeUtils.toInstant(workingBusinessDay);
	}
	
	private String callApi(Instant date, CurrencyEnum fromCurrency, CurrencyEnum toCurrency) throws ApplicationException  {
		// TODO refactor to use this application UrlContentUtil
		var client = HttpClient.newHttpClient();
		//var dateString = date.format(dateTimeFormatter);
		var dateString = dateTimeFormatter.format(date);
		var url = String.format(BANK_OF_CANADA_URL_TEMPLATE, fromCurrency, toCurrency, dateString);
		var request = HttpRequest.newBuilder(
			       URI.create(url))
			   .header("accept", "application/json")
			   .build();
		HttpResponse<String> httpResponse;
		try {
			httpResponse = client.send(request, BodyHandlers.ofString());
		} catch (ConnectException e) {
			// ConnectException has a null message, so we catch it here and append 'Connect exception' to the message
			var message = String.format("Exception converting %s to %s while contacting: %s. Connect exception.", fromCurrency, toCurrency, url);
			LOGGER.error(message, e);
			throw new ApplicationException(message);
		} catch (IOException e) {
			var message = String.format("Exception converting %s to %s while contacting: %s. %s", fromCurrency, toCurrency, url, e.getMessage());
			LOGGER.error(message, e);
			throw new ApplicationException(message);
		} catch (InterruptedException e) {
			LOGGER.error("Interrupted!", e);
			Thread.currentThread().interrupt();
			throw new ApplicationException(e.getMessage());
		}
		if (httpResponse.statusCode() != HttpStatus.OK.value()) {
			var message = String.format("Exception converting %s to %s while contacting: %s. Http status code: %d", fromCurrency, toCurrency, url, httpResponse.statusCode()); 
			LOGGER.error(message);
			throw new ApplicationException(message);
		}
		return httpResponse.body();
	}
	
	private Double parseRate(String jsonResponse) throws ApplicationException {
		var mapper = new ObjectMapper();
		JsonNode root;
		try {
			root = mapper.readTree(jsonResponse);
		} catch (JsonProcessingException e) {
			var message = String.format("Exception whne parsing json response: %s", jsonResponse);
			LOGGER.error(message, e);
			throw new ApplicationException(message);
		}
		LOGGER.debug(root.toPrettyString());
		var observations = root.path("observations");
		Double rate = null;
		if (observations.isArray()) {
			for (JsonNode observation : observations) {
				rate = observation.path("FXUSDCAD").get("v").asDouble();
			}
		}
		return rate;
	}
	
}
