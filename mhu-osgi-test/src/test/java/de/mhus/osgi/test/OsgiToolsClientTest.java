/*
 *    Copyright 2015 Achim Nierbeck
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package de.mhus.osgi.test;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiToolsClientTest extends TestBase{
	

	
//	@Test
//	public void shouldHaveBundleContext() {
//		assertNotNull(bc);
//		System.out.println(bc);
//		System.out.println("************** shouldHaveBundleContext");
//	}

	@Test
	public void loadFeature() throws Exception {
		System.out.println("************** loadFeature");
		//Thread.sleep(10000);
//		ServiceReference<HeartbeatAdmin> hbref = bc.getServiceReference(HeartbeatAdmin.class);
//		HeartbeatAdmin hbAdmin = bc.getService(hbref);
//		System.out.println(hbAdmin);
		
        byteArrayOutputStream.flush();
        byteArrayOutputStream.reset();

        FutureTask<String> commandFuture = new FutureTask<String>(new Callable<String>() {
            @Override
			public String call() {
				try {
					System.out.println("************** installFeature");
					featuresService.installFeature("mhu-osgi-test");
					
					System.out.println("************** ll");
                    session.execute("ll");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                printStream.flush();
                errStream.flush();
                return byteArrayOutputStream.toString();
            }
        });

		executor.submit(commandFuture);
        String response = commandFuture.get(10000L, TimeUnit.MILLISECONDS);
        System.out.println(">>>");
        System.out.println(response);
        System.out.println("<<<");

	}

//	@Test
//	public void featuresShouldBeInstalled() throws Exception {
//		assertThat(featuresService.isInstalled(featuresService
//				.getFeature("Karaf-Cassandra-Embedded")), is(true));
//		assertThat(featuresService.isInstalled(featuresService
//				.getFeature("Karaf-Cassandra-Client")), is(true));
//		assertThat(featuresService.isInstalled(featuresService
//				.getFeature("Karaf-Cassandra-Shell")), is(true));
//	}
//	
//	@Test
//	public void cassandraConnect() throws Exception {
//
//		assertThat(executeCommand("cassandra:connect -p 9142 localhost"), containsString("Connected to cluster:"));
//		
//		executeCommand("cassandra:disconnect");
//	}
//
//	
//	@Test
//	public void cassandraDisconnect() throws Exception {
//		executeCommand("cassandra:connect -p 9142 localhost");
//		assertThat(executeCommand("cassandra:disconnect"), containsString("disconnected"));
//	}
//	
//	@Test
//	public void cassandraIsConnected() throws Exception {
//		executeCommand("cassandra:connect -p 9142 localhost");
//		assertThat(executeCommand("cassandra:isConnected"), containsString("true"));
//		executeCommand("cassandra:disconnect");
//	}
//	
//	@Test
//	public void cassandraCqlArgument() throws Exception {
//		executeCommand("cassandra:connect -p 9142 localhost");
//		assertThat(executeCommand("cassandra:cql \"SELECT keyspace_name FROM system_schema.keyspaces;\""), containsString("keyspace_name"));
//		executeCommand("cassandra:disconnect");
//	}
//	
//	@Test
//	public void cassandraCqlOption() throws Exception {
//		executeCommand("cassandra:connect -p 9142 localhost");
//		assertThat(executeCommand("cassandra:cql -f file:"+new File("../../../../src/test/resources/test.cql").getAbsolutePath()), containsString("keyspace_name"));
//		executeCommand("cassandra:disconnect");
//	}
	
}
