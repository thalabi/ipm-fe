<html>
<head>
  <title>Instrument(s) due notification</title>
  <style type="text/css">

  tr {
    line-height: 1rem;
    background-color: #e4e4e4; 
  }
  .red {
    color: red;
  }

  </style>
</head>

<#function checkOverdue overdue>
  <#if (overdue)>
    <#return "red" />
  <#else>
    <#return "" />
  </#if>
</#function>

<body>

  <h3>The following
	  <#if (instrumentDueVList?size == 1)>
	  instrument
	  </#if>
	  <#if (instrumentDueVList?size > 1)>
	  ${instrumentDueVList?size} instruments
	  </#if>
  will be due within the next ${daysToNotify} days:</h3>
  
  <#if (overdueInstrumentFound)>
  	<h3>*** Dates marked in <span class="red">red</span> are overdue ***</h3>
  </#if>
  
  <table>
  <#list instrumentDueVList as instrumentDueV>

  <tr>
  	<td>Portfolio:</td> <td><strong>${instrumentDueV.portfolioName}</strong></td>
  </tr>

  <#if ((instrumentDueV.portfolioHolder)??)>
	  <tr>
	  	<td>Holder:</td> <td><strong>${instrumentDueV.portfolioHolder}</strong></td>
	  </tr>
  </#if>

  <#if ((instrumentDueV.registeredAccount)??)>
	  <tr>
	  	<td>Registered:</td> <td><strong>${instrumentDueV.registeredAccount}</strong></td>
	  </tr>
  </#if>

  <#if ((instrumentDueV.instrumentName)??)>
	  <tr>
	  	<td>Instrument:</td> <td><strong>${instrumentDueV.instrumentName}</strong></td>
	  </tr>
  </#if>

  <#if ((instrumentDueV.cusip)?? && instrumentDueV.type == "BOND")>
	  <tr >
	  	<td>Cusip:</td> <td><strong>${instrumentDueV.cusip}</strong></td>
	  </tr>
  </#if>

  <#if ((instrumentDueV.issuerFiName)?? && instrumentDueV.type != "BOND")>
	  <tr>
	  	<td>Financial institution:</td> <td><strong>${instrumentDueV.issuerFiName}</strong></td>
	  </tr>
  </#if>

  <#if ((instrumentDueV.issuerEntity)?? && instrumentDueV.type == "BOND")>
	  <tr>
	  	<td>Issuer:</td> <td><strong>${instrumentDueV.issuerEntity}</strong></td>
	  </tr>
  </#if>

  <#if ((instrumentDueV.type)??)>
    <tr>
  	  <td>Type:</td> <td><strong>${instrumentDueV.type}</strong></td>
    </tr>
  </#if>
  
  <#if ((instrumentDueV.currency)??)>
    <tr>
  	  <td>Currency:</td> <td><strong>${instrumentDueV.currency}</strong></td>
    </tr>
  </#if>
  
  <#if ((instrumentDueV.term)??)>
	  <tr>
	  	<td>Term:</td> <td><strong>${instrumentDueV.term}</strong></td>
	  </tr>
  </#if>
  
  <#if ((instrumentDueV.maturityDate)?? && instrumentDueV.type != "BOND")>
	  <tr>
	  	<td>Maturity date:</td> <td class="${checkOverdue(instrumentDueV.overdue)}"><strong>${instrumentDueV.maturityDate[0..9]}</strong></td>
	  </tr>
  </#if>

  <#if (instrumentDueV.type == "BOND")>
  	<#if ((instrumentDueV.issueDate)??)>
	  <tr>
	  	<td>Issue date:</td> <td><strong>${instrumentDueV.issueDate[0..9]}</strong></td>
	  </tr>
	</#if>
  	<#if ((instrumentDueV.paymentFrequency)??)>
	  <tr>
	  	<td>Payment frequency:</td> <td><strong>${instrumentDueV.paymentFrequency}</strong></td>
	  </tr>
	</#if>
  	<#if ((instrumentDueV.nextPaymentDate)??)>
	  <tr>
	  	<td>Next payment date:</td> <td class="${checkOverdue(instrumentDueV.overdue)}"><strong>${instrumentDueV.nextPaymentDate[0..9]}</strong></td>
	  </tr>
	</#if>
  </#if>
  
  <#if ((instrumentDueV.price)?? && instrumentDueV.type == "BOND")>
	  <tr>
	  	<td>Price:</td> <td><strong>${instrumentDueV.price?string.currency}</strong></td>
	  </tr>
  </#if>
  
  <#if ((instrumentDueV.quantity)??)>
	  <tr>
	    <#if ((instrumentDueV.type)??)>
	      <#if (instrumentDueV.type == "BOND")>
	  	    <td>Quantity:</td> <td><strong>${instrumentDueV.quantity}</strong></td>
	  	  <#else>
	  	    <td>Principal:</td> <td><strong>${instrumentDueV.quantity?string.currency}</strong></td>
	  	  </#if>
        </#if>
	  </tr>
  </#if>
  
  <#if ((instrumentDueV.promotionalInterestRate)??)>
	  <tr>
	  	<td>Promotional rate:</td> <td><strong>${instrumentDueV.promotionalInterestRate}</strong></td>
	  </tr>
  </#if>
  
  <#if ((instrumentDueV.interestRate)??)>
	  <tr>
	  	<td>Rate:</td> <td><strong>${instrumentDueV.interestRate}</strong></td>
	  </tr>
  </#if>
  
  <#if ((instrumentDueV.promotionEndDate)??)>
	  <tr>
	  	<td>Promotion end date:</td> <td class="${checkOverdue(instrumentDueV.overdue)}"><strong>${instrumentDueV.promotionEndDate[0..9]}</strong></td>
	  </tr>
  </#if>

  <tr style="background-color: rgba(0,0,0,0) !important;">
  	<td>&nbsp;</td> <td>&nbsp;</td>
  </tr>
  
  </#list>
  </table>


</body>
</html>