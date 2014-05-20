

install -s mvn:de.mhus.osgi/jwsosgibridge/1.0.0-SNAPSHOT
install -s mvn:de.mhus.osgi/jwskarafbridge/1.0.0-SNAPSHOT

bundle:watch jwsosgibridge
bundle:watch jwskarafbridge