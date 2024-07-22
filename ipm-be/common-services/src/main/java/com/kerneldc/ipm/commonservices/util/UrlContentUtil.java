package com.kerneldc.ipm.commonservices.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.web.servlet.server.Encoding;
import org.springframework.stereotype.Service;

@Service
public class UrlContentUtil {

	public String getUrlContent(URL url) throws IOException {
		HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
		//httpUrlConnection.connect();
		var httpStatusCode = httpUrlConnection.getResponseCode();
		if (httpStatusCode != HttpURLConnection.HTTP_OK) {
			var message = String.format("Fetching contents of URL [%s], returned Http status code [%d]", url.toString(), httpStatusCode);
			throw new IOException(message);
		}
		return IOUtils.toString(httpUrlConnection.getInputStream(), Encoding.DEFAULT_CHARSET);
	}

}
