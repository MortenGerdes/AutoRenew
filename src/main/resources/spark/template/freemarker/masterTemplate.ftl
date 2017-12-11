<#macro masterTemplate title="Welcome">
<!DOCTYPE html
        PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Autorenew</title>
</head>
<body>
<div class="page">
    <h1>AutoRenew</h1>
    <p>A server which renews your application on ungdomsboligaarhus.dk for you.</p>
    <div class="body">
        <#nested />
    </div>
    <div class="footer">
        Autorenew &mdash; A Spark Application made by Morten Gerdes
    </div>
</div>
</body>
</html>
</#macro>