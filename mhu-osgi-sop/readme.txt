
install -s mvn:de.mhus.osgi/mhu-osgi-sop-api/1.3.1-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-osgi-sop-impl/1.3.1-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-osgi-sop-jms/1.3.1-SNAPSHOT

install -s mvn:de.mhus.ports/bonita-client/1.3.1-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-osgi-bpm-bonita/1.3.1-SNAPSHOT


bundle:persistentwatch add mhu-osgi-bpm-bonita
