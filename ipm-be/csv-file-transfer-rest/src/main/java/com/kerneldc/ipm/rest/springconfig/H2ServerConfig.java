package com.kerneldc.ipm.rest.springconfig;

import java.sql.SQLException;

import org.h2.tools.Server;

import lombok.extern.slf4j.Slf4j;

//@Configuration
@Slf4j
public class H2ServerConfig {

	//@Value("${h2.server.port:9092}")
	private String h2TcpPort;
	
	private static final String tcpPassword = String.valueOf(Math.random());

	//@Bean
	//@Profile("!test") // don't create server during tests
	public void h2Server() throws SQLException {
		var h2server = Server.createTcpServer("-tcpPort", h2TcpPort, "-tcpAllowOthers", "-tcpPassword", tcpPassword).start();
		int h2serverPort = h2server.getPort();
		LOGGER.info("H2 database server started on port {}", h2serverPort);
	}

	//@PreDestroy
	public void h2ServerShutdown() throws SQLException {
		Server.shutdownTcpServer("tcp://localhost:"+h2TcpPort, tcpPassword, true, false);
		LOGGER.info("H2 database server shutdown");
	}
}
