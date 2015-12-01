== Description ==

The servlet will call another servlet in the OSGi engine, collect the content and perform regex replacements before
returning it to the browser. It's a content rewrite engine.

== Installation into karaf ==

(Version could be different)

feature:install http-whiteboard
install -s mvn:de.mhus.osgi/rewrite-servlet/1.0.5-SNAPSHOT

bundle:watch rewrite-servlet

== Usage ==

Create the file etc/rewriteservlet.properties in the karaf home directory.

You can configure different rewrite configurations using the syntax:

[name].servlet=[servlet identifier]
[name][rule id].path=[path regex]
[name][rule id].contentType=[type regex]
[name][rule id].rule=[rule regex]
[name][rule id].replace=[replace]

The 'name' must be unique and is used in the url

http://localhost:8181/rewrite/[name]

again. It must be in a URL compatible format.

* name: The name of the configuration
* servlet identifier: The servlet alias (http:list) starting with the slash or the name of the bundle (problem with multiple servlet for a bundle)
* rule id: Increasing number (integer) starting at zero to to identify the rule number
* path regex: Regex matching the affected path to use this rule, default is .*
* type regex: Regex matching the affected content type, default is .*
* rule regex: Regex to search and replace the content
* replace: The replacement for the matching rule regex

== Sample ==

cxf.servlet=/cxf
cxf0.path=.*
cxf0.contentType=.*
cxf0.rule=http://
cxf0.replace=https://
cxf1.path=.*
cxf1.rule=localhost:8181
cxf1.replace=my-server.domain

Call the rewrite with:

http://localhost:8181/rewrite/cxf
