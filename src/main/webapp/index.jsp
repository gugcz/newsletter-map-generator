<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>GUG Newsletter</title>
    <script type="text/javascript">
        function setDefaultValues() {
            var date = new Date();
            date.setDate(date.getDate() + 25);
            document.getElementById("selectMonth").value = date.getMonth() + 1;
            document.getElementById("selectYear").value = date.getFullYear();
        }
    </script>
</head>
<body onload="setDefaultValues()">
<h2>Automatické generování GUG newsletteru</h2>
<form action="createNewsletter" method="post">
    <select id="selectMonth" name="month">
        <option value="1">Leden</option>
        <option value="2">Únor</option>
        <option value="3">Březen</option>
        <option value="4">Duben</option>
        <option value="5">Květen</option>
        <option value="6">Červen</option>
        <option value="7">Červenec</option>
        <option value="8">Srpen</option>
        <option value="9">Září</option>
        <option value="10">Říjen</option>
        <option value="11">Listopad</option>
        <option value="12">Prosinec</option>
    </select>
    <select id="selectYear" name="year">
        <option value="2010">2010</option>
        <option value="2011">2011</option>
        <option value="2012">2012</option>
        <option value="2013">2013</option>
        <option value="2014">2014</option>
        <option value="2015">2015</option>
        <option value="2016">2016</option>
        <option value="2017">2017</option>
        <option value="2018">2018</option>
    </select>
    <input type="submit" value="Vytvoř newsletter">

</form>
</body>
</html>
