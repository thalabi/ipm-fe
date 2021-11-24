<html>
<head>
  <title>Daily market value failure</title>
</head>
<body>

  <#if (priceHoldingsExceptions.messageList?size gt 0)>
     <span style="font-style: underline;">Daily market value processing failed with the following exceptions:</span>
	  <#list priceHoldingsExceptions.messageList as message>
		  <ul style="font-style: italic;">
		    <li>${message}</li>
		  </ul>
	  </#list>
  </#if>

</body>
</html>