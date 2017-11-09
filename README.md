mhus-osgi-tools
=============

A collection of osgi and karaf tools. The tools are created make work with osgi more efficient. Most time prepare osgi with service consumers, so other bundles can provide functionality which is atomatically integrated into the osgi engine.

vaadinbridge
-------------

Provide in OSGi the servlet /VAADIN to map vaadin resources (like widgetsets and themes) out in the world. The bridge will watch bundles and automatically publish/destroy VAADIN resources.

jwsbridge
-------------

Provide an OSGi service handling jws services. Implement and install the JavaWebService interface and the web service will automatically become an entry point.

Build from source
-------------

	git clone https://github.com/mhus/mhus-osgi-tools.git mhus-osgi-tools
	cd mhus-osgi-tools
	mvn install

Use with maven
-------------

**Change version**

Append a version property

	<properties>
		<mhus-osgi-tools.version>1.0.2</mhus-osgi-tools.version>
	</properties>

Follow the instructions in the sections jwsbridge and vaadinbridge.

Install Into Karaf
------

Use Feature

Maybe need cglib provider installed:
install -s mvn:de.mhus.ports/mhus-cglib/1.3.1-SNAPSHOT

feature:repo-add activemq 5.12.1
feature:repo-add cxf 3.1.5
feature:repo-add mvn:de.mhus.osgi/mhu-karaf-feature/1.3.2-SNAPSHOT/xml/features


feature:install mhu-osgi-base
or
feature:install mhu-osgi-jms
or
feature:install mhu-osgi-full


Manually:
------

feature:repo-add activemq 5.12.1
feature:repo-add cxf 2.7.11

feature:install jdbc
feature:install openjpa
feature:install scr

feature:install activemq-broker

#  http://localhost:8185/activemqweb


install -s mvn:mysql/mysql-connector-java/5.1.18

install -s mvn:org.codehaus.jackson/jackson-core-asl/1.9.5
install -s mvn:org.codehaus.jackson/jackson-mapper-asl/1.9.5

install -s mvn:de.mhus.lib/mhu-lib-annotations/3.3.2-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-core/3.3.2-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-jms/3.3.2-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-persistence/3.3.2-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-karaf/3.3.2-SNAPSHOT

install -s mvn:de.mhus.osgi/mhu-osgi-jms/1.3.2-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-osgi-commands/1.3.2-SNAPSHOT

install -s mvn:org.apache.httpcomponents/httpcore-osgi/4.2.1
install -s mvn:org.apache.httpcomponents/httpclient-osgi/4.2.1

install -s mvn:de.mhus.osgi/mhu-osgi-mailosgi/1.3.2-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-osgi-mailkaraf/1.3.2-SNAPSHOT

---

bundle:persistentwatch add mhu-lib-annotations
bundle:persistentwatch add mhu-lib-persistence
bundle:persistentwatch add mhu-lib-karaf
bundle:persistentwatch add mhu-lib-jms
bundle:persistentwatch add mhu-lib-vaadin
bundle:persistentwatch add mhu-lib-forms
bundle:persistentwatch add mhu-lib-core
bundle:persistentwatch add mhu-osgi-jms
bundle:persistentwatch add mhu-osgi-mailosgi
bundle:persistentwatch add mhu-osgi-mailkaraf
bundle:persistentwatch add mhu-osgi-commands
bundle:persistentwatch add mhu-osgi-sop-api
bundle:persistentwatch add mhu-osgi-sop-impl

bundle:list|grep --color never mhu-osgi|cut -d '\|' -f 4|run -c "for i in read *;bundle:persistentwatch add \$i\ ;done"



