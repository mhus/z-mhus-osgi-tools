

### Start jms

```
docker run --name='karaf-jms' -d \
 -e 'ACTIVEMQ_CONFIG_NAME=amqp-srv1' \
 -e 'ACTIVEMQ_CONFIG_DEFAULTACCOUNT=false' \
 -e 'ACTIVEMQ_ADMIN_LOGIN=admin' -e 'ACTIVEMQ_ADMIN_PASSWORD=nein' \
 -e 'ACTIVEMQ_CONFIG_MINMEMORY=1024' -e  'ACTIVEMQ_CONFIG_MAXMEMORY=4096' \
 -e 'ACTIVEMQ_CONFIG_SCHEDULERENABLED=true' \
 webcenter/activemq:5.14.3
```

### Start karaf 1:

```
docker run -it \
 -v ~/.m2:/home/user/.m2\
 --link karaf-jms:jmsserver\
 -p 15006:5005\
 -p 18182:8181\
 --name karaf1\
 mhus/apache-karaf:4.2.6_04 debug
```

### Start karaf 2:

```
docker run -it \
 -v ~/.m2:/home/user/.m2\
 --link karaf-jms:jmsserver\
 -p 15007:5005\
 -p 18183:8181\
 --name karaf2\
 mhus/apache-karaf:4.2.6_04 debug
```

### Install

And install software in both environments

```
feature:repo-add activemq
feature:repo-add mvn:org.apache.shiro/shiro-features/1.5.1/xml/features
feature:repo-add mvn:de.mhus.osgi/karaf-features/7.0.0-SNAPSHOT/xml/features
feature:install mhu-micro-jms mhu-dev

mhus:dev -y cp jms ENV_JMS_SOP_USER=admin ENV_JMS_SOP_PASS=nein ENV_JMS_SERVER=jmsserver:61616

bundle:persistentwatch add .*

```

Maybe you need to wait until the jms discovery is scheduled or you start it manually

```
micro require
```

And try a ping - login as admin before:

```
access admin
micro ping
```
