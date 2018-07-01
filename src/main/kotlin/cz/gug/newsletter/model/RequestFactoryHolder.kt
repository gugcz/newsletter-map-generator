package cz.gug.newsletter.model

import com.google.api.client.extensions.appengine.http.UrlFetchTransport
import com.google.api.client.http.HttpRequestFactory

object RequestFactoryHolder {
    val requestFactory: HttpRequestFactory = UrlFetchTransport.Builder().doNotValidateCertificate().build().createRequestFactory()
}