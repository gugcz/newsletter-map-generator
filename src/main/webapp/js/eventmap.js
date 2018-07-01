var map;
var cities = {};

function init() {
    initMap();
    readData();
}

function readData() {

    var month = getParameterByName("month");
    var year = getParameterByName("year");

    $.ajax({
        url: "getMapData",
        dataType: "json",
        data: {
            "year": year,
            "month": month
        }
    }).done(function (data) {
        for (var city in GROUP_POSITIONS) {
            if (data.hasOwnProperty(city)) {
                createMarkersForCity(city, data[city]);
            }
        }
    }).fail(function (jqXHR, textStatus, errorThrown) {
        errorMessage("Failed to read events (" + textStatus + ")");
        console.error(errorThrown);
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

function createMarkersForCity(cityName, events) {
    var groupPosition = GROUP_POSITIONS[cityName];
    if (!groupPosition) {
        cities[cityName] = null;
        return;
    }
    var padding = 10;
    var div = $("<div/>")[0];

    var width = 210;
    var height = 10;
    var index = 0;
    for (var i = 0; i < events.length; i++) {
        var event = events[i];

        var eventDay = event.date.substring(0, event.date.indexOf("\.") + 1);

        var table = $("<table/>").addClass("labelTable")
            .css("position", "relative")
            .css("left", (index * 10) + "px")
            .css("top", -(index * 10) + "px")
            .appendTo(div);
        var tr = $("<tr/>").appendTo(table);
        var groupStyle = event.groupShortcut.toLowerCase();
        $("<td/>").addClass("date " + groupStyle).html(eventDay).appendTo(tr);
        $("<td/>").addClass("name").html(event.name).appendTo(tr);

        width += padding;
        height += 45;
        index++;
    }

    div.style.width = width + "px";

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
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}