
feature:repo-add activemq
feature:install activemq-client
  
install -s mvn:de.mhus.osgi.jms/jms-commands/1.3.0-SNAPSHOT
bundle:watch jms-commands

---

feature:install scr
feature:install jdbc
feature:install openjpa

install -s mvn:org.codehaus.jackson/jackson-core-asl/1.9.5
install -s mvn:org.codehaus.jackson/jackson-mapper-asl/1.9.5
install -s mvn:javax.portlet/portlet-api/2.0

install -s mvn:de.mhus.lib/mhu-lib-annotations/3.3.0-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-core/3.3.0-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-persistence/3.3.0-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-logging/3.3.0-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-karaf/3.3.0-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-j2ee/3.3.0-SNAPSHOT
  
  
---

jms:send tcp://localhost:61613 event hello 100
jms:sendfile tcp://localhost:61613 event /path/to/file/or/dir  
