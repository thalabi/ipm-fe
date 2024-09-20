package com.kerneldc.common.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class LogicalKeyHolder implements Serializable, ILogicallyKeyed {

	private static final long serialVersionUID = 1L;
	private static final String KEY_SEPERATOR = "|";

	@Column(name = COLUMN_LK)
	private String logicalKey;
	
	public static LogicalKeyHolder build(Object... keyParts) {
		var lk = new LogicalKeyHolder();
		String[] stringKeyParts = new String[keyParts.length];
		var i = 0;
		for (Object keyPart: keyParts) {
			if (keyPart == null) {
				stringKeyParts[i++] = StringUtils.EMPTY;
				continue;
			}
			if (keyPart.getClass().isEnum()) {
				stringKeyParts[i++] = ((Enum<?>)keyPart).name();
				continue;
			}
			switch (keyPart.getClass().getSimpleName()) {
				case "Boolean" ->
					stringKeyParts[i++] = ((Boolean)keyPart).toString();
				case "String" ->
					stringKeyParts[i++] = (String)keyPart;
				case "Integer", "Long", "Float", "Double", "BigDecimal", "Short" ->
					stringKeyParts[i++] = String.valueOf(keyPart);	
				case "LocalDateTime" ->
					stringKeyParts[i++] = ((LocalDateTime)keyPart).format(AbstractEntity.LOCAL_DATE_TIME_FORMATTER);
				case "OffsetDateTime" ->
					stringKeyParts[i++] = ((OffsetDateTime)keyPart).format(AbstractEntity.OFFSET_DATE_TIME_UTC_FORMATTER); // to UTC
				default ->
					throw new IllegalArgumentException(String.format("Unsupported data type in logical key: %s", keyPart.getClass().getSimpleName()));
			}
		}
		lk.setLogicalKey(concatLogicalKeyParts(stringKeyParts));
		return lk;
	}

	private static String concatLogicalKeyParts(String... keyParts) {
    	return String.join(KEY_SEPERATOR, keyParts);
    }	
}
