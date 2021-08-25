<html>
<head>
  <title>Reset password</title>
</head>
<body>
  <h1>Reset password</h1>
  <p>Please click the link below to reset you password. The link expires in ${linkExpiryInHours} <#if linkExpiryInHours == 1>hour<#else>hours</#if> and can only be used to change your password once.</p>
  
  <table width="100%" cellspacing="0" cellpadding="0">
    <tr>
	  <td>
	    <table cellspacing="0" cellpadding="0">
          <tr>
	        <td style="border-radius: 2px;" bgcolor="#007ad9">
	          <a href="${resetPasswordUrl}" target="_blank" style="padding: 8px 12px; border: 1px solid #007ad9;border-radius: 2px;font-family: Helvetica, Arial, sans-serif;font-size: 14px; color: #ffffff;text-decoration: none;font-weight:bold;display: inline-block;">
				Reset Password             
	          </a>
	        </td>
	      </tr>
	    </table>
	  </td>
    </tr>
  </table>
  
  <p>If you experience any issues with the button above, copy and paste the URL below into your web browser.</p>
  ${resetPasswordUrl}
  
  <p>If you did not request your password to be set, you can safely ignore this email and your password will remain the same.</p>
  
</body>
</html>