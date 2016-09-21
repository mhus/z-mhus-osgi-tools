

bundle:install -s mvn:javax.portlet/portlet-api/2.0


cp .../target/HelloPortlet.war deploy/



bundle:install -s webbundle:http://tomcat.apache.org/tomcat-5.5-doc/appdev/sample/sample.war?Bundle-SymbolicName=tomcat-sample&Web-ContextPath=/sample
