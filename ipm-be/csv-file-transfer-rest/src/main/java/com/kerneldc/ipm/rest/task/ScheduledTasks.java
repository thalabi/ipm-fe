package com.kerneldc.ipm.rest.task;

import java.io.File;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.batch.HoldingPricingService;
import com.kerneldc.ipm.batch.HoldingsReportService;
import com.kerneldc.ipm.batch.InstrumentDueNotificationService;
import com.kerneldc.ipm.repository.FixedIncomeAuditRepository;
import com.kerneldc.ipm.rest.csv.service.GenericFileTransferService;
import com.kerneldc.ipm.util.EmailService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScheduledTasks {
	
	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
	private static final String CLEANUP_TEMP_FILES_METHOD_NAME = "cleanupTempFiles";
	@Autowired
	private Environment environment;
	
	@Autowired
	private HoldingPricingService holdingPricingService;
	@Autowired
	private HoldingsReportService holdingsReportService;
	@Autowired
	private InstrumentDueNotificationService instrumentDueNotificationService;
	@Autowired
	private FixedIncomeAuditRepository fixedIncomeAuditRepository;

	
	@Autowired
	private EmailService emailService;

	
	private long fixedDelayThroughReflection;
	
	@PostConstruct
	private void getFixedDelayOnCleanupTempDir() throws NoSuchMethodException {
		Method method = ScheduledTasks.class.getDeclaredMethod(CLEANUP_TEMP_FILES_METHOD_NAME);
		Scheduled annotation = method.getAnnotation(Scheduled.class);
        String fixedDelayString = annotation.fixedDelayString(); // fixedDelayString is a property
        Pattern p = Pattern.compile("\\$\\{(.+)\\}");
        Matcher m = p.matcher(fixedDelayString);
        String fixedDelayStringProperty = StringUtils.EMPTY;
        if (m.matches()) {
        	fixedDelayStringProperty = m.group(1);
        	fixedDelayThroughReflection = Long.valueOf(environment.getProperty(fixedDelayStringProperty));
        }
        if (fixedDelayThroughReflection == 0) {
        	String exceptionMessage = String.format("Could not determine value of @Scheduled fixedDelay argument on method: %s", CLEANUP_TEMP_FILES_METHOD_NAME);
        	LOGGER.error(exceptionMessage);
        	LOGGER.error("method: {}, fixedDelayString: {}, fixedDelayStringProperty: {}, fixedDelayThroughReflection: {}", method, fixedDelayString, fixedDelayStringProperty, fixedDelayThroughReflection);
        	throw new IllegalStateException(exceptionMessage);
        }
	}

	@Scheduled(initialDelay = 0, fixedDelayString = "${application.task.tempFilesCleanup.intervalInMilliseconds}")
	public void cleanupTempFiles() {
		deleteExceptionFiles();
		deleteTransformerWorkFiles();
		deleteXlsxFiles();

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime nextCleanup = now.plus(fixedDelayThroughReflection, ChronoUnit.MILLIS);
		LOGGER.info("Next temp directory file cleanup at: {}", nextCleanup.format(TIMESTAMP_FORMAT));
	}
	private void deleteExceptionFiles() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		IOFileFilter wildcardFileFilter = new WildcardFileFilter(GenericFileTransferService.EXCEPTIONS_FILE_PREFIX+"*"+GenericFileTransferService.EXCEPTIONS_FILE_SUFFIX);
		IOFileFilter ageFileFilter = new AgeFileFilter(System.currentTimeMillis() - fixedDelayThroughReflection-1); // files created before last cleanup
		Collection<File> deleteFileList = FileUtils.listFiles(tempDir, new AndFileFilter(wildcardFileFilter, ageFileFilter), null);
		LOGGER.info("Scanning for exception files to delete ...");
		deleteFileList.stream().forEach(fileToDelete -> {    		
			LOGGER.info("Deleteing exception file: {}", fileToDelete);
			FileUtils.deleteQuietly(fileToDelete);
		});
	}
	private void deleteTransformerWorkFiles() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		IOFileFilter wildcardFileFilter = new WildcardFileFilter(GenericFileTransferService.TEMP_FILES_PREFIX+"*.tmp");
		IOFileFilter ageFileFilter = new AgeFileFilter(System.currentTimeMillis() - fixedDelayThroughReflection-1); // files created before last cleanup
		Collection<File> deleteFileList = FileUtils.listFiles(tempDir, new AndFileFilter(wildcardFileFilter, ageFileFilter), null);
		LOGGER.info("Scanning for transformer work files to delete ...");
		deleteFileList.stream().forEach(fileToDelete -> {    		
			LOGGER.info("Deleteing transformer work file: {}", fileToDelete);
			FileUtils.deleteQuietly(fileToDelete);
		});
	}
	private void deleteXlsxFiles() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		IOFileFilter wildcardFileFilter = new WildcardFileFilter(GenericFileTransferService.TEMP_FILES_PREFIX+"*.xlsx");
		IOFileFilter ageFileFilter = new AgeFileFilter(System.currentTimeMillis() - fixedDelayThroughReflection-1); // files created before last cleanup
		Collection<File> deleteFileList = FileUtils.listFiles(tempDir, new AndFileFilter(wildcardFileFilter, ageFileFilter), null);
		LOGGER.info("Scanning for xlsx files to delete ...");
		deleteFileList.stream().forEach(fileToDelete -> {    		
			LOGGER.info("Deleteing xlsx file: {}", fileToDelete);
			FileUtils.deleteQuietly(fileToDelete);
		});
	}
/*
	@Scheduled(initialDelay = 0, fixedDelayString = "${application.task.purgePositionSnapshots.intervalInMilliseconds}")
	@Transactional
	public void purgePositionSnapshots() {
		var maxPositionSnapshotList = jdbcTemplate.queryForList(
                "with position_date as ("
                + "    select distinct position_snapshot, cast(position_snapshot as date) position_date from position"
                + ")"
                + "select max(position_snapshot) max_position_snapshot, position_date from position_date group by position_date");
		LOGGER.debug("maxPositionSnapshotList: {}", maxPositionSnapshotList);
		for (Map<String, Object> maxPositionSnapshot : maxPositionSnapshotList) {
			LOGGER.info("Purging earliest position snapshots for date: {}", maxPositionSnapshot.get("POSITION_DATE"));
			var maxPositionSnapshotTimestamp = (Timestamp)maxPositionSnapshot.get("MAX_POSITION_SNAPSHOT");
			var deleteCount = positionRepository.deleteByPositionSnapshotNot(maxPositionSnapshotTimestamp.toLocalDateTime());
			LOGGER.info("Positions deleted: {}", deleteCount);
		}
	}
*/
	
	@Scheduled(cron = "0 0 19 * * MON-FRI")
	public void getHoldingPrices() throws ApplicationException {
		try {
			holdingPricingService.priceHoldings(true, true);
		} catch (ApplicationException applicationException) {
			applicationException.printStackTrace();
			emailService.sendDailyMarketValueFailure(applicationException);
		}
	}
	@Scheduled(cron = "1 0 0 * * *")
	public void checkInstrumentDue() throws ApplicationException {
		try {
			instrumentDueNotificationService.checkDueDate();
		} catch (ApplicationException applicationException) {
			applicationException.printStackTrace();
			emailService.sendInstrumentDueNotificationFailure(applicationException);
		}
	}
	@Scheduled(cron = "1 0 0 * * *")
	public void holdingsReport() throws ApplicationException {
		var fixedIncomeAudit = fixedIncomeAuditRepository.findById(1l)
				.orElseThrow(() -> new IllegalStateException("Entity fixedIncomeAudit does not have a row with id 1."));
		if (Boolean.TRUE.equals(fixedIncomeAudit.getChange())) {
			try {
				holdingsReportService.generateAndEmail();
			} catch (ApplicationException applicationException) {
				applicationException.printStackTrace();
				emailService.sendFixedIncomeInstrumentReportFailure(applicationException);
			}
			
			fixedIncomeAudit.setChange(false);
			fixedIncomeAuditRepository.save(fixedIncomeAudit);
		}
	}
	
	@Autowired
	private IncrementingIntegerComponent incrementingIntegerComponent;
	
	@Scheduled(initialDelay = 3000, fixedDelayString = "100000")
	public void testTaskInSeparateThread() throws InterruptedException {
		incrementingIntegerComponent.taskIncrementingSomeInteger();
	}

	@Scheduled(initialDelay = 5000, fixedDelayString = "100000")
	public void testTaskInSeparateThread2() throws InterruptedException {
		incrementingIntegerComponent.taskIncrementingSomeInteger();
	}

//	private static final ThreadLocal<Integer> context = new ThreadLocal<>();
//	private void taskIncrementingSomeInteger() throws InterruptedException {
//		LOGGER.info("Begin ...");
//		LOGGER.info("Current thread name: {}, thread ID: {}", Thread.currentThread().getName(), Thread.currentThread().getId());
//		Thread.sleep(1000);
//		if (context.get() == null) {
//			context.set(1);
//		} else {
//			context.set(context.get()+1);
//		}
//		LOGGER.info("context: {}", context.get());
//		Thread.sleep(5000);
//		context.remove();
//		LOGGER.info("End ...");
//	}
}
