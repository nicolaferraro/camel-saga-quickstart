# Camel Saga Quickstart

This quickstarts demonstrates how to use the new saga feature of Camel 2.21.

It runs on **Kubernetes** or **Openshift**. You can install a development version, like [Minishift](https://github.com/minishift/minishift/releases)
or [Minikube](https://github.com/kubernetes/minikube/releases).

<p style="text-align: center">
    <img src="/saga-quickstart-system.png" alt="Saga Quickstart System"/>
</p>

The `camel-saga-app` module has the following route:

```java
from("timer:clock?period=5s")
  .saga()
    .setHeader("id", header(Exchange.TIMER_COUNTER))
    .setHeader(Exchange.HTTP_METHOD, constant("POST"))
    .log("Executing saga #${header.id}")
    .to("http4://camel-saga-train-service:8080/api/train/buy/seat")
    .to("http4://camel-saga-flight-service:8080/api/flight/buy");
```

It executes *2 remote actions* within a saga:
- Buy a train ticket
- Buy an airplane ticket

Each action in turn will make a call to the payment service *within the context of the saga*. *Calls to payment fail with 15% probability*.

Since all actions are executed in the context of a saga, whenever one of the payment action fails (or another action, for any reason), 
the whole saga is compensated (cancelled) automatically.

Each atomic action declares its corresponding compensating action using the new Saga EIP DSL. For example, the train 
route is:

```java
rest().post("/train/buy/seat")
  .param().type(RestParamType.header).name("id").required(true).endParam()
  .route()
  .saga()
    .propagation(SagaPropagation.SUPPORTS)
    .option("id", header("id"))
    .compensation("direct:cancelPurchase") // <-- compensation
  .log("Buying train seat #${header.id}")
  .to("http4://camel-saga-payment-service:8080/api/pay?bridgeEndpoint=true&type=train")
  .log("Payment for train #${header.id} done");

from("direct:cancelPurchase") // <-- compensation points to this
  .log("Train purchase #${header.id} has been cancelled");
```

## Requirements

### Openshift or Kubernetes

You can install [Minishift](https://github.com/minishift/minishift/releases).

## Running the demo

This project uses the [Fabric8 Maven Plugin](https://maven.fabric8.io/) to deploy itself automatically to Openshift or Kubernetes.

After you connect to the cluster, type the following command on a terminal from the repository root:

```
oc create -f lra-coordinator.yaml
mvn clean fabric8:deploy
```

Look into Openshift/Kubernetes console, all components will be deployed. You can follow the logs of the different services to see compensating actions.
