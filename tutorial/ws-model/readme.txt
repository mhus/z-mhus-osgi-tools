feature:repo-add cxf 2.7.9
feature:install cxf
feature:install scr

install -s mvn:org.codehaus.jackson/jackson-core-asl/1.9.5
install -s mvn:org.codehaus.jackson/jackson-mapper-asl/1.9.5
install -s mvn:de.mhus.lib/mhu-lib-annotations/3.2.6-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-core/3.2.6-SNAPSHOT

install -s mvn:de.mhus.osgi/jwsosgibridge/1.0.5-SNAPSHOT
install -s mvn:de.mhus.osgi/jwskarafbridge/1.0.5-SNAPSHOT

install -s mvn:de.mhus.osgi.tutorial/ws-model/1.0.5-SNAPSHOT
install -s mvn:de.mhus.osgi.tutorial/ws-server/1.0.5-SNAPSHOT

bundle:watch ws-model
bundle:watch ws-server


mvn org.apache.maven.plugins:maven-dependency-plugin:2.1:get -DrepoUrl=http://nexus.doexecute.com/nexus/content/repositories/snapshots/ -Dartifact=de.mhus.test.ws:ws-client:0.0.1-SNAPSHOT

