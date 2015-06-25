var map;
var cities = {};

function init() {
    initMap();
    readData();
}

function readData() {
    $.ajax({
        url: "testData.json",
        //force to handle it as text
        dataType: "text",
        success: function(data) {
            var json = $.parseJSON(data);
            for (var city in GROUP_POSITIONS) {
                //createMarker(homeLatLng, json[0].date_from, json[0].event_name, json[0].g_plus_event_link);
                if (GROUP_POSITIONS.hasOwnProperty(city)) {
                    createMarker(GROUP_POSITIONS[city], new Date(), city, "");
                }
            }
            writeCoordinates();
        }
    });
}

function initMap() {
    var latLng = new google.maps.LatLng(49.84264, 15.46619);

    map = new google.maps.Map(document.getElementById('map_canvas'), {
        zoom: 8,
        center: latLng,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    });
}

function createMarker(groupPosition, date, cityName, link) {
    if (!groupPosition){
        cities[cityName] = null;
        return;
    }
    var count = 2;
    var padding = 10;
    var div = $("<div/>")[0];

    var width = 180;
    var height = 10;
    for (var i = 0; i < count; i++) {
        var table = $("<table/>").addClass("labelTable")
            .css("position", "relative")
            .css("left", (i * 10) + "px")
            .css("top", -(i * 10) + "px")
            .appendTo(div);
        var tr = $("<tr/>").appendTo(table);
        $("<td/>").addClass("date gdg").html(new Date(date).getDate() + ".").appendTo(tr);
        $("<td/>").addClass("name").html(cityName).appendTo(tr);

        width += padding;
        height += 45;
    }

    div.style.width = width + "px";
    div.style.height = height + "px";

    var latLng = new google.maps.LatLng(groupPosition.lat, groupPosition.lng);

    var marker = new MarkerWithLabel({
        icon: " ",
        position: latLng,
        map: map,
        draggable: true,
        labelContent: div,
        labelAnchor: new google.maps.Point(width * groupPosition.offsetX, height * groupPosition.offsetY)
    });

    cities[cityName] = {"marker": marker, "groupPosition": groupPosition};

    google.maps.event.addListener(marker, "click", function () {
        window.open(link)
    });
    google.maps.event.addListener(marker, "dragend", writeCoordinates);
}

function writeCoordinates() {
    var message = $("#message").empty();
    for (var cityName in cities){
        if (cities.hasOwnProperty(cityName)) {
            var label = cities[cityName];
            var content;
            if (label) {
                content = "{lng: " + label.marker.position.lng() + ", lat: " + label.marker.position.lat()
                + ", offsetX: " + label.groupPosition.offsetX + ", offsetY: " + label.groupPosition.offsetY + "}";
            } else {
                content = "null";
            }
            $("<div/>").html("\"" + cityName + "\": " + content + ", ").appendTo(message);
        }
    }
}