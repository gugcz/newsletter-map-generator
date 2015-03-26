<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>GUG Newsletter</title>
</head>
<body>
<h2>Automatické generování GUG newsletteru</h2>
<form action="createNewsletter" method="post">
    <select name="month">
        <option name="1">Leden</option>
        <option name="2">Únor</option>
        <option name="3">Březen</option>
        <option name="4">Duben</option>
        <option name="5">Květen</option>
        <option name="6">Červen</option>
        <option name="7">Červenec</option>
        <option name="8">Srpen</option>
        <option name="9">Září</option>
        <option name="10">Říjen</option>
        <option name="11">Listopad</option>
        <option name="12">Prosinec</option>
    </select>
    <select name="year">
        <option name="2015">2015</option>
    </select>
    <input type="submit" value="Vytvoř newsletter">

</form>
</body>
</html>
