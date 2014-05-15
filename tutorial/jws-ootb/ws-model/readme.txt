feature:repo-add cxf 2.7.9
feature:install cxf
feature:install scr

install -s mvn:de.mhus.test.ws/ws-model/0.0.1-SNAPSHOT
install -s mvn:de.mhus.test.ws/ws-server/0.0.1-SNAPSHOT

bundle:watch ws-model
bundle:watch ws-server
