package com.kerneldc.ipm.repository.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.kerneldc.ipm.domain.InterestBearingTypeEnum;
import com.kerneldc.ipm.domain.instrumentdetail.InstrumentInterestBearing;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InstrumentInterestBearingService extends AbstractRepositoryService<InstrumentInterestBearing, Long>{
	
	public InstrumentInterestBearingService(JpaRepository<InstrumentInterestBearing, Long> holdingRepository) {
		super(holdingRepository);
	}

	@Override
	protected void handleEntity(InstrumentInterestBearing iib) {
		LOGGER.info("iib: {}", iib);
		var i = iib.getInstrument();
		i.setTicker(getTicker(iib.getType(), i.getName(), i.getTicker()));
		iib.setPrice(setDefaultPrice(iib.getPrice()));
	}
	
    private String getTicker(InterestBearingTypeEnum type, String name, String ticker) {
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
    	// Remove null bytes from hash because postgres does not allow nulls in varchar
    	var noNullHashBytesList = new ArrayList<Byte>();
    	for (byte hashByte: hashBytes) {
    		if (hashByte == 0) continue;
    		noNullHashBytesList.add(hashByte);
    	}
    	var noNullHashBytesArray = noNullHashBytesList.toArray(new Byte[noNullHashBytesList.size()]);
    	return new String(ArrayUtils.toPrimitive(noNullHashBytesArray), StandardCharsets.UTF_8);
    }

    private BigDecimal setDefaultPrice(BigDecimal price) {
    	return price == null || price.equals(BigDecimal.ZERO) ? BigDecimal.ONE : price;
    }

}
