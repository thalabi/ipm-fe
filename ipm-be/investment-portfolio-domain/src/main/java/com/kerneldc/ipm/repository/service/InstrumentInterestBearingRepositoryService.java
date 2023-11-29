package com.kerneldc.ipm.repository.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.kerneldc.ipm.domain.InterestBearingTypeEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InstrumentInterestBearingRepositoryService extends AbstractRepositoryService<InstrumentInterestBearing, Long>{
	
	public InstrumentInterestBearingRepositoryService(JpaRepository<InstrumentInterestBearing, Long> instrumentInterestBearingRepository) {
		super(instrumentInterestBearingRepository);
	}

	@Override
	protected void handleEntity(InstrumentInterestBearing iib) {
		LOGGER.info("iib: {}", iib);
		var i = iib.getInstrument();
		i.setTicker(determineTickerToSet(iib.getType(), i.getName(), i.getTicker()));
		iib.setPrice(setDefaultPrice(iib.getPrice()));
	}
	
    private String determineTickerToSet(InterestBearingTypeEnum type, String name, String ticker) {
		if (Arrays.asList(InterestBearingTypeEnum.MONEY_MARKET, InterestBearingTypeEnum.INVESTMENT_SAVINGS)
				.contains(type)) {
			return ticker;
    	} else {
    		// For InterestBearingTypeEnum CHEQUING,SAVINGS, GIC and TERM_DEPOSIT use the instrument name to generate a hash as the ticker
    		return md5(name);
    	}
    }

    private static String md5(String name) {
    	
    	var hashBytes = DigestUtils.md5Digest(name.getBytes());
    	LOGGER.info("md5 of {} is: {}", name, hashBytes);
    	// Remove null bytes from hash because postgres does not allow nulls/zeros in varchar
    	var noZeroHashBytesArray = new byte[hashBytes.length];
    	var i = 0;
    	for (byte hashByte: hashBytes) {
    		if (hashByte == 0) {
    			continue;
    		}
    		noZeroHashBytesArray[i++] = hashByte;
    	}
    	var trimmedByteArray = new byte[i];
    	System.arraycopy(noZeroHashBytesArray, 0, trimmedByteArray, 0, i);
    	return new String(trimmedByteArray, StandardCharsets.UTF_8);
    }

    private BigDecimal setDefaultPrice(BigDecimal price) {
    	return price == null || price.equals(BigDecimal.ZERO) ? BigDecimal.ONE : price;
    }

}
