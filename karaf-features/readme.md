## Install

```
feature:repo-add mvn:org.apache.shiro/shiro-features/1.5.1/xml/features
feature:repo-add mvn:de.mhus.osgi/karaf-features/7.0.0-SNAPSHOT/xml/features
feature:install mhu-base
```

### JMS

```
feature:repo-add activemq
feature:install mhu-jms
```

### Vaadin

```
feature:install mhu-vaadin
```

Vaadin Desktop (Theme and UI)

access via /ui

```
feature:install mhu-vaadin
```

### Rest

```
feature:install mhu-rest
```

### Microservice framework

```
feature:install mhu-micro
```

JMS Provider

```
feature:repo-add activemq
feature:install mhu-micro-jms
```

### Transform Services

```
feature:install mhu-transform
```


### Crypt Services

```
feature:install mhu-crypt
```


