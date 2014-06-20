VAADIN Bridge
=============

The vaadin bridge provide a bundle which is watching for vaadin resources. The bundle will automatically
add a dependency to the http bundle and make the vaadin resouces accessible to the web.

The UI vaadin (http://www.vadin.com) allows you to develop web applications. The framework expects under the
path /VAADIN resources like themes and widgetsets. The vaadin libraries are already defined as OSGi bundles but the do
not provide the /VAADIN servlet in the OSGi environment.

To provide the VAADIN servlet the vaadinbridge supports such a servlet. It automatically scans all bundles for the resource
folder VAADIN and provide the content via the servlet. This will also include the basic VAADIN resources from vaadin-server, vaadin-client ...

To use the bridge you don't need to append dependencies to the bridge. The bridge will automatically recognize of the VAADIN folder. Develop
you application as vaadin maven application as described in the vaadin documentation. For details have a look in the vaadin-sample.

Karaf support
-------------

If you install also the karafbridge, it will provide commands to control the osgi bridge. The commands are in the space 'vaadin':

	resourcelist: List all known and mapped resources
	resourceadd: Create or overwrite a resource map
	resourceremove: Remove a resource mapping
	debug: Enable/disable debuging
	watchrefresh: Reload the mapping from all bundles
	watchenabled: Enable/disable automatic bundle watch (enabled by default)


Instalation
-------------

	install -s mvn:de.mhus.osgi/vaadinosgibridge/1.0.2
	install -s mvn:de.mhus.osgi/vaadinkarafbridge/1.0.2
	
