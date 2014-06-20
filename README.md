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

**Maybe change version**

Append a version property

	<properties>
		<mhus-osgi-tools.version>1.0.2</mhus-osgi-tools.version>
	</properties>

Follow the instructions in the sections jwsbridge and vaadinbridge.


