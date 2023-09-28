package com.kerneldc.common.domain;

import java.util.List;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import lombok.extern.slf4j.Slf4j;

/**
 * Add class name to spring.jpa.hibernate.naming.physical-strategy in application.properties
 */
@Slf4j
public class UpperCaseNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

	private List<String> entitiesThatNeedUpperCaseTables = List.of("user", "group");
	
	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
		String entityName = name.getText();
		Identifier tableNameIdentifier = super.toPhysicalTableName(name, context);
		for (String entityThatNeedUpperCaseTable : entitiesThatNeedUpperCaseTables) {
			if (entityThatNeedUpperCaseTable.equalsIgnoreCase(entityName)) {
				tableNameIdentifier = context.getIdentifierHelper().toIdentifier(entityName.toUpperCase(), true); // a value of true quotes the tableName
			}
		}
		LOGGER.debug("Entity name [{}] will be mapped to table name [{}]", entityName, tableNameIdentifier.getText());
		return tableNameIdentifier;
	}


}
