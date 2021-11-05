<html>
<head>
  <title>Daily market value notification</title>
</head>
<body>
  <h3>Today's market value as of ${todaysSnapshot?datetime} is ${todaysMarketValue?string.currency}</h3>
  
  <table>
  <caption>Last four days market value:</caption>
  <tr>
    <th>As of</th>
    <th>Market Value</th>
  </tr>
  <#list nMarketValues as marketVlaueRow>
	  <tr>
	    <td>${marketVlaueRow.positionSnapshot}</td>
	    <td>${marketVlaueRow.marketValue?string.currency}</td>
	  </tr>
  </#list>
  </table>

</body>
</html>