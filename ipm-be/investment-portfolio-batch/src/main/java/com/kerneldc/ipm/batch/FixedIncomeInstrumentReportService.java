package com.kerneldc.ipm.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kerneldc.common.exception.ApplicationException;
import com.kerneldc.ipm.domain.CurrencyEnum;
import com.kerneldc.ipm.domain.ExchangeRate;
import com.kerneldc.ipm.domain.FinancialInstitutionEnum;
import com.kerneldc.ipm.domain.HolderEnum;
import com.kerneldc.ipm.domain.InstrumentDueV;
import com.kerneldc.ipm.repository.InstrumentDueVRepository;
import com.kerneldc.ipm.util.AppFileUtils;
import com.kerneldc.ipm.util.AppTimeUtils;
import com.kerneldc.ipm.util.EmailService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixedIncomeInstrumentReportService {
	private static final String LOG_BEGIN = "Begin ...";
	private static final String LOG_END = "End ...";
	private static final byte NUMBER_OF_COLUMNS = 15;
	private static final String ARIAL_FONT = "Arial";
	private static final byte FINANCIAL_INSTITUTION_CELL = 0;
	private static final byte HOLDER_CELL = 1;
	private static final byte REGISTERED_ACCOUNT_CELL = 2;
	private static final byte INSTRUMENT_CELL = 3;
	private static final byte INSTRUMENT_ACCOUNT_NUMBER_CELL = 4;
	private static final byte PORTFOLIO_ACCOUNT_NUMBER_CELL = 5;
	private static final byte USD_BALANCE_CELL = 6;
	private static final byte BALANCE_CELL = 7;
	private static final byte TYPE_CELL = 8;
	private static final byte TERM_CELL = 9;
	private static final byte RATE_CELL = 10;
	private static final byte PROMO_RATE_CELL = 11;
	private static final byte PROMO_RATE_END_DATE_CELL = 12;
	private static final byte MATURITY_DATE_CELL = 13;
	private static final byte NOTES_CELL = 14;
	
	@Value("${instrument.due.days.to.notify:7}")
	private Long daysToNotify;

	private final EmailService emailService;
	private final InstrumentDueVRepository instrumentDueVRepository;
	private final ExchangeRateService exchangeRateService;
	
	@Data
	@AllArgsConstructor
	private class PoiContext {
		private Integer rowNumber;
		private XSSFWorkbook workbook;
		private XSSFSheet sheet;
		private XSSFDataFormat format;
		private XSSFFont arialFont;
		private XSSFFont arialFontBold;
		private XSSFFont arialFontItalic;
		private Map<HolderEnum, XSSFCellStyle> holderCellStyleMap;
	}
	
	public File generate() throws ApplicationException {
		
		LOGGER.info(LOG_BEGIN);
		var now = Instant.now();
		
		var usdToCadExchangeRate = exchangeRateService.retrieveAndPersistExchangeRate(now, CurrencyEnum.USD, CurrencyEnum.CAD, true);
		var instrumentDueVList = instrumentDueVRepository.findByOrderByPortfolioFiAscPortfolioHolderAscCurrencyAscPortfolioNameAsc();
		LOGGER.info("instrumentDueVList.size(): {}", instrumentDueVList.size());
		
		var poiContext = createPoiContext();

		printHeader(poiContext);
		
		for (var instrumentDueV : instrumentDueVList) {
			setFinancialInstitutionName(instrumentDueV);
		}
		
		printDetailLines(now, poiContext, instrumentDueVList, usdToCadExchangeRate);

		printExchangeRateFootnote(poiContext, usdToCadExchangeRate);

		autoSizeColumns(poiContext);
		
		File excelFile = null;
		try {
			var tempFilePath = AppFileUtils.createTempFile("fixed-income-report", ".xlsx");
			LOGGER.info("tempPath: {}", tempFilePath);
			excelFile = tempFilePath.toFile();
			var outputStream = new FileOutputStream(excelFile);
			poiContext.workbook.write(outputStream);
		} catch (IOException e) {
			throw new ApplicationException("Unable to open ByteArrayOutputStream", e);
		}
		
		LOGGER.info(LOG_END);				
		return excelFile;
	}

	private void autoSizeColumns(PoiContext context) {
		for (int i=0; i<=NUMBER_OF_COLUMNS; i++) {
			context.sheet.autoSizeColumn(i);
		}
	}
	private PoiContext createPoiContext() {
		var workbook = new XSSFWorkbook();
		var sheet = workbook.createSheet();
		sheet.addIgnoredErrors(CellRangeAddress.valueOf("e2:e999"), IgnoredErrorType.NUMBER_STORED_AS_TEXT);
		sheet.addIgnoredErrors(CellRangeAddress.valueOf("f2:f999"), IgnoredErrorType.NUMBER_STORED_AS_TEXT);
		var format = workbook.createDataFormat();
		var arialFont = workbook.createFont();
		arialFont.setFontName(ARIAL_FONT);
		arialFont.setFontHeightInPoints((short)9);
		var arialFontBold = workbook.createFont();
		arialFontBold.setFontName(ARIAL_FONT);
		arialFontBold.setFontHeightInPoints((short)9);
		arialFontBold.setBold(true);
		var arialFontItalic = workbook.createFont();
		arialFontItalic.setFontName(ARIAL_FONT);
		arialFontItalic.setFontHeightInPoints((short)9);
		arialFontItalic.setItalic(true);
		
		Map<HolderEnum, XSSFCellStyle> holderCellStyleMap = new EnumMap<>(HolderEnum.class);
		for (HolderEnum holderEnum : HolderEnum.values()) {
			var style = workbook.createCellStyle();
			style.setFont(arialFont);
			style.setFillForegroundColor(getHolderCellColor(holderEnum));
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			holderCellStyleMap.put(holderEnum, style);
		}
		
		return new PoiContext(0, workbook, sheet, format, arialFont, arialFontBold, arialFontItalic, holderCellStyleMap);
	}
	private void printExchangeRateFootnote(PoiContext poiContext, ExchangeRate usdToCadExchangeRate) {

		var exchangeRateStyle = poiContext.workbook.createCellStyle();
		exchangeRateStyle.setFont(poiContext.arialFontItalic);

		var row = poiContext.sheet.createRow(getRowNumber(poiContext));
		var rowNum = row.getRowNum();
		poiContext.sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 3));
		var cell = row.createCell(0);
		var cellText = String.format("*** %s to %s exchange rate as of %s: %.4f", usdToCadExchangeRate.getFromCurrency(),
				usdToCadExchangeRate.getToCurrency(), usdToCadExchangeRate.getAsOfDate().format(AppTimeUtils.DATE_FORMATTER),
				usdToCadExchangeRate.getRate());
		cell.setCellValue(cellText);
		cell.setCellStyle(exchangeRateStyle);
		
	}

	private void printTestColorsLine(PoiContext poiContext, XSSFSheet sheet, XSSFWorkbook workbook) {
		XSSFCellStyle colorStyle;
		var row = sheet.createRow(getRowNumber(poiContext));
		XSSFCell cell;
		for (int i=0; i<=64; i++) {
			if (i > 31 && i <= 40) {
				continue;
			}
			cell = row.createCell(i);
			colorStyle = workbook.createCellStyle();
			cell.setCellValue(IndexedColors.fromInt(i).name());
			
			colorStyle.setFillForegroundColor((short)i);
			colorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			
			cell.setCellStyle(colorStyle);
		}
	}

	
	private void printDetailLines(Instant now, PoiContext poiContext,	List<InstrumentDueV> instrumentDueVList, ExchangeRate usdToCadExchangeRate) {
		var balanceFormat = poiContext.format.getFormat("###,##0.00##");
		var rateFormat = poiContext.format.getFormat("##0.####");
		var dateFormat = poiContext.format.getFormat("yyyy-mm-dd");

		var arialFontStyle = poiContext.workbook.createCellStyle();
		arialFontStyle.setFont(poiContext.arialFont);

		var balanceStyle = poiContext.workbook.createCellStyle();
		balanceStyle.setFont(poiContext.arialFont);
		balanceStyle.setDataFormat(balanceFormat);
		
		var rateStyle = poiContext.workbook.createCellStyle();
		rateStyle.setFont(poiContext.arialFont);
		rateStyle.setDataFormat(rateFormat);
		
		Function<Float, XSSFCellStyle> returnRateStyle = rate -> (rate.intValue() != rate.floatValue() ? rateStyle : arialFontStyle);
		
		var dateStyle = poiContext.workbook.createCellStyle();
		dateStyle.setFont(poiContext.arialFont);
		dateStyle.setDataFormat(dateFormat);
		
		var dateStylePink = dateStyle.copy();
		
		dateStylePink.setFillForegroundColor(IndexedColors.PINK1.getIndex());
		dateStylePink.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		var boldStyle = poiContext.workbook.createCellStyle();
		boldStyle.setFont(poiContext.arialFontBold);

		var totalBalanceStyle = poiContext.workbook.createCellStyle();
		totalBalanceStyle.setFont(poiContext.arialFontBold);
		totalBalanceStyle.setDataFormat(balanceFormat);

		var oldFiName = "";
		var oldHolder = "";
		var totalBalance = 0d;
		var totalHolderBalance = 0d;
		for (InstrumentDueV instrumentDueV : instrumentDueVList) {

			if (! /* not */ instrumentDueV.getPortfolioHolder().equals(oldHolder) && StringUtils.isNotBlank(oldHolder) ||
					! /* not */ instrumentDueV.getPortfolioFiName().equals(oldFiName) && StringUtils.isNotBlank(oldFiName)) {
				printHolderTotalBalance(poiContext, totalHolderBalance, totalBalanceStyle);
				totalHolderBalance = 0;
			}
			
			var row = poiContext.sheet.createRow(getRowNumber(poiContext));
			
			// Financial Institution
			XSSFCell cell = null;
			if (! /* not */ instrumentDueV.getPortfolioFiName().equals(oldFiName)) {
				cell = row.createCell(FINANCIAL_INSTITUTION_CELL);
				cell.setCellStyle(arialFontStyle);
				cell.setCellValue(instrumentDueV.getPortfolioFiName());
			}
	
			// Holder
			cell = row.createCell(HOLDER_CELL);
			if (! /* not */ instrumentDueV.getPortfolioHolder().equals(oldHolder) ||
					! /* not */ instrumentDueV.getPortfolioFiName().equals(oldFiName)) {
				cell.setCellValue(instrumentDueV.getPortfolioHolder());
			} else {
				cell.setCellValue(StringUtils.EMPTY);
			}
			var holderEnum = HolderEnum.valueOf(instrumentDueV.getPortfolioHolder());
			var holderCellStyleMap = poiContext.getHolderCellStyleMap();
			cell.setCellStyle(holderCellStyleMap.get(holderEnum));
			
			// RegisteredAccount 
			cell = row.createCell(REGISTERED_ACCOUNT_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getRegisteredAccount());

			// Instrument 
			cell = row.createCell(INSTRUMENT_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getInstrumentName());

			// Instrument Account Number 
			cell = row.createCell(INSTRUMENT_ACCOUNT_NUMBER_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getInstrumentAccountNumber());

			// Portfolio Account Number
			cell = row.createCell(PORTFOLIO_ACCOUNT_NUMBER_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getPortfolioAccountNumber());
			
			var balance = instrumentDueV.getPrice().multiply(instrumentDueV.getQuantity()).doubleValue();
			
			// USD Balance
			if (instrumentDueV.getCurrency().equals(CurrencyEnum.USD.name())) {
				cell = row.createCell(USD_BALANCE_CELL);
				cell.setCellValue(balance);
				cell.setCellType(CellType.NUMERIC);
				cell.setCellStyle(balanceStyle);
			}
			
			// Balance
			if (instrumentDueV.getCurrency().equals(CurrencyEnum.USD.name())) {
				cell = row.createCell(BALANCE_CELL);
				balance = round(balance * usdToCadExchangeRate.getRate());
				cell.setCellValue(balance);
				cell.setCellType(CellType.NUMERIC);
				cell.setCellStyle(balanceStyle);
			} else {
				cell = row.createCell(BALANCE_CELL);
				cell.setCellValue(balance);
				cell.setCellType(CellType.NUMERIC);
				cell.setCellStyle(balanceStyle);
			}
			totalBalance += balance;
			totalHolderBalance += balance;
			
			// Type
			cell = row.createCell(TYPE_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getType());
			
			// Term
			cell = row.createCell(TERM_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getTerm());
			
			// Rate
			if (instrumentDueV.getInterestRate() != null) {
				cell = row.createCell(RATE_CELL);
				cell.setCellStyle(returnRateStyle.apply(instrumentDueV.getInterestRate()));
				cell.setCellValue(instrumentDueV.getInterestRate());
			}
			
			// Promo Rate
			if (instrumentDueV.getPromotionalInterestRate() != null) {
				cell = row.createCell(PROMO_RATE_CELL);
				cell.setCellStyle(returnRateStyle.apply(instrumentDueV.getPromotionalInterestRate()));
				cell.setCellValue(instrumentDueV.getPromotionalInterestRate());
			}
			
			// Promo End Date
			if (instrumentDueV.getPromotionEndDate() != null) {
				cell = row.createCell(PROMO_RATE_END_DATE_CELL);
				cell.setCellValue(instrumentDueV.getPromotionEndDate().toLocalDate());
				
				if (isWithinNextDays(now, instrumentDueV.getPromotionEndDate(), daysToNotify)) {
					cell.setCellStyle(dateStylePink);
				} else {
					cell.setCellStyle(dateStyle);
				}
				
			}
			
			// Maturity Date
			if (instrumentDueV.getMaturityDate() != null) {
				cell = row.createCell(MATURITY_DATE_CELL);
				cell.setCellValue(instrumentDueV.getMaturityDate().toLocalDate());

				if (isWithinNextDays(now, instrumentDueV.getMaturityDate(), daysToNotify)) {
					cell.setCellStyle(dateStylePink);
				} else {
					cell.setCellStyle(dateStyle);
				}
			
			}

			// Notes 
			cell = row.createCell(NOTES_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getNotes());

			oldFiName = instrumentDueV.getPortfolioFiName();
			oldHolder = instrumentDueV.getPortfolioHolder();
			
		}

		printHolderTotalBalance(poiContext, totalHolderBalance, totalBalanceStyle);

		printGrandTotalBalance(poiContext, totalBalance, totalBalanceStyle);
	}

	private short getHolderCellColor(HolderEnum holderEnum) {
		return IndexedColors.valueOf(holderEnum.getReportCellColor()).getIndex();
	}
	
	private double round(double balance) {
		return BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	
	private void printGrandTotalBalance(PoiContext poiContext, double totalBalance, XSSFCellStyle totalBalanceStyle) {
		printTotalBalance(poiContext, totalBalance, totalBalanceStyle, false);
	}
	private void printHolderTotalBalance(PoiContext poiContext, double totalBalance, XSSFCellStyle totalBalanceStyle) {
		printTotalBalance(poiContext, totalBalance,  totalBalanceStyle, true);
	}
	private void printTotalBalance(PoiContext poiContext, double totalBalance, XSSFCellStyle totalBalanceStyle, boolean holderBalance) {
		var row = poiContext.sheet.createRow(getRowNumber(poiContext));
		
		var cell = row.createCell(holderBalance ? 1 : 0);
		cell.setCellValue("Total");
		
		if (holderBalance) {
			var previousRow = poiContext.sheet.getRow(row.getRowNum()-1);
			var cellAbove = previousRow.getCell(1);
			var cellStyle = cellAbove.getCellStyle().copy();
			cellStyle.setFont(poiContext.arialFontBold);
			cell.setCellStyle(cellStyle);
		} else {
			var cellStyle = poiContext.workbook.createCellStyle();
			cellStyle.setFont(poiContext.arialFontBold);
			cell.setCellStyle(cellStyle);
		}
		
		cell = row.createCell(BALANCE_CELL);
		cell.setCellValue(totalBalance);
		cell.setCellType(CellType.NUMERIC);
		cell.setCellStyle(totalBalanceStyle);
	}

	private boolean isWithinNextDays(Instant now, OffsetDateTime dateToCheck, Long days) {
		Long numberOfDays = AppTimeUtils.daysBetween(AppTimeUtils.toOffsetDateTime(now), dateToCheck);
		LOGGER.debug("daysBetween({}, {}) = {}", now, dateToCheck, numberOfDays);
		return numberOfDays.compareTo(days) <= 0;
	}
	
	private void printHeader(PoiContext poiContext) {

		var boldFontGreenBackground = poiContext.workbook.createCellStyle();
		boldFontGreenBackground.setFont(poiContext.arialFontBold);
		// setFillForegroundColor is not a typo
		boldFontGreenBackground.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		boldFontGreenBackground.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    
		var row = poiContext.sheet.createRow(getRowNumber(poiContext));

		var cell = row.createCell(FINANCIAL_INSTITUTION_CELL);
		cell.setCellValue("Financial Institution");
		
		cell = row.createCell(HOLDER_CELL);
		cell.setCellValue("Holder");
		
		cell = row.createCell(REGISTERED_ACCOUNT_CELL);
		cell.setCellValue("Reg");
		
		cell = row.createCell(INSTRUMENT_CELL);
		cell.setCellValue("Instrument");
		
		cell = row.createCell(INSTRUMENT_ACCOUNT_NUMBER_CELL);
		cell.setCellValue("Instrument Acc No");
		
		cell = row.createCell(PORTFOLIO_ACCOUNT_NUMBER_CELL);
		cell.setCellValue("Portfolio Acc No");
		
		cell = row.createCell(USD_BALANCE_CELL);
		cell.setCellValue("USD Balance");
		
		cell = row.createCell(BALANCE_CELL);
		cell.setCellValue("Balance");
		
		cell = row.createCell(TYPE_CELL);
		cell.setCellValue("Type");
		
		cell = row.createCell(TERM_CELL);
		cell.setCellValue("Term");
		
		cell = row.createCell(RATE_CELL);
		cell.setCellValue("Rate");
		
		cell = row.createCell(PROMO_RATE_CELL);
		cell.setCellValue("Promo Rate");
		
		cell = row.createCell(PROMO_RATE_END_DATE_CELL);
		cell.setCellValue("Promo End Date");
		
		cell = row.createCell(MATURITY_DATE_CELL);
		cell.setCellValue("Maturity Date");		

		cell = row.createCell(NOTES_CELL);
		cell.setCellValue("Notes");		

		setRowStyle(row, boldFontGreenBackground);
		
		poiContext.sheet.createFreezePane(0, 1); // freeze header row
	}

	private void setRowStyle(XSSFRow row, XSSFCellStyle style) {
		// getLastCellNum() gets the last cell number plus 1
		for (int i=0; i<row.getLastCellNum(); i++) {
			row.getCell(i).setCellStyle(style);
		}
	}

	private Integer getRowNumber(PoiContext poiContext) {
		var rn = poiContext.rowNumber;
		poiContext.rowNumber = rn + 1;
		return rn;
	}
	
	public File generateAndEmail() throws ApplicationException {
		LOGGER.info(LOG_BEGIN);
		var excelFile = generate();
		emailService.sendFixedIncomeInstrumentReport(excelFile);
		LOGGER.info(LOG_END);
		return excelFile;
	}

	private void setFinancialInstitutionName(InstrumentDueV instrumentDueV) {
		if (instrumentDueV.getPortfolioFi() != null) {
			instrumentDueV.setPortfolioFiName(FinancialInstitutionEnum.financialInstitutionEnumOf(instrumentDueV.getPortfolioFi()).name());
		}
		if (instrumentDueV.getIssuerFi() != null) {
			instrumentDueV.setIssuerFiName(FinancialInstitutionEnum.financialInstitutionEnumOf(instrumentDueV.getIssuerFi()).name());
		}
	}

}
