
In Karaf:

feature:install http
feature:install http-whiteboard
feature:install scr

# Install vaadin core
bundle:install -s mvn:de.mhus.osgi.ports/vaadin-shared-deps/1.0.5-SNAPSHOT
bundle:install -s mvn:com.vaadin/vaadin-shared/7.5.8
bundle:install -s mvn:com.vaadin/vaadin-themes/7.5.8
bundle:install -s mvn:org.jsoup/jsoup/1.7.3
bundle:install -s mvn:com.vaadin/vaadin-server/7.5.8
bundle:install -s mvn:com.vaadin/vaadin-client-compilerd/7.5.8

# Install vaadin bridge and karaf management tool
bundle:install -s 'wrap:mvn:org.apache.commons/commons-io/1.3.2/$Bundle-SymbolicName=commons-io&Bundle-Version=1.3.2&Export-Package=org.apache.commons.io;version="1.3.2",\!*'
bundle:install -s mvn:de.mhus.osgi/vaadinosgibridge/1.0.5-SNAPSHOT

bundle:install -s mvn:org.codehaus.jackson/jackson-core-asl/1.9.5
bundle:install -s mvn:org.codehaus.jackson/jackson-mapper-asl/1.9.5
bundle:install -s mvn:de.mhus.lib/mhu-lib-annotations/3.2.9-SNAPSHOT
bundle:install -s mvn:de.mhus.lib/mhu-lib-core/3.2.9-SNAPSHOT
bundle:install -s mvn:de.mhus.osgi/vaadinkarafbridge/1.0.5-SNAPSHOT

# Install sample
bundle:install -s mvn:de.mhus.osgi.tutorial/vaadin-sample/1.0.5-SNAPSHOT


In Browser:

http://localhost:8181/vaadinsample