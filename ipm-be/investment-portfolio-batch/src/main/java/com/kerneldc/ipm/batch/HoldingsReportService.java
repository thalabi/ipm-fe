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
import com.kerneldc.ipm.repository.IEquityReport;
import com.kerneldc.ipm.repository.InstrumentDueVRepository;
import com.kerneldc.ipm.repository.PositionRepository;
import com.kerneldc.ipm.util.AppFileUtils;
import com.kerneldc.ipm.util.AppTimeUtils;
import com.kerneldc.ipm.util.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingsReportService {
	private static final String LOG_BEGIN = "Begin ...";
	private static final String LOG_END = "End ...";
	private static final byte FI_NUMBER_OF_COLUMNS = 15;
	private static final String ARIAL_FONT = "Arial";
	private static final byte FI_FINANCIAL_INSTITUTION_CELL = 0;
	private static final byte FI_HOLDER_CELL = 1;
	private static final byte FI_REGISTERED_ACCOUNT_CELL = 2;
	private static final byte FI_INSTRUMENT_CELL = 3;
	private static final byte FI_INSTRUMENT_ACCOUNT_NUMBER_CELL = 4;
	private static final byte FI_PORTFOLIO_ACCOUNT_NUMBER_CELL = 5;
	private static final byte FI_USD_BALANCE_CELL = 6;
	private static final byte FI_BALANCE_CELL = 7;
	private static final byte FI_TYPE_CELL = 8;
	private static final byte FI_TERM_CELL = 9;
	private static final byte FI_RATE_CELL = 10;
	private static final byte FI_PROMO_RATE_CELL = 11;
	private static final byte FI_PROMO_RATE_END_DATE_CELL = 12;
	private static final byte FI_MATURITY_DATE_CELL = 13;
	private static final byte FI_NOTES_CELL = 14;
	
	private static final byte EQ_NUMBER_OF_COLUMNS = 14;
	private static final byte EQ_FINANCIAL_INSTITUTION_CELL = 0;
	private static final byte EQ_HOLDER_CELL = 1;
	private static final byte EQ_PORTFOLIO_ID_CELL = 2;
	private static final byte EQ_PORTFOLIO_NAME_CELL = 3;
	private static final byte EQ_CURRENCY_CELL = 4;
	private static final byte EQ_INSTRUMENT_NAME_CELL = 5;
	private static final byte EQ_TICKER_CELL = 6;
	private static final byte EQ_INSTRUMENT_TYPE_CELL = 7;
	private static final byte EQ_QUANTITY_CELL = 8;
	private static final byte EQ_PRICE_CELL = 9;
	private static final byte EQ_PRICE_TIMESTAMP_CELL = 10;
	private static final byte EQ_PRICE_TIMESTAMP_FROM_SOURCE_CELL = 11;
	private static final byte EQ_MARKET_VALUE_CELL = 12;
	private static final byte EQ_CAD_MARKET_VALUE_CELL = 13;

	@Value("${instrument.due.days.to.notify:7}")
	private Long daysToNotify;

	private final EmailService emailService;
	private final InstrumentDueVRepository instrumentDueVRepository;
	private final ExchangeRateService exchangeRateService;
	private final PositionRepository positionRepository;
	
	private enum SheetNameEnum {FIXED_INCOME, EQUITY, SUMMARY}

	private record PoiContext (
		Map<SheetNameEnum, Integer> rowNumberMap,
		XSSFWorkbook workbook,
		Map<SheetNameEnum, XSSFSheet> sheetMap,
		XSSFFont arialFontBold,
		XSSFFont arialFontItalic,
		XSSFCellStyle boldFontGreenBackground,
		XSSFCellStyle arialFontStyle,
		XSSFCellStyle amountStyle,
		XSSFCellStyle rateStyle,
		XSSFCellStyle dateStyle,
		XSSFCellStyle dateStylePink,
		XSSFCellStyle boldStyle,
		XSSFCellStyle totalAmountStyle,
		Map<HolderEnum, XSSFCellStyle> holderCellStyleMap) {}
	
	private record Total (double total, double cadTotal) {}
	
	public File generate() throws ApplicationException {
		
		LOGGER.info(LOG_BEGIN);
		var now = Instant.now();
		
		var usdToCadExchangeRate = exchangeRateService.fetchAndPersistExchangeRate(now, CurrencyEnum.USD, CurrencyEnum.CAD, true);
		var fixedIncomeList = instrumentDueVRepository.findByOrderByPortfolioFiAscPortfolioHolderAscCurrencyAscPortfolioNameAsc();
		var equityList = positionRepository.equityReport();
		LOGGER.info("fixedIncomeList.size(): {}", fixedIncomeList.size());
		LOGGER.info("equityList.size(): {}", equityList.size());
		
		var poiContext = createPoiContext();

		printFixedIncomeHeader(poiContext);
		
		for (var fixedIncome : fixedIncomeList) {
			setFinancialInstitutionName(fixedIncome);
		}
		
		var totalFixedAssetsCadBalance = printFixedIncomeDetailLines(now, poiContext, fixedIncomeList, usdToCadExchangeRate);

		printExchangeRateFootnote(poiContext, SheetNameEnum.FIXED_INCOME, usdToCadExchangeRate);

		
		
		printEquityHeader(poiContext);
		var totalEquityCadBalance = printEquityDetailLines(poiContext, equityList, usdToCadExchangeRate);
		printExchangeRateFootnote(poiContext, SheetNameEnum.EQUITY, usdToCadExchangeRate);
		
		printSummarySheet(poiContext, totalFixedAssetsCadBalance, totalEquityCadBalance);
		
		autoSizeColumns(poiContext);
		
		File excelFile = null;
		try {
			var tempFilePath = AppFileUtils.createTempFile("holdings-report", ".xlsx");
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

	private void printSummarySheet(PoiContext poiContext, double totalFixedAssetsCadBalance,
			double totalEquityCadBalance) {
		var sheetNameEnum = SheetNameEnum.SUMMARY;
		var row = getNextRow(poiContext, sheetNameEnum);
		
		XSSFCell cell = null;

		var arialFontStyle = poiContext.arialFontStyle;
		var amountStyle = poiContext.amountStyle;
		var totalAmountStyle = poiContext.totalAmountStyle;
		var boldStyle = poiContext.boldStyle;

		cell = row.createCell(0);
		cell.setCellValue("Asset");
		cell = row.createCell(1);
		cell.setCellValue("Amount");

		setRowStyle(row, poiContext.boldFontGreenBackground);

		row = getNextRow(poiContext, sheetNameEnum);

		cell = row.createCell(0);
		cell.setCellStyle(arialFontStyle);
		cell.setCellValue("Fixed Income");

		cell = row.createCell(1);
		cell.setCellStyle(amountStyle);
		cell.setCellValue(totalFixedAssetsCadBalance);
		

		row = getNextRow(poiContext, sheetNameEnum);
		cell = row.createCell(0);
		cell.setCellStyle(arialFontStyle);
		cell.setCellValue("Equity");

		cell = row.createCell(1);
		cell.setCellStyle(amountStyle);
		cell.setCellValue(totalEquityCadBalance);
		

		row = getNextRow(poiContext, sheetNameEnum);
		cell = row.createCell(0);
		cell.setCellStyle(boldStyle);
		cell.setCellValue("Total");

		cell = row.createCell(1);
		cell.setCellStyle(totalAmountStyle);
		cell.setCellValue(totalFixedAssetsCadBalance+totalEquityCadBalance);
	}

	private void autoSizeColumns(PoiContext poiContext) {
		for (int i=0; i<=FI_NUMBER_OF_COLUMNS; i++) {
			poiContext.sheetMap.get(SheetNameEnum.FIXED_INCOME).autoSizeColumn(i);
		}
		for (int i=0; i<=EQ_NUMBER_OF_COLUMNS; i++) {
			poiContext.sheetMap.get(SheetNameEnum.EQUITY).autoSizeColumn(i);
		}
		for (int i=0; i<=2; i++) {
			poiContext.sheetMap.get(SheetNameEnum.SUMMARY).autoSizeColumn(i);
		}
	}
	private PoiContext createPoiContext() {
		var workbook = new XSSFWorkbook();
		var fixedIncomeSheet = workbook.createSheet("Fixed Income");
		var equitySheet = workbook.createSheet("Equity");
		var summarySheet = workbook.createSheet("Summary");
		fixedIncomeSheet.addIgnoredErrors(CellRangeAddress.valueOf("e2:e999"), IgnoredErrorType.NUMBER_STORED_AS_TEXT);
		fixedIncomeSheet.addIgnoredErrors(CellRangeAddress.valueOf("f2:f999"), IgnoredErrorType.NUMBER_STORED_AS_TEXT);
		var format = workbook.createDataFormat();
		var amountFormat = format.getFormat("###,##0.00##");
		var rateFormat = format.getFormat("##0.####");
		var dateFormat = format.getFormat("yyyy-mm-dd");

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
		
		var boldFontGreenBackground = workbook.createCellStyle();
		boldFontGreenBackground.setFont(arialFontBold);
		// setFillForegroundColor is not a typo
		boldFontGreenBackground.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		boldFontGreenBackground.setFillPattern(FillPatternType.SOLID_FOREGROUND);


		var arialFontStyle = workbook.createCellStyle();
		arialFontStyle.setFont(arialFont);

		var amountStyle = workbook.createCellStyle();
		amountStyle.setFont(arialFont);
		amountStyle.setDataFormat(amountFormat);
		
		var rateStyle = workbook.createCellStyle();
		rateStyle.setFont(arialFont);
		rateStyle.setDataFormat(rateFormat);
		
		var dateStyle = workbook.createCellStyle();
		dateStyle.setFont(arialFont);
		dateStyle.setDataFormat(dateFormat);

		var dateStylePink = dateStyle.copy();
		
		dateStylePink.setFillForegroundColor(IndexedColors.PINK1.getIndex());
		dateStylePink.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		var boldStyle = workbook.createCellStyle();
		boldStyle.setFont(arialFontBold);

		var totalAmountStyle = workbook.createCellStyle();
		totalAmountStyle.setFont(arialFontBold);
		totalAmountStyle.setDataFormat(amountFormat);

		
		Map<HolderEnum, XSSFCellStyle> holderCellStyleMap = new EnumMap<>(HolderEnum.class);
		for (HolderEnum holderEnum : HolderEnum.values()) {
			var style = workbook.createCellStyle();
			style.setFont(arialFont);
			style.setFillForegroundColor(getHolderCellColor(holderEnum));
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			holderCellStyleMap.put(holderEnum, style);
		}
		
		var sheetMap = Map.of(SheetNameEnum.FIXED_INCOME, fixedIncomeSheet, SheetNameEnum.EQUITY, equitySheet, SheetNameEnum.SUMMARY, summarySheet);
		EnumMap<SheetNameEnum, Integer> rowNumberMap = new EnumMap<>(SheetNameEnum.class);
		rowNumberMap.put(SheetNameEnum.FIXED_INCOME, 0);
		rowNumberMap.put(SheetNameEnum.EQUITY, 0);
		rowNumberMap.put(SheetNameEnum.SUMMARY, 0);
		return new PoiContext(rowNumberMap, workbook, sheetMap, arialFontBold, arialFontItalic,
				boldFontGreenBackground,
				arialFontStyle, amountStyle, rateStyle, dateStyle,
				dateStylePink, boldStyle, totalAmountStyle,
				holderCellStyleMap);
	}
	
	private void printExchangeRateFootnote(PoiContext poiContext, SheetNameEnum sheetNameEnum, ExchangeRate usdToCadExchangeRate) {

		var exchangeRateStyle = poiContext.workbook.createCellStyle();
		exchangeRateStyle.setFont(poiContext.arialFontItalic);

		//var row = poiContext.sheetMap.get(sheetNameEnum).createRow(getNextRowNumber(poiContext, sheetNameEnum));
		var row = getNextRow(poiContext, sheetNameEnum);
		var rowNum = row.getRowNum();
		poiContext.sheetMap.get(sheetNameEnum).addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 3));
		var cell = row.createCell(0);
		var cellText = String.format("*** %s to %s exchange rate as of %s: %.4f", usdToCadExchangeRate.getFromCurrency(),
				usdToCadExchangeRate.getToCurrency(), usdToCadExchangeRate.getAsOfDate().format(AppTimeUtils.DATE_FORMATTER),
				usdToCadExchangeRate.getRate());
		cell.setCellValue(cellText);
		cell.setCellStyle(exchangeRateStyle);
		
	}

//	private void printTestColorsLine(PoiContext poiContext, XSSFSheet sheet, XSSFWorkbook workbook) {
//		XSSFCellStyle colorStyle;
//		var row = sheet.createRow(getNextFixedIncomeRowNumber(poiContext));
//		XSSFCell cell;
//		for (int i=0; i<=64; i++) {
//			if (i > 31 && i <= 40) {
//				continue;
//			}
//			cell = row.createCell(i);
//			colorStyle = workbook.createCellStyle();
//			cell.setCellValue(IndexedColors.fromInt(i).name());
//			
//			colorStyle.setFillForegroundColor((short)i);
//			colorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//			
//			cell.setCellStyle(colorStyle);
//		}
//	}

	
	private double printFixedIncomeDetailLines(Instant now, PoiContext poiContext, List<InstrumentDueV> fixedIncomeList, ExchangeRate usdToCadExchangeRate) {
		
		var sheetNameEnum = SheetNameEnum.FIXED_INCOME; 

		var arialFontStyle = poiContext.arialFontStyle;
		var balanceStyle = poiContext.amountStyle;
		var dateStyle = poiContext.dateStyle; 
		var rateStyle = poiContext.rateStyle;
		
		Function<Float, XSSFCellStyle> returnRateStyle = rate -> (rate.intValue() != rate.floatValue() ? rateStyle : arialFontStyle);
		
		
		var dateStylePink = poiContext.dateStylePink;
		var totalAmountStyle = poiContext.totalAmountStyle;

		var oldFiName = StringUtils.EMPTY;
		var oldHolder = StringUtils.EMPTY;
		var totalBalance = 0d;
		var totalHolderBalance = 0d;
		for (InstrumentDueV instrumentDueV : fixedIncomeList) {

			if (! /* not */ instrumentDueV.getPortfolioHolder().equals(oldHolder) && StringUtils.isNotBlank(oldHolder) ||
					! /* not */ instrumentDueV.getPortfolioFiName().equals(oldFiName) && StringUtils.isNotBlank(oldFiName)) {

				var total = new Total(0d, totalHolderBalance);
				printTotal(poiContext, sheetNameEnum, TotalTypeEnum.HOLDER, total,  totalAmountStyle);
				totalHolderBalance = 0;
			}
			
			var row = getNextRow(poiContext, sheetNameEnum);

			
			XSSFCell cell = null;
			// Financial Institution
			if (! /* not */ instrumentDueV.getPortfolioFiName().equals(oldFiName)) {
				cell = row.createCell(FI_FINANCIAL_INSTITUTION_CELL);
				cell.setCellStyle(arialFontStyle);
				cell.setCellValue(instrumentDueV.getPortfolioFiName());
			}
	
			// Holder
			cell = row.createCell(FI_HOLDER_CELL);
			if (! /* not */ instrumentDueV.getPortfolioHolder().equals(oldHolder) ||
					! /* not */ instrumentDueV.getPortfolioFiName().equals(oldFiName)) {
				cell.setCellValue(instrumentDueV.getPortfolioHolder());
			} else {
				cell.setCellValue(StringUtils.EMPTY);
			}
			var holderEnum = HolderEnum.valueOf(instrumentDueV.getPortfolioHolder());
			var holderCellStyleMap = poiContext.holderCellStyleMap;
			cell.setCellStyle(holderCellStyleMap.get(holderEnum));
			
			// RegisteredAccount 
			cell = row.createCell(FI_REGISTERED_ACCOUNT_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getRegisteredAccount());

			// Instrument 
			cell = row.createCell(FI_INSTRUMENT_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getInstrumentName());

			// Instrument Account Number 
			cell = row.createCell(FI_INSTRUMENT_ACCOUNT_NUMBER_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getInstrumentAccountNumber());

			// Portfolio Account Number
			cell = row.createCell(FI_PORTFOLIO_ACCOUNT_NUMBER_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getPortfolioAccountNumber());
			
			var balance = instrumentDueV.getPrice().multiply(instrumentDueV.getQuantity()).doubleValue();
			
			// USD Balance
			if (instrumentDueV.getCurrency().equals(CurrencyEnum.USD.name())) {
				cell = row.createCell(FI_USD_BALANCE_CELL);
				cell.setCellType(CellType.NUMERIC);
				cell.setCellStyle(balanceStyle);
				cell.setCellValue(balance);
			}
			
			// Balance
			if (instrumentDueV.getCurrency().equals(CurrencyEnum.USD.name())) {
				cell = row.createCell(FI_BALANCE_CELL);
				cell.setCellType(CellType.NUMERIC);
				cell.setCellStyle(balanceStyle);
				balance = round(balance * usdToCadExchangeRate.getRate());
				cell.setCellValue(balance);
			} else {
				cell = row.createCell(FI_BALANCE_CELL);
				cell.setCellType(CellType.NUMERIC);
				cell.setCellStyle(balanceStyle);
				cell.setCellValue(balance);
			}
			totalBalance += balance;
			totalHolderBalance += balance;
			
			// Type
			cell = row.createCell(FI_TYPE_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getType());
			
			// Term
			cell = row.createCell(FI_TERM_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getTerm());
			
			// Rate
			if (instrumentDueV.getInterestRate() != null) {
				cell = row.createCell(FI_RATE_CELL);
				cell.setCellStyle(returnRateStyle.apply(instrumentDueV.getInterestRate()));
				cell.setCellValue(instrumentDueV.getInterestRate());
			}
			
			// Promo Rate
			if (instrumentDueV.getPromotionalInterestRate() != null) {
				cell = row.createCell(FI_PROMO_RATE_CELL);
				cell.setCellStyle(returnRateStyle.apply(instrumentDueV.getPromotionalInterestRate()));
				cell.setCellValue(instrumentDueV.getPromotionalInterestRate());
			}
			
			// Promo End Date
			if (instrumentDueV.getPromotionEndDate() != null) {
				cell = row.createCell(FI_PROMO_RATE_END_DATE_CELL);
				cell.setCellValue(instrumentDueV.getPromotionEndDate().toLocalDate());
				
				if (isWithinNextDays(now, instrumentDueV.getPromotionEndDate(), daysToNotify)) {
					cell.setCellStyle(dateStylePink);
				} else {
					cell.setCellStyle(dateStyle);
				}
				
			}
			
			// Maturity Date
			if (instrumentDueV.getMaturityDate() != null) {
				cell = row.createCell(FI_MATURITY_DATE_CELL);

				if (isWithinNextDays(now, instrumentDueV.getMaturityDate(), daysToNotify)) {
					cell.setCellStyle(dateStylePink);
				} else {
					cell.setCellStyle(dateStyle);
				}
				cell.setCellValue(instrumentDueV.getMaturityDate().toLocalDate());
			
			}

			// Notes 
			cell = row.createCell(FI_NOTES_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(instrumentDueV.getNotes());

			oldFiName = instrumentDueV.getPortfolioFiName();
			oldHolder = instrumentDueV.getPortfolioHolder();
		}

		var totalHolder = new Total(0, totalHolderBalance);
		printTotal(poiContext, sheetNameEnum, TotalTypeEnum.HOLDER, totalHolder,  totalAmountStyle);

		var totalGrand = new Total(0, totalBalance);
		printTotal(poiContext, sheetNameEnum, TotalTypeEnum.GRAND, totalGrand, totalAmountStyle);
		
		return totalBalance;
	}

	private double printEquityDetailLines(PoiContext poiContext,	List<IEquityReport> equityList, ExchangeRate usdToCadExchangeRate) {
		
		var sheetNameEnum = SheetNameEnum.EQUITY;

		var arialFontStyle = poiContext.arialFontStyle;
		var amountStyle = poiContext.amountStyle;
		var totalAmountStyle = poiContext.totalAmountStyle;

		var oldFinancialInstitution = StringUtils.EMPTY;
		HolderEnum oldHolder = null;
		var oldPortfolioId = StringUtils.EMPTY;
		CurrencyEnum oldCurrency = null;

		var totalCadMarketValue = 0d;
		var totalPortfolioMarketValue = 0d;
		var totalPortfolioCadMarketValue = 0d;
		var totalHolderCadMarketValue = 0d;
		for (IEquityReport equityReport : equityList) {
			
			if (! /* not */ StringUtils.equals(equityReport.getPortfolioId(), oldPortfolioId) && StringUtils.isNotBlank(oldPortfolioId) ||
					! /* not */ equityReport.getHolder().equals(oldHolder) && oldHolder != null ||
					! /* not */ StringUtils.equals(equityReport.getFinancialInstitution(), oldFinancialInstitution) && StringUtils.isNotBlank(oldFinancialInstitution)) {

				var totalPortfolio = new Total(totalPortfolioMarketValue, totalPortfolioCadMarketValue);
				printTotal(poiContext, sheetNameEnum, TotalTypeEnum.PORTFOLIO, totalPortfolio,  totalAmountStyle);
				totalPortfolioMarketValue = 0;
				totalPortfolioCadMarketValue = 0;
			}
			
			if (! /* not */ equityReport.getHolder().equals(oldHolder) && oldHolder != null ||
					! /* not */ StringUtils.equals(equityReport.getFinancialInstitution(), oldFinancialInstitution) && StringUtils.isNotBlank(oldFinancialInstitution)) {

				var totalHolder = new Total(0, totalHolderCadMarketValue);
				printTotal(poiContext, sheetNameEnum, TotalTypeEnum.HOLDER, totalHolder,  totalAmountStyle);
				totalHolderCadMarketValue = 0;
			}
			
			var row = getNextRow(poiContext, sheetNameEnum);
			
			XSSFCell cell = null;
			// Financial Institution
			if (! /* not */ StringUtils.equals(equityReport.getFinancialInstitution(), oldFinancialInstitution)) {
				cell = row.createCell(EQ_FINANCIAL_INSTITUTION_CELL);
				cell.setCellStyle(arialFontStyle);
				cell.setCellValue(equityReport.getFinancialInstitution());
			}
	
			// Holder
			cell = row.createCell(EQ_HOLDER_CELL);
			if (! /* not */ equityReport.getHolder().equals(oldHolder) ||
					! /* not */ equityReport.getFinancialInstitution().equals(oldFinancialInstitution)) {
				cell.setCellValue(equityReport.getHolder().getName());
			} else {
				cell.setCellValue(StringUtils.EMPTY);
			}
			var holderCellStyleMap = poiContext.holderCellStyleMap;
			cell.setCellStyle(holderCellStyleMap.get(equityReport.getHolder()));
			
			// Portfolio ID 
			if (! /* not */ equityReport.getPortfolioId().equals(oldPortfolioId)) {
				cell = row.createCell(EQ_PORTFOLIO_ID_CELL);
				cell.setCellStyle(arialFontStyle);
				cell.setCellValue(equityReport.getPortfolioId());
			// Portfolio Name
				cell = row.createCell(EQ_PORTFOLIO_NAME_CELL);
				cell.setCellStyle(arialFontStyle);
				cell.setCellValue(equityReport.getPortfolioName());
			}

			// Currency 
			if (! /* not */ StringUtils.equals(equityReport.getFinancialInstitution(), oldFinancialInstitution) ||
					! /* not */ equityReport.getHolder().equals(oldHolder) ||
					! /* not */ StringUtils.equals(equityReport.getPortfolioId(), oldPortfolioId) ||
					! /* not */ equityReport.getCurrency().equals(oldCurrency)) {
				cell = row.createCell(EQ_CURRENCY_CELL);
				cell.setCellStyle(arialFontStyle);
				cell.setCellValue(equityReport.getCurrency().name());
			}
			
			// Instrument Name
			cell = row.createCell(EQ_INSTRUMENT_NAME_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(equityReport.getInstrumentName());
			
			// Ticker
			cell = row.createCell(EQ_TICKER_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(equityReport.getTicker());
			
			// Instrument Type
			cell = row.createCell(EQ_INSTRUMENT_TYPE_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(equityReport.getInstrumentType().name());
			
			// Quantity
			cell = row.createCell(EQ_QUANTITY_CELL);
			cell.setCellType(CellType.NUMERIC);
			cell.setCellStyle(amountStyle);
			cell.setCellValue(equityReport.getQuantity().doubleValue());
			
			// Price
			cell = row.createCell(EQ_PRICE_CELL);
			cell.setCellType(CellType.NUMERIC);
			cell.setCellStyle(amountStyle);
			cell.setCellValue(equityReport.getPrice().doubleValue());

			// Timestamp
			cell = row.createCell(EQ_PRICE_TIMESTAMP_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(equityReport.getPriceTimestamp().substring(0, 10));
			
			// Timestamp Source
			cell = row.createCell(EQ_PRICE_TIMESTAMP_FROM_SOURCE_CELL);
			cell.setCellStyle(arialFontStyle);
			cell.setCellValue(equityReport.getPriceTimestampFromSource());
			
			// Market Value
			var marketValue = equityReport.getPrice().multiply(equityReport.getQuantity()).doubleValue();
			totalPortfolioMarketValue += marketValue;
			cell = row.createCell(EQ_MARKET_VALUE_CELL);
			cell.setCellType(CellType.NUMERIC);
			cell.setCellStyle(amountStyle);
			cell.setCellValue(marketValue);
			
			// CAD Market Value
			double cadMarketValue;
			if (equityReport.getCurrency().equals(CurrencyEnum.USD)) {
				cadMarketValue = round(marketValue * usdToCadExchangeRate.getRate());
				totalCadMarketValue += cadMarketValue;
			} else {
				cadMarketValue = marketValue;
				totalCadMarketValue += marketValue;
			}
			totalPortfolioCadMarketValue += cadMarketValue;
			totalHolderCadMarketValue += cadMarketValue;
			
			cell = row.createCell(EQ_CAD_MARKET_VALUE_CELL);
			cell.setCellType(CellType.NUMERIC);
			cell.setCellStyle(amountStyle);
			cell.setCellValue(cadMarketValue);

						
			oldFinancialInstitution = equityReport.getFinancialInstitution();
			oldHolder = equityReport.getHolder();
			oldPortfolioId = equityReport.getPortfolioId();
			oldCurrency = equityReport.getCurrency();
		}

		
		var portfolioHolder = new Total(totalPortfolioMarketValue, totalPortfolioCadMarketValue);
		printTotal(poiContext, sheetNameEnum, TotalTypeEnum.PORTFOLIO, portfolioHolder, totalAmountStyle);
		
		var totalHolder = new Total(0, totalHolderCadMarketValue);
		printTotal(poiContext, sheetNameEnum, TotalTypeEnum.HOLDER, totalHolder, totalAmountStyle);
		
		var totalGran = new Total(0, totalCadMarketValue);
		printTotal(poiContext, sheetNameEnum, TotalTypeEnum.GRAND, totalGran, totalAmountStyle);
		
		
		return totalCadMarketValue;

	}

	private short getHolderCellColor(HolderEnum holderEnum) {
		return IndexedColors.valueOf(holderEnum.getReportCellColor()).getIndex();
	}
	
	private double round(double balance) {
		return BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	
	
	private enum TotalTypeEnum {HOLDER, PORTFOLIO, GRAND}
	private void printTotal(PoiContext poiContext, SheetNameEnum sheetNameEnum, TotalTypeEnum totalTypeEnum, Total total, XSSFCellStyle totalStyle) {

		var row = getNextRow(poiContext, sheetNameEnum);
		
		var cell = row.createCell(
				switch (totalTypeEnum) {
				case GRAND -> 0;
				case HOLDER -> 1;
				case PORTFOLIO -> 2;
				}
				);
		cell.setCellValue("Total");
		
		if (totalTypeEnum.equals(TotalTypeEnum.HOLDER)) {
			var cellStyle = getCellAboveStyle(poiContext, sheetNameEnum, row);
			cellStyle.setFont(poiContext.arialFontBold);
			cell.setCellStyle(cellStyle);
		} else {
			cell.setCellStyle(poiContext.boldStyle);
		}
		
		if (sheetNameEnum.equals(SheetNameEnum.EQUITY) && totalTypeEnum.equals(TotalTypeEnum.PORTFOLIO)) {
			cell = row.createCell(EQ_HOLDER_CELL);
			cell.setCellValue(StringUtils.EMPTY);
			var cellStyle = getCellAboveStyle(poiContext, sheetNameEnum, row);
			cell.setCellStyle(cellStyle);
			
			cell = row.createCell(EQ_MARKET_VALUE_CELL);
			cell.setCellValue(total.total());
			cell.setCellType(CellType.NUMERIC);
			cell.setCellStyle(totalStyle);
		}
		
		cell = row.createCell(
				switch (sheetNameEnum) {
				case FIXED_INCOME -> FI_BALANCE_CELL;
				case EQUITY -> EQ_CAD_MARKET_VALUE_CELL;
				case SUMMARY -> throw new UnsupportedOperationException();
				}
				);
		cell.setCellValue(total.cadTotal());
		cell.setCellType(CellType.NUMERIC);
		cell.setCellStyle(totalStyle);
	}
	
	private XSSFCellStyle getCellAboveStyle(PoiContext poiContext, SheetNameEnum sheetNameEnum, XSSFRow row) {
		var previousRow = poiContext.sheetMap.get(sheetNameEnum).getRow(row.getRowNum() - 1);
		var cellAbove = previousRow.getCell(1);
		return cellAbove.getCellStyle().copy();
	}

	private boolean isWithinNextDays(Instant now, OffsetDateTime dateToCheck, Long days) {
		Long numberOfDays = AppTimeUtils.daysBetween(AppTimeUtils.toOffsetDateTime(now), dateToCheck);
		LOGGER.debug("daysBetween({}, {}) = {}", now, dateToCheck, numberOfDays);
		return numberOfDays.compareTo(days) <= 0;
	}
	
	private void printFixedIncomeHeader(PoiContext poiContext) {

		var sheetNameEnum = SheetNameEnum.FIXED_INCOME;

		var row = getNextRow(poiContext, sheetNameEnum);

		var cell = row.createCell(FI_FINANCIAL_INSTITUTION_CELL);
		cell.setCellValue("Financial Institution");
		
		cell = row.createCell(FI_HOLDER_CELL);
		cell.setCellValue("Holder");
		
		cell = row.createCell(FI_REGISTERED_ACCOUNT_CELL);
		cell.setCellValue("Reg");
		
		cell = row.createCell(FI_INSTRUMENT_CELL);
		cell.setCellValue("Instrument");
		
		cell = row.createCell(FI_INSTRUMENT_ACCOUNT_NUMBER_CELL);
		cell.setCellValue("Instrument Acc No");
		
		cell = row.createCell(FI_PORTFOLIO_ACCOUNT_NUMBER_CELL);
		cell.setCellValue("Portfolio Acc No");
		
		cell = row.createCell(FI_USD_BALANCE_CELL);
		cell.setCellValue("USD Balance");
		
		cell = row.createCell(FI_BALANCE_CELL);
		cell.setCellValue("Balance");
		
		cell = row.createCell(FI_TYPE_CELL);
		cell.setCellValue("Type");
		
		cell = row.createCell(FI_TERM_CELL);
		cell.setCellValue("Term");
		
		cell = row.createCell(FI_RATE_CELL);
		cell.setCellValue("Rate");
		
		cell = row.createCell(FI_PROMO_RATE_CELL);
		cell.setCellValue("Promo Rate");
		
		cell = row.createCell(FI_PROMO_RATE_END_DATE_CELL);
		cell.setCellValue("Promo End Date");
		
		cell = row.createCell(FI_MATURITY_DATE_CELL);
		cell.setCellValue("Maturity Date");		

		cell = row.createCell(FI_NOTES_CELL);
		cell.setCellValue("Notes");		

		setRowStyle(row, poiContext.boldFontGreenBackground);
		
		poiContext.sheetMap.get(sheetNameEnum).createFreezePane(0, 1); // freeze header row
	}
	
	private void printEquityHeader(PoiContext poiContext) {

		var sheetNameEnum = SheetNameEnum.EQUITY;
		//var row = poiContext.sheetMap.get(SheetNameEnum.EQUITY).createRow(getNextEquityRowNumber(poiContext));
		var row = getNextRow(poiContext, sheetNameEnum);

		var cell = row.createCell(EQ_FINANCIAL_INSTITUTION_CELL);
		cell.setCellValue("Financial Institution");
		
		cell = row.createCell(EQ_HOLDER_CELL);
		cell.setCellValue("Holder");
		
		cell = row.createCell(EQ_PORTFOLIO_ID_CELL);
		cell.setCellValue("Portfolio ID");
		
		cell = row.createCell(EQ_PORTFOLIO_NAME_CELL);
		cell.setCellValue("Portfolio Name");
		
		cell = row.createCell(EQ_CURRENCY_CELL);
		cell.setCellValue("Currency");
		
		cell = row.createCell(EQ_INSTRUMENT_NAME_CELL);
		cell.setCellValue("Instrument Name");
		
		cell = row.createCell(EQ_TICKER_CELL);
		cell.setCellValue("Ticker");
		
		cell = row.createCell(EQ_INSTRUMENT_TYPE_CELL);
		cell.setCellValue("Type");
		
		cell = row.createCell(EQ_QUANTITY_CELL);
		cell.setCellValue("Quantity");
		
		cell = row.createCell(EQ_PRICE_CELL);
		cell.setCellValue("Price");
		
		cell = row.createCell(EQ_PRICE_TIMESTAMP_CELL);
		cell.setCellValue("Timestamp");
		
		cell = row.createCell(EQ_PRICE_TIMESTAMP_FROM_SOURCE_CELL);
		cell.setCellValue("Timestamp Source");
		
		cell = row.createCell(EQ_MARKET_VALUE_CELL);
		cell.setCellValue("Market Value");
		
		cell = row.createCell(EQ_CAD_MARKET_VALUE_CELL);
		cell.setCellValue("CAD Market Value");
		
		setRowStyle(row, poiContext.boldFontGreenBackground);
		
		poiContext.sheetMap.get(SheetNameEnum.EQUITY).createFreezePane(0, 1); // freeze header row
	}

	private void setRowStyle(XSSFRow row, XSSFCellStyle style) {
		// getLastCellNum() gets the last cell number plus 1
		for (int i=0; i<row.getLastCellNum(); i++) {
			row.getCell(i).setCellStyle(style);
		}
	}

	private XSSFRow getNextRow(PoiContext poiContext, SheetNameEnum sheetNameEnum) {
		var rn = poiContext.rowNumberMap.get(sheetNameEnum);
		poiContext.rowNumberMap.put(sheetNameEnum, rn + 1);
		return poiContext.sheetMap.get(sheetNameEnum).createRow(rn);
	}

	public File generateAndEmail() throws ApplicationException {
		LOGGER.info(LOG_BEGIN);
		var excelFile = generate();
		emailService.sendHoldingsReport(excelFile);
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
