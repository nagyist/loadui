// 
// Copyright 2013 SmartBear Software
// 
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
// 
// http://ec.europa.eu/idabc/eupl
// 
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
// 

/**
 * Sends an HTTP request
 *
 * @id com.eviware.WebRunner
 * @help http://www.loadui.org/Runners/web-page-runner-component.html
 * @name HTTP Runner
 * @category runners
 */

import org.apache.http.*
import org.apache.http.client.*
import org.apache.http.auth.*
import org.apache.http.conn.params.*
import org.apache.http.conn.scheme.*
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.util.EntityUtils
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import com.eviware.loadui.api.events.ActionEvent
import com.eviware.loadui.api.events.PropertyEvent
import com.eviware.loadui.api.model.CanvasItem
import com.eviware.loadui.impl.component.categories.RunnerBase.SampleCancelledException
import com.eviware.loadui.impl.component.ActivityStrategies
import com.eviware.loadui.util.ReleasableUtils

import javax.net.ssl.*
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.ssl.X509HostnameVerifier

//SSL support, trust all certificates and hostnames.
class NaiveTrustManager implements X509TrustManager {
    void checkClientTrusted(X509Certificate[] cert, String authType) throws CertificateException {}

    void checkServerTrusted(X509Certificate[] cert, String authType) throws CertificateException {}

    X509Certificate[] getAcceptedIssuers() { null }
}

class AllowAllHostNamesVerifier implements X509HostnameVerifier {
    void verify(String host, SSLSocket ssl) throws IOException {}

    void verify(String host, X509Certificate cert) throws SSLException {}

    void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {}

    boolean verify(String hostname, SSLSession session) {}
}

def sslContext = SSLContext.getInstance("SSL")
TrustManager[] tms = [new NaiveTrustManager()]
sslContext.init(new KeyManager[0], tms, new SecureRandom())

def sslSocketFactory = new SSLSocketFactory(sslContext, new AllowAllHostNamesVerifier())

def sr = new SchemeRegistry()
sr.register(new Scheme("http", PlainSocketFactory.socketFactory, 80))
sr.register(new Scheme("https", sslSocketFactory, 443))

def cm = new ThreadSafeClientConnManager(sr)
cm.maxTotal = 50000
cm.defaultMaxPerRoute = 50000

//Properties
url = createProperty('url', String) {->
    validateUrl()
}
outputBody = createProperty('outputBody', Boolean, false)

errorCodeList = createProperty('errorCodeList', String)

proxyHost = createProperty('proxyHost', String)
proxyPort = createProperty('proxyPort', Long)
proxyUsername = createProperty('proxyUsername', String)
proxyPassword = createProperty('_proxyPassword', String)
authUsername = createProperty('_authUsername', String)
authPassword = createProperty('_authPassword', String)

def latencyVariable = addStatisticVariable( "Latency", '', "SAMPLE" )

http = new DefaultHttpClient(cm)

inlineUrlAuthUsername = null
inlineUrlAuthPassword = null

def runningSamples = ([] as Set).asSynchronized()
runAction = null

def dummyUrl = "http://GoSpamYourself.com"
def validUrl = ""
validateUrl = {
    def cleanUrl = url.value
    if (!(cleanUrl ==~ "https?://.*")) {
        cleanUrl = 'http://' + cleanUrl
    }

    if (cleanUrl =~ /https?:\/\/(www\.)?(eviware\.com|(soapui|loadui)\.org)(\/.*)?/) {
        url.value = dummyUrl
        setInvalid(true)
        return
    }

    // extract possible username and password from username:password@domain syntax
    matcher = cleanUrl?.replace("http://", "") =~ /([^:]+):([^@]+)@(.+)/
    if (matcher) {
        inlineUrlAuthUsername = matcher[0][1]
        inlineUrlAuthPassword = matcher[0][2]
    } else {
        inlineUrlAuthUsername = inlineUrlAuthPassword = null
    }
    updateAuth()

    try {
        new URI(cleanUrl)
        setInvalid(!url.value || url.value == dummyUrl)
    } catch (e) {
        setInvalid(true)
    }

    runAction?.enabled = !isInvalid()
    validUrl = cleanUrl
}

updateProxy = {
    if (proxyHost.value?.trim() && proxyPort.value) {
        // recreate the client because otherwise the credentials do not seem to be updated
        http = new DefaultHttpClient(cm)

        HttpHost hcProxyHost = new HttpHost(proxyHost.value, (int) proxyPort.value, "http")
        http.params.setParameter(ConnRoutePNames.DEFAULT_PROXY, hcProxyHost)

        if (proxyUsername.value?.trim() && proxyPassword.value) {
            http.credentialsProvider.setCredentials(
                    new AuthScope(proxyHost.value, (int) proxyPort.value),
                    new UsernamePasswordCredentials(proxyUsername.value, proxyPassword.value)
            )
        } else {
            http.credentialsProvider.clear()
        }
    } else {
        http.params.setParameter(ConnRoutePNames.DEFAULT_PROXY, null)
    }
}

updateAuth = {
    def username = null
    def password = null
    if (inlineUrlAuthUsername && inlineUrlAuthPassword) {
        username = inlineUrlAuthUsername
        password = inlineUrlAuthPassword
    } else if (authUsername.value?.trim() && authPassword.value?.trim()) {
        username = authUsername.value
        password = authPassword.value
    }

    if (username && password) {
        // recreate the client because otherwise the credentials do not seem to be updated
        http = new DefaultHttpClient(cm)

        http.credentialsProvider.setCredentials(
                new AuthScope(AuthScope.ANY),
                new UsernamePasswordCredentials(username, password)
        )
    }
}

validateUrl()
updateProxy()

requestResetValue = 0
sampleResetValue = 0
discardResetValue = 0
failedResetValue = 0

sample = { message, sampleId ->

    def uri = message['url'] ?: validUrl
    if (uri) {
        def get = new HttpGet(uri)
        message['ID'] = uri

        runningSamples.add(get)
        try {
            def response = http.execute(get)
            message['Status'] = true
            message['URI'] = uri
            message['HttpStatus'] = response.statusLine.statusCode

            if (errorCodeList.value) {
                def assertionCodes = errorCodeList.value.split(',')

                for (code in assertionCodes) {
                    if (code.trim() == response.statusLine.statusCode.toString()) {
                        failedRequestCounter.increment()
                        failureCounter.increment()
                        break
                    }
                }
            }

            if (response.entity != null) {
                int contentLength = response.entity.contentLength
                message['Bytes'] = contentLength

                determineLatency(response.entity.content, sampleId)

                if (outputBody.value)
                    message['Response'] = EntityUtils.toString(response.entity)

                if (contentLength < 0) {
                    if (outputBody.value)
                        message['Bytes'] = message['Response'].length()
                    else
                        message['Bytes'] = EntityUtils.toString(response.entity).length()
                }

                response.entity.consumeContent()

                if (!runningSamples.remove(get)) {
                    throw new SampleCancelledException()
                }

                return message
            }
        } catch (e) {
            if (e instanceof SampleCancelledException)
                throw e

            if (e instanceof IOException)
                log.warn("IOException: {}: ", e.message)
            else
                log.error("Exception:", e)

            get.abort()

            if (!runningSamples.remove(get)) {
                throw new SampleCancelledException()
            }

            message['Status'] = false
            failedRequestCounter.increment()
            failureCounter.increment()

            return message
        }
    } else {
        throw new SampleCancelledException()
    }

}

def firstByte = new byte[1]
determineLatency = { content, startTime ->
    content.read(firstByte)
    latencyVariable.update(System.currentTimeMillis(), (System.nanoTime() - startTime)/1000000)
}

onCancel = {
    def numberOfRunning = 0
    synchronized (runningSamples) {
        def methods = runningSamples.toArray()
        numberOfRunning = methods.size()
        runningSamples.clear()
        methods.each { if (!it.aborted) it.abort() }
    }

    return numberOfRunning
}

onAction("RESET") {
    requestResetValue = 0
    sampleResetValue = 0
    discardResetValue = 0
    failedResetValue = 0
}

addEventListener(PropertyEvent) { event ->
    if (event.event == PropertyEvent.Event.VALUE) {
        if (event.property in [proxyHost, proxyPort, proxyUsername, proxyPassword, authUsername, authPassword]) {
            http.credentialsProvider.clear()
            updateProxy()
            updateAuth()
        }
    }
}

//Layout
layout {

    box(layout: 'wrap 2, ins 0') {
        property(property: url, label: 'Web Page Address', constraints: 'w 300!, spanx 2', style: '-fx-font-size: 17pt')
        action(label: 'Open in Browser', constraints: 'spanx 2', action: {
            com.eviware.loadui.ui.fx.util.UIUtils.openInExternalBrowser( validUrl )
        })
        runAction = action(label: 'Run Once', action: { triggerAction('SAMPLE') })
        action(label: 'Abort Running Pages', action: { triggerAction('CANCEL') })
    }

    separator(vertical: true)
    box(layout: 'wrap, ins 0') {
        box(widget: 'display', layout: 'wrap 3, align right') {
            node(label: 'Requests', content: { requestCounter.get() - requestResetValue }, constraints: 'w 50!')
            node(label: 'Running', content: { currentlyRunning }, constraints: 'w 50!')
            node(label: 'Completed', content: { sampleCounter.get() - sampleResetValue }, constraints: 'w 60!')
            node(label: 'Queued', content: { queueSize }, constraints: 'w 50!')
            node(label: 'Discarded', content: { discardCounter.get() - discardResetValue }, constraints: 'w 50!')
            node(label: 'Failed', content: { failureCounter.get() - failedResetValue }, constraints: 'w 60!')
        }
        action(label: 'Reset', action: {
            requestResetValue = requestCounter.get()
            sampleResetValue = sampleCounter.get()
            discardResetValue = discardCounter.get()
            failedResetValue = failureCounter.get()
            triggerAction('CANCEL')
        }, constraints: 'align right')
    }
}

//Compact Layout
compactLayout {
    box(widget: 'display', layout: 'wrap 3, align right') {
        node(label: 'Requests', content: { requestCounter.get() - requestResetValue }, constraints: 'w 50!')
        node(label: 'Running', content: { currentlyRunning }, constraints: 'w 50!')
        node(label: 'Completed', content: { sampleCounter.get() - sampleResetValue }, constraints: 'w 60!')
        node(label: 'Queued', content: { queueSize }, constraints: 'w 50!')
        node(label: 'Discarded', content: { discardCounter.get() - discardResetValue }, constraints: 'w 50!')
        node(label: 'Failed', content: { failureCounter.get() - failedResetValue }, constraints: 'w 60!')
    }
}

settings(label: "Basic") {
    property(property: outputBody, label: 'Output Response Body')
    //property( property: propagateSession, label: 'Propagate Session' )
    property(property: concurrentSamples, label: 'Max Concurrent Requests')
    property(property: maxQueueSize, label: 'Max Queue')
    property(property: errorCodeList, label: 'Error Codes that Count as Failures', constraints: 'w 200!')
    property(property: countDiscarded, label: 'Count Discarded Requests as Failed')

}

settings(label: "Authentication") {
    property(property: authUsername, label: 'Username')
    property(property: authPassword, widget: 'password', label: 'Password')
}

settings(label: "Proxy") {
    property(property: proxyHost, label: 'Proxy Host')
    property(property: proxyPort, label: 'Proxy Port')
    property(property: proxyUsername, label: 'Proxy Username')
    property(property: proxyPassword, widget: 'password', label: 'Proxy Password')
}
