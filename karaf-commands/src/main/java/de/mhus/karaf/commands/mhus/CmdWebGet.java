/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.karaf.commands.mhus;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.lib.basics.RC;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.console.Console;
import de.mhus.lib.core.util.Base64;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "mhus", name = "wget", description = "Download a http resource")
@Service
public class CmdWebGet extends AbstractCmd {

    @Argument(
            index = 0,
            name = "url",
            required = true,
            description = "Url to download",
            multiValued = true)
    String[] urls;

    @Option(
            name = "-m",
            aliases = "--method",
            description = "Set the http method to use: GET POST PUT DELETE HEAD",
            required = false)
    String method = null;

    @Option(
            name = "-a",
            aliases = "--authenticate",
            description =
                    "set the basic auth credentials use <user>:<pass> or <user> and it will ask for the password",
            required = false)
    String auth = null;

    @Option(
            name = "-h",
            aliases = "--header",
            description = "add header values",
            required = false,
            multiValued = true)
    String[] headers = null;

    @Option(
            name = "-p",
            aliases = "--payload",
            description = "Set the payload content",
            required = false)
    String payload = null;

    @Option(
            name = "-f",
            description = "Set Form data content - do not use with payload",
            required = false,
            multiValued = true)
    String[] formData = null;

    @Option(
            name = "-o",
            aliases = "--output",
            description = "set the output file or - for stdout (default as return value)",
            required = false)
    String out = null;

    @Option(
            name = "-v",
            aliases = "--verbose",
            description = "verbose output to stdout",
            required = false)
    boolean verbose = false;

    @Option(
            name = "--no-follow",
            description = "set to deny follow on permanently moved",
            required = false)
    boolean noFollow = false;

    @Option(
            name = "--ignore-certificates",
            description = "Ignore SSL/TLS certificate host names",
            required = false)
    boolean ignoreCertificates = false;

    @Option(
            name = "--iterate",
            description = "Iterate the requests multiple times",
            required = false)
    int iterate = 1;

    @Option(
            name = "--iterateSleep",
            description = "Sleep time in seconds between iterations",
            required = false)
    int iterateSleep = 0;

    @Option(
            name = "--proxy",
            description = "Set proxy or - for no proxy, default is system proxy",
            required = false)
    String proxy = null;

    @Option(name = "--proxyAuth", description = "Set proxy authentication", required = false)
    String proxyAuth = null;

    @Override
    public Object execute2() throws Exception {

        HttpClientBuilder builder = HttpClientBuilder.create();
        if (verbose) {
            builder.addInterceptorFirst(
                    new HttpRequestInterceptor() {

                        @Override
                        public void process(HttpRequest arg0, HttpContext arg1)
                                throws HttpException, IOException {
                            System.out.println(">>> Request First: " + arg0);
                        }
                    });
            builder.addInterceptorFirst(
                    new HttpResponseInterceptor() {

                        @Override
                        public void process(HttpResponse arg0, HttpContext arg1)
                                throws HttpException, IOException {
                            System.out.println(">>> Response First: " + arg0);
                        }
                    });
            builder.addInterceptorLast(
                    new HttpRequestInterceptor() {

                        @Override
                        public void process(HttpRequest arg0, HttpContext arg1)
                                throws HttpException, IOException {
                            System.out.println("<<< Request Last: " + arg0);
                        }
                    });
            builder.addInterceptorLast(
                    new HttpResponseInterceptor() {

                        @Override
                        public void process(HttpResponse arg0, HttpContext context)
                                throws HttpException, IOException {

                            ManagedHttpClientConnection routedConnection =
                                    (ManagedHttpClientConnection)
                                            context.getAttribute(HttpCoreContext.HTTP_CONNECTION);
                            SSLSession sslSession = routedConnection.getSSLSession();
                            if (sslSession != null) {
                                Certificate[] peerCertificates = sslSession.getPeerCertificates();
                                if (peerCertificates != null) {
                                    for (Certificate certificate : peerCertificates) {
                                        X509Certificate real = (X509Certificate) certificate;
                                        System.out.println("=== Certificate");
                                        System.out.println("--- Type: " + real.getType());
                                        System.out.println(
                                                "--- Signing Algorithm: " + real.getSigAlgName());
                                        System.out.println(
                                                "--- IssuerDN Principal: "
                                                        + real.getIssuerX500Principal());
                                        System.out.println(
                                                "--- SubjectDN Principal: "
                                                        + real.getSubjectX500Principal());
                                        System.out.println(
                                                "--- Not After: "
                                                        + DateUtils.formatDate(
                                                                real.getNotAfter(), "dd-MM-yyyy"));
                                        System.out.println(
                                                "--- Not Before: "
                                                        + DateUtils.formatDate(
                                                                real.getNotBefore(), "dd-MM-yyyy"));
                                    }
                                } else {
                                    System.out.println("--- Certificate not found");
                                }
                            } else System.out.println("--- SSL session not found");
                            System.out.println("<<< Response Last: " + arg0);
                        }
                    });
        }
        if (ignoreCertificates)
            builder.setSSLHostnameVerifier(
                    new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

        if (auth != null) {
            if (!MString.isIndex(auth, ':')) {
                // read password
                Console console = M.l(Console.class);
                console.print("Password: ");
                console.flush();
                String pass = console.readPassword();
                auth = auth + ":" + pass;
            }
        }

        if (method != null) method = method.toLowerCase().trim();

        String proxyHost = null;
        int proxyPort = 1080;
        if (proxy != null) {
            if (proxy.equals("-")) builder.setProxy(null);
            else {
                proxyHost = proxy;
                if (MString.isIndex(proxyHost, ':')) {
                    proxyPort = MCast.toint(MString.afterLastIndex(proxyHost, ':'), proxyPort);
                    proxyHost = MString.beforeLastIndex(proxyHost, ':');
                }
                builder.setProxy(new HttpHost(proxyHost, proxyPort));
            }
        } else {
            builder.useSystemProperties();
            proxyHost = System.getProperty("http.proxy");
            if (MString.isIndex(proxyHost, ':')) {
                proxyPort = MCast.toint(MString.afterLastIndex(proxyHost, ':'), proxyPort);
                proxyHost = MString.beforeLastIndex(proxyHost, ':');
            }
        }

        if (proxyAuth != null) {
            String user = MString.beforeIndex(proxyAuth, ':');
            String pass = MString.afterIndex(proxyAuth, ':');
            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(user, pass);
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), creds);
            builder.setDefaultCredentialsProvider(credsProvider);
            ProxyAuthenticationStrategy proxyAuthStrategy = new ProxyAuthenticationStrategy();
            builder.setProxyAuthenticationStrategy(proxyAuthStrategy);
        }

        try (CloseableHttpClient client = builder.build()) {

            OutputStream os = null;
            if (out == null) os = new ByteArrayOutputStream();
            else if (out.equals("-")) os = System.out;
            else os = new FileOutputStream(out);

            for (int i = 0; i < iterate; i++) {

                if (i != 0 && iterateSleep > 0) Thread.sleep(iterateSleep * 1000);

                for (String url : urls) {
                    while (true) {
                        HttpUriRequest request = null;
                        if (method == null || method.equals("get")) request = new HttpGet(url);
                        else if (method.equals("post")) request = new HttpPost(url);
                        else if (method.equals("put")) request = new HttpPut(url);
                        else if (method.equals("delete")) request = new HttpDelete(url);
                        else if (method.equals("head")) request = new HttpHead(url);
                        else if (method.equals("options")) request = new HttpOptions(url);
                        else throw new MException(RC.USAGE, "http method {1} unknown", method);

                        if (headers != null)
                            for (String header : headers)
                                request.setHeader(
                                        MString.beforeIndex(header, ':'),
                                        MString.afterIndex(header, ':').trim());

                        if (auth != null)
                            request.setHeader("Authorization", "Basic " + Base64.encode(auth));

                        if (payload != null) {
                            if (!(request instanceof HttpEntityEnclosingRequest))
                                throw new MException(
                                        RC.USAGE,
                                        "Payload is only possible for requests with POST and PUT");
                            StringEntity pl = new StringEntity(payload);
                            if (verbose) System.out.println("--- Payload: " + pl);
                            ((HttpEntityEnclosingRequest) request).setEntity(pl);
                        } else if (formData != null) {
                            if (!(request instanceof HttpEntityEnclosingRequest))
                                throw new MException(
                                        RC.USAGE,
                                        "Form data is only possible for requests with POST and PUT");
                            List<NameValuePair> form = new ArrayList<>();
                            for (String data : formData)
                                form.add(
                                        new BasicNameValuePair(
                                                MString.beforeIndex(data, '='),
                                                MString.afterIndex(data, '=')));
                            UrlEncodedFormEntity entity =
                                    new UrlEncodedFormEntity(form, Consts.UTF_8);
                            ((HttpEntityEnclosingRequest) request).setEntity(entity);
                        }

                        CloseableHttpResponse result = client.execute(request);

                        StatusLine status = result.getStatusLine();

                        HttpEntity entity = result.getEntity();
                        if (entity != null) {
                            InputStream is = entity.getContent();
                            long size = MFile.copyFile(is, os);
                            if (size > 0) os.write('\n');
                        }
                        result.close();

                        if (status.getStatusCode() == 200) {
                            if (verbose) System.out.println("--- " + status);

                            os.flush();
                            break;
                        } else if (status.getStatusCode() == 401) {
                            Console console = M.l(Console.class);
                            console.print("Username: ");
                            String user = console.readLine();
                            if (user.length() == 0) break;
                            console.print("Password: ");
                            console.flush();
                            String pass = console.readPassword();
                            auth = user + ":" + pass;
                        } else if (status.getStatusCode() == 301) {
                            Header[] location = result.getHeaders("Location");
                            if (location == null || location.length != 1) {
                                System.out.println("Permanently moved failed");
                                break;
                            }
                            String l = location[1].getValue();
                            if (l.startsWith("/")) {
                                // relative to host name
                                int pos = url.indexOf('/', 8); // could be a problem
                                if (pos > 0) url = url.substring(0, pos) + l;
                                else url = url + l;
                                if (verbose) System.out.println("--- Moved to " + url);
                            } else
                                // full
                                url = l;
                        } else {
                            System.out.println("*** Error: " + status);
                            break;
                        }
                    }
                }
            }

            String ret = null;
            if (out == null) ret = new String(((ByteArrayOutputStream) os).toByteArray());
            else if (out.equals("-")) os.flush();
            else ((FileOutputStream) os).close();
            ;

            return ret;
        }
    }
}
