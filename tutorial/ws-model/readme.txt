feature:repo-add cxf 2.7.9
feature:install cxf
feature:install scr

install -s mvn:de.mhus.osgi/jwsosgibridge/1.0.0-SNAPSHOT
install -s mvn:de.mhus.osgi/jwskarafbridge/1.0.0-SNAPSHOT

install -s mvn:de.mhus.test.ws/ws-model/0.0.1-SNAPSHOT
install -s mvn:de.mhus.test.ws/ws-server/0.0.1-SNAPSHOT

bundle:watch ws-model
bundle:watch ws-server


mvn org.apache.maven.plugins:maven-dependency-plugin:2.1:get -DrepoUrl=http://nexus.doexecute.com/nexus/content/repositories/snapshots/ -Dartifact=de.mhus.test.ws:ws-client:0.0.1-SNAPSHOT

