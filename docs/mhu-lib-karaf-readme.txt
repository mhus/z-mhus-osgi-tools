====
    Copyright 2018 Mike Hummel

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

# Optional bouncycastle
install -s mvn:org.bouncycastle/bcprov-jdk15on/1.56

install -s mvn:org.codehaus.jackson/jackson-core-asl/1.9.5
install -s mvn:org.codehaus.jackson/jackson-mapper-asl/1.9.5
install -s mvn:javax.portlet/portlet-api/2.0

install -s mvn:de.mhus.lib/mhu-lib-annotations/3.2.9-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-core/3.2.9-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-persistence/3.2.9-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-logging/3.2.9-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-karaf/3.2.9-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-j2ee/3.2.9-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-jms/3.2.9-SNAPSHOT

bundle:watch mhu-lib-core
bundle:watch mhu-lib-annotations
bundle:watch mhu-lib-persistence
bundle:watch mhu-lib-logging
bundle:watch mhu-lib-karaf
bundle:watch mhu-lib-j2ee
bundle:watch mhu-lib-jms


<?xml version="1.0" encoding="UTF-8"?>
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0">
    <bean id="cmd" 
          class="de.mhus.lib.karaf.services.ScheduleGogo" 
          init-method="init" destroy-method="destroy">
      <property name="name" value="cmd_hello"/>
      <property name="interval" value="*/2 * * * *"/>
      <property name="command" value="log:log 'hello world!'"/>
      <property name="timerFactory" ref="TimerFactoryRef" />
    </bean>
    <reference
       id="TimerFactoryRef" 
       interface="de.mhus.lib.core.util.TimerFactory" />
    <service 
      interface="de.mhus.lib.karaf.services.SimpleServiceIfc" 
      ref="cmd">
        <service-properties>
            <entry key="osgi.jndi.service.name" value="cmd_hello"/>
        </service-properties>
    </service>
</blueprint>
