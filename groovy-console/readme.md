Groovy Console
=============

The bundle enables a vaadin web console in /system/groovy (http://localhost:8181/system/groovy). To use the console you need
the role 'admin'. In the default karaf installation use the user 'karaf' with 'karaf'. In default felix use 'admin' and 'admin' as
credentials.

The console depends on mhu-lib and vaadinosgibridge.

Installation
-------------

**Maybe change version strings**

First install the vaadinbridge as described in the project.

Install groovy

	install -s mvn:org.codehaus.groovy/groovy-all/1.8.2

Install the mhu-lib
	
	install -s mvn:de.mhus.lib/mhu-lib-annotations/3.2.5
	install -s mvn:de.mhus.lib/mhu-lib-core/3.2.5
	install -s mvn:de.mhus.lib/mhu-lib-logging/3.2.5
	install -s mvn:de.mhus.lib/mhu-lib-vaadin/3.2.5
	install -s mvn:de.mhus.lib/mhu-lib-karaf/3.2.5

Install the groovy console

	install -s mvn:de.mhus.osgi/groovy-console/1.0.2
 	
Usage
-------------


