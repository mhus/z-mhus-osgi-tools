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

xdb:api mo

xdb:create test TestPerson name=Max
xdb:create test TestPerson name=Sabine
xdb:create test TestPerson name=Mickey
xdb:create test TestPerson name=Donald

xdb:select test TestPerson
 
xdb:select test TestPerson '"$or":[{"name":"Max"},{"name":"Donald"}],"$order":"name"'


xdb:create test TestBook name=A meta.isbn=1234567
mongo:find local test TestBook
mongo:find local test TestBook "{'meta.isbn':'1234567'}"


mongo-ds.xml:

<blueprint default-activation="eager"
    xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:cm="http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.0.0">

<bean id="client" class="de.mhus.karaf.mongo.MongoConnection">
  <argument value="local"/>
  <argument value="localhost"/>
  <argument value="27017"/>
</bean>

<service interface="de.mhus.karaf.mongo.MongoDataSource" ref="client">
    <service-properties>
       <entry key="lookup.name" value="local"/>
    </service-properties>
</service>

</blueprint>


install -s mvn:org.mongodb/mongo-java-driver/3.6.0
install -s wrap:mvn:com.thoughtworks.proxytoys/proxytoys/1.0
install -s mvn:org.mongodb.morphia/morphia/1.3.2

install -s mvn:de.mhus.osgi/mhu-karaf-mongo/1.3.2-SNAPSHOT
install -s mvn:de.mhus.osgi/mhu-karaf-xdb/1.3.2-SNAPSHOT
install -s mvn:de.mhus.osgi.examples/mongo-manager/1.3.1-SNAPSHOT

