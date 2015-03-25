function generateNewsletter() {
    var month = getParameterByName("month");
    var year = getParameterByName("year");

    $.ajax({
        //url: CONFIG.GUG_WEB_ENDPOINT,
        url: "testData.json",
        dataType: "json",
        data: {
            "Token": CONFIG.GUG_WEB_API_KEY,
            "date_from": "2010-02-17T17:00:00+01:00",
            "date_to": "2011-02-17T17:00:00+01:00",
            "date_from_status": "known"
        }
    }).done(function (data) {
        createNewsletterFromEvents(data._embedded.event_occurrences);
    }).fail(function (jqXHR, textStatus, errorThrown) {
        alert("Faild to read events.\n" + textStatus);
    });
}

function createNewsletterFromEvents(events) {
    var mailContent = createMailContent(events);

    var requestData = {
        "apikey": CONFIG.MAILCHIMP_API_KEY,
        "type": "regular",
        "options": {
            "list_id": CONFIG.MAIL_LIST_ID,
            "template_id": CONFIG.TEMPLATE_ID,
            "from_email": CONFIG.FROM_EMAIL,
            "from_name": CONFIG.FROM_NAME,
            "subject": "Duben 2015"
        },
        "content": mailContent
    };
    console.log(requestData);
}

function createMailContent(events) {
    var sections = {};
    var gdgIndex = 0;
    $.each(events, function (i, event) {
        if (event.is_published) {
            var fieldNameStart = "repeat_1:" + gdgIndex + ":gdg_";
            sections[fieldNameStart + "event_title"] = event.event_name;
            sections[fieldNameStart + "event_description"] = event.event_tagline;
            sections[fieldNameStart + "event_date"] = event.date_from;
            sections[fieldNameStart + "event_button"] = "Registrace";
            if (event._embedded.venue) {
                sections[fieldNameStart + "event_location"] = event._embedded.venue.address;
            }
            gdgIndex++;
        }
    });


    var mailContent = {};
    mailContent.sections = sections;
    return mailContent;
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}