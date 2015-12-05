JWS Bridge
=============

The JWS Bridge is a simple and fast way to develop your Java Web Services in an OSGi environment. Install and start the bridge (jwsosgibridge)
to enable all dependend modules to define JWS services.

Implementation
-------------

To define a service create a provider for the JavaWebService interface. You can work faster if you also extend the AbstractJavaWebService.
It's going to define the class also as web service implementation. Have a look into the tutorial 'bridgews-server' for more details.

	@WebService(endpointInterface = "de.mhus.test.ws.ws_model.WSService")
	@Component(name="HohohoHeHeBridge",immediate=true,provide=JavaWebService.class)
	public class BridgeWsServer extends AbstractJavaWebService implements WSService {
	
	[implement interface]
	
Define the Web-Service in the interface:

	@WebService
	@SOAPBinding(style = Style.RPC)
	public interface WSService {
	
		@WebMethod
		void addEntity(WSEntity entity);

	[...]


Karaf Support
-------------

To allow command support the bundle jwskarafbridge will append the needed commands to the gogo shell.
The command space is 'jws' and will provide the following commands

	list: list all known jws services
	stop: stop a service
	publish: pubish a service (if stopped)
	remove: remove the service from list

Installation
-------------

**Maybe update version**

	install -s mvn:de.mhus.osgi/jwsosgibridge/1.3.0-SNAPSHOT
	install -s mvn:de.mhus.osgi/jwskarafbridge/1.3.0-SNAPSHOT

