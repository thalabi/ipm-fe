package com.kerneldc.ipm.commonservices.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.server.Encoding;
import org.springframework.stereotype.Service;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.CurrencyEnum;

@Service
public class HttpUtil {

	private final String bankOfCanadaUrlTemplate;
	private final String yahooFinanceApiUrlTemplate;
	
	public HttpUtil(@Value("${bank.of.canada.url.template}")String bankOfCanadaUrlTemplate, @Value("${yahoo.finance.api.url.template}") String yahooFinanceApiUrlTemplate) {
		this.bankOfCanadaUrlTemplate = bankOfCanadaUrlTemplate;
		this.yahooFinanceApiUrlTemplate = yahooFinanceApiUrlTemplate;
	}

	public String getUrlContent(String urlString) throws ApplicationException {
		HttpURLConnection httpUrlConnection;
		try {
			var url = new URL(urlString);
			httpUrlConnection = (HttpURLConnection) url.openConnection();
			// httpUrlConnection.connect();
			var httpStatusCode = httpUrlConnection.getResponseCode();
			if (httpStatusCode != HttpURLConnection.HTTP_OK) {
				var message = String.format("Fetching contents of URL [%s], returned Http status code [%d]. ",
						url.toString(), httpStatusCode);
				var urlContent = IOUtils.toString(httpUrlConnection.getErrorStream(), Encoding.DEFAULT_CHARSET);
				if (StringUtils.isNotEmpty(urlContent)) {
					message += String.format("Site message: [%s]", urlContent);
				}
				throw new ApplicationException(message);
			}
			return IOUtils.toString(httpUrlConnection.getInputStream(), Encoding.DEFAULT_CHARSET);
		} catch (IOException e) {
			e.printStackTrace();
			var message = String.format("Fetching contents of URL [%s]failed. ", urlString);
			throw new ApplicationException(message, e);
		}
	}

	public String bankOfCanadaContent(Instant date, CurrencyEnum fromCurrency, CurrencyEnum toCurrency) throws ApplicationException {
		var dateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withZone(ZoneId.systemDefault());
		var dateString = dateTimeFormatter.format(date);
		var urlString = String.format(bankOfCanadaUrlTemplate, fromCurrency, toCurrency, dateString);		
		return getUrlContent(urlString);
	}

	public String alphavantageApiContent(String ticker) throws ApplicationException {
		// TODO copy source from StockAndEtfPriceService
		return null;
	}

	public String yahooFinanceApiContent(String ticker) throws ApplicationException {
		var urlString = String.format(yahooFinanceApiUrlTemplate, ticker);
		return getUrlContent(urlString);
	}

}
