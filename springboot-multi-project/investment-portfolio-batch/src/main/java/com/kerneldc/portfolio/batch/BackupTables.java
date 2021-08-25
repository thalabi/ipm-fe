package com.kerneldc.portfolio.batch;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.RunScript;

public class BackupTables {

	public static void main(String[] a) throws Exception {
		var bt = new BackupTables();
		bt.run();
	}
	private void run() throws SQLException {
		var conn = DriverManager.getConnection("jdbc:h2:~/springsecuirityjwt/springsecuirityjwt;AUTO_SERVER=TRUE", "sa", "");
		var tableNames = new String[] {"portfolio", "instrument"};
		for (int i=0; i<tableNames.length; i++) {
			var tableName = tableNames[i];
			var sequenceName = tableName + "_seq";
			var byteArray = new String("script simple columns table " +  tableName).getBytes();
			ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
			var resultSet = RunScript.execute(conn, new InputStreamReader(bais));
			while (resultSet.next()) {
				var sqlLine = resultSet.getString(1); 
				if (sqlLine.startsWith("INSERT ")) {
					var sqlLine2 = sqlLine.replaceFirst("VALUES\\(\\d+\\,", "VALUES(nextval('"+sequenceName+"'),");
					System.out.println(sqlLine2);
				}
			}
			resultSet.close();
		}
		conn.close();
	}

}
