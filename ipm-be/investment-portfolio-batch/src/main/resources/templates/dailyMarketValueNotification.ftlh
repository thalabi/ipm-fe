<html>
<head>
  <title>Daily market value notification</title>
</head>
<body>

  <#if (priceHoldingsExceptions.messageList?size gt 0)>
     <span style="text-decoration: underline;">The balance below is not accurate as there are exceptions during pricing. See below:</span>
	  <#list priceHoldingsExceptions.messageList as message>
		  <ul style="font-style: italic;">
		    <li>${message}</li>
		  </ul>
	  </#list>
  </#if>


  <h3>Market value as of ${todaysSnapshot?datetime} is
	  <#if (priceHoldingsExceptions.messageList?size == 0)>
	    ${todaysMarketValue?string.currency}
	  <#else>
	    <span style="opacity: 0.7;">${todaysMarketValue?string.currency}</span> <!-- use 'opacity' to greyout market value --> 
	  </#if>
  </h3>   
  
  Change of <b>
  <#if percentChange lt 0>
  <span style="color: red;"><img src="${downArrowUrl}" style="vertical-align: middle;" alt="Down arrow">${percentChange}%</span>
  <#else>
  <span style="color: green;"><img src="${upArrowUrl}" style="vertical-align: middle;" alt="Up arrow">${percentChange}%</span>
  </#if>
  </b>
  
  <table style="margin-top: 1rem;" cellspacing="10">
  <caption>Last four days market value:</caption>
  <tr>
    <th>As of</th>
    <th>Market Value</th>
  </tr>
  <#list nMarketValues as marketVlaueRow>
	  <tr>
	    <td>${marketVlaueRow.positionSnapshot[0..9]}</td>
	    <td>${marketVlaueRow.marketValue?string.currency}</td>
	  </tr>
  </#list>
  </table>

</body>
</html>