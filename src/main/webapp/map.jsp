<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>GUG Newsletter map</title>
    <link rel="stylesheet" href="eventmap.css" type="text/css"/>

    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
    <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?v=3&amp;sensor=false"></script>
    <script type="text/javascript" src="grouppositions.js"></script>
    <script type="text/javascript" src="markerwithlabel.js"></script>
    <script type="text/javascript" src="eventmap.js"></script>


</head>
<body onload="init()">
<div id="map_canvas" style="height: 800px; width: 100%;"></div>
<div id="message"></div>
</body>
</html>