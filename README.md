# Camel Saga Quickstart

This quickstarts demonstrates how to use the new saga feature of Camel 2.21.

The `camel-saga-app` module has the following route:
```
from("timer:clock?period=5s")
  .saga()
    .setHeader("id", header(Exchange.TIMER_COUNTER))
    .setHeader(Exchange.HTTP_METHOD, constant("POST"))
    .log("Executing saga #${header.id}")
    .to("undertow:http://localhost:8282/train/buy/seat")
    .to("undertow:http://localhost:8383/flight/buy")
    .to("undertow:http://localhost:8484/pay");
```

It executes *3 remote actions* within a saga:
- Buy a train ticket
- Buy an airplane ticket
- Make the payment (*last action, it fails with 30% probability*)

Since all 3 actions are executed in the context of a saga, whenever the payment action fails, 
the other actions are compensated (cancelled) automatically.

Each atomic action declares its corresponding compensating action using the new Saga EIP DSL:

```
rest().post("/train/buy/seat")
  .param().type(RestParamType.header).name("id").required(true).endParam()
  .route()
  .saga()
    .propagation(SagaPropagation.SUPPORTS)
    .option("id", header("id"))
    .compensation("direct:cancelPurchase") // <-- compensation
      .log("Buying train seat #${header.id}");
    
from("direct:cancelPurchase") // <-- points to this
  .log("Train purchase #${header.id} has been cancelled");
```

## Requirements

### LRA Coordinator

This quickstart needs to communicate with a LRA coordinator. You may start the Narayana LRA coordinator from https://github.com/jbosstm/narayana/tree/master/rts/lra/lra-coordinator, 
that listens on `8080` by default.

Checkout the Narayana repository, build and run:

```
# from the lra-coordinator dir
mvn wildfly-swarm:run
``` 

### Camel 2.21.0-SNAPSHOT

Camel needs to be built from this branch: https://github.com/nicolaferraro/camel/tree/saga, with:

```
mvn clean install -P fastinstall
```

## Running the demo

```
# Terminal 1
cd camel-saga-train-service
mvn spring-boot:run
```

```
# Terminal 2
cd camel-saga-flight-service
mvn spring-boot:run
```

```
# Terminal 3
cd camel-saga-payment-service
mvn spring-boot:run
```

```
# Terminal 4
cd camel-saga-app
mvn spring-boot:run
```
