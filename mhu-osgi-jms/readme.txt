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

feature:repo-add activemq
feature:install activemq-client
  
install -s mvn:de.mhus.osgi.jms/jms-commands/1.3.0-SNAPSHOT
bundle:watch jms-commands

---

feature:install scr
feature:install jdbc
feature:install openjpa

install -s mvn:org.codehaus.jackson/jackson-core-asl/1.9.5
install -s mvn:org.codehaus.jackson/jackson-mapper-asl/1.9.5
install -s mvn:javax.portlet/portlet-api/2.0

install -s mvn:de.mhus.lib/mhu-lib-annotations/3.3.0-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-core/3.3.0-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-persistence/3.3.0-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-logging/3.3.0-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-karaf/3.3.0-SNAPSHOT
install -s mvn:de.mhus.lib/mhu-lib-j2ee/3.3.0-SNAPSHOT
  
  
---

jms:send tcp://localhost:61613 event hello 100
jms:sendfile tcp://localhost:61613 event /path/to/file/or/dir  



---


Sample client-config.wsdd file

<?xml version="1.0" encoding="UTF-8"?>
<deployment name="defaultClientConfig"
   xmlns="http://xml.apache.org/axis/wsdd/"
   xmlns:java="http://xml.apache.org/axis/wsdd/providers/java">

   <handler name="log"
      type="java:de.mhus.osgi.jms.util.AxisLog2LogHandler">
      <parameter name="LogHandler.logLevel" value="info" />
   </handler>

   <globalConfiguration>
      <parameter name="disablePrettyXML" value="false" />
      <requestFlow>
         <handler type="log" />
      </requestFlow>
      <responseFlow>
         <handler type="log" />
      </responseFlow>
   </globalConfiguration>

   <transport name="http"
      pivot="java:org.apache.axis.transport.http.HTTPSender" />
   <transport name="local"
      pivot="java:org.apache.axis.transport.local.LocalSender" />
   <transport name="java"
      pivot="java:org.apache.axis.transport.java.JavaSender" />
</deployment>

