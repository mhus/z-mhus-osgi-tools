
feature:install http-whiteboard
install -s mvn:de.mhus.osgi/mhu-osgi-root-servlet/1.3.0-SNAPSHOT


etc/rootservlet.properties

default.redirect=/home
rule0=/console
rule0.redirect=/system/console
rule1=.*/favicon.ico
rule1.error=404
rule1.errormsg=favicon not supported

