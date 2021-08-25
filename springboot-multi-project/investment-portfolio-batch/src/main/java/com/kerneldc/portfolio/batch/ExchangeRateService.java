package com.kerneldc.portfolio.batch;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.common.enums.CurrencyEnum;
import com.kerneldc.portfolio.domain.ExchangeRate;
import com.kerneldc.portfolio.repository.ExchangeRateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

	private final ExchangeRateRepository exchangeRateRepository;
	
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public void retrieveAndPersistExchangeRate(LocalDate date, CurrencyEnum fromCurrency, CurrencyEnum toCurrency) throws IOException, InterruptedException {

		var response = callApi(date, fromCurrency, toCurrency);
		
		var rate = parseRate(response.body());

		if (rate == null) {
			LOGGER.warn("No exchange rate found for {} to {} on {}", fromCurrency, toCurrency, date.format(dateTimeFormatter));
		} else {
			var exchangeRateList = exchangeRateRepository.findByDateAndFromCurrencyAndToCurrency(
					date, fromCurrency, toCurrency);
			ExchangeRate exchangeRate;
			if (CollectionUtils.isEmpty(exchangeRateList)) {
				exchangeRate = new ExchangeRate();
				exchangeRate.setDate(date);
				exchangeRate.setFromCurrency(CurrencyEnum.USD);
				exchangeRate.setToCurrency(CurrencyEnum.CAD);
				exchangeRate.setToCurrency(CurrencyEnum.CAD);
			} else {
				exchangeRate = exchangeRateList.get(0);
			}
			exchangeRate.setRate(rate);
			exchangeRateRepository.save(exchangeRate);
		}
	}
	
	private HttpResponse<String> callApi(LocalDate date, CurrencyEnum fromCurrency, CurrencyEnum toCurrency) throws IOException, InterruptedException {
		var client = HttpClient.newHttpClient();
		var dateString = date.format(dateTimeFormatter);
		var request = HttpRequest.newBuilder(
			       URI.create("https://www.bankofcanada.ca/valet/observations/FX"+fromCurrency+toCurrency+"/json?start_date="+dateString))
			   .header("accept", "application/json")
			   .build();
		return client.send(request, BodyHandlers.ofString());
	}
	
	private Double parseRate(String jsonResponse) throws JsonProcessingException {
		var mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(jsonResponse);
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
