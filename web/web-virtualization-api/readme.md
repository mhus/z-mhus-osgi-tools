feature:install http
feature:install http-whiteboard
feature:install scr
feature:install jdbc
feature:install openjpa

install -s mvn:org.codehaus.jackson/jackson-core-asl/1.9.5
install -s mvn:org.codehaus.jackson/jackson-mapper-asl/1.9.5
install -s mvn:de.mhus.lib/mhu-lib-annotations/3.2.7
install -s mvn:de.mhus.lib/mhu-lib-core/3.2.7
install -s mvn:de.mhus.lib/mhu-lib-persistence/3.2.7
install -s mvn:de.mhus.lib/mhu-lib-logging/3.2.7
install -s mvn:de.mhus.lib/mhu-lib-karaf/3.2.7

install -s mvn:de.mhus.osgi/commands/1.0.4-SNAPSHOT

uninstall -f org.ops4j.pax.web.pax-web-jetty

install -s mvn:de.mhus.osgi.ports/mhus-pax-web-jetty/1.0.4-SNAPSHOT
install -s mvn:de.mhus.osgi.web/web-virtualization-api/1.0.4-SNAPSHOT
install -s mvn:de.mhus.osgi.web/web-virtualization-service/1.0.4-SNAPSHOT
install -s mvn:de.mhus.osgi.web/web-virtualization-impl/1.0.4-SNAPSHOT
install -s mvn:de.mhus.osgi.web/web-application-jsp/1.0.4-SNAPSHOT



bundle:watch mhus-pax-web-jetty
bundle:watch web-virtualization-api
bundle:watch web-virtualization-service
bundle:watch web-virtualization-impl
bundle:watch web-application-jsp

feature:repo-add cxf 2.7.9
feature:install cxf
