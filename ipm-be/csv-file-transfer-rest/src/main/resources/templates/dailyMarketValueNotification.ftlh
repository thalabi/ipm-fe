<html>
<head>
  <title>Daily market value notification</title>
</head>
<body>
  <h3>Market value as of ${todaysSnapshot?datetime} is ${todaysMarketValue?string.currency}</h3>
  
  Change of <b>
  <#if percentChange lt 0>
  <span style="color: red;"><img src="http://kerneldc.com/ipm/assets/arrow-down-right-red.png" style="vertical-align: middle;" alt="Down arrow">${percentChange}%</span>
  <#else>
  <span style="color: green;"><img src="http://kerneldc.com/ipm/assets/arrow-up-right-green.png" style="vertical-align: middle;" alt="Up arrow">${percentChange}%</span>
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