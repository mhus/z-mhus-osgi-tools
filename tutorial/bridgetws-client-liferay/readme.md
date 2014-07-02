JWS Liferay Client
=================

This client demonstrate how to use the JWS in a liferay client.

Installation
---------------

You need to install the mhu-liferay-theme included in mhu-lib and this portlet. Start a karaf
server and install the demo server as described in the server project.

If you have installed the portlet you can change the connect url in the preferences of the portlet. But
if you installed the karaf in localhost you don't need to touch the preferences.

Debugging
------------

Open the Website with the mhu-liferay-theme and the browser javascript console. Type

	setAjaxDebug(true);

to enable debugging of the ajax requests. The debug information will be send to the console.

Used Technologies
---------
- Use of JSTL Tag Lib
- Access Control
- i18n Liferay and Callback
- Ajax Callback Framework
- JWS Client
