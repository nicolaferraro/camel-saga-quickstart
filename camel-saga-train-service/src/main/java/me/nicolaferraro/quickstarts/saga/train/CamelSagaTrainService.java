package me.nicolaferraro.quickstarts.saga.train;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class CamelSagaTrainService {

    public static void main(String[] args) {
        SpringApplication.run(CamelSagaTrainService.class, args);
    }

    @Component
    static class Routes extends RouteBuilder {
        @Override
        public void configure() {

            rest().post("/train/buy/seat")
                    .param().type(RestParamType.header).name("id").required(true).endParam()
                    .route()
                    .saga()
                        .propagation(SagaPropagation.SUPPORTS)
                        .option("id", header("id"))
                        .compensation("direct:cancelPurchase")
                    .log("Buying train seat #${header.id}")
                    .to("http4://camel-saga-payment-service:8080/api/pay?bridgeEndpoint=true&type=train")
                    .log("Payment for train #${header.id} done");

            from("direct:cancelPurchase")
                    .log("Train purchase #${header.id} has been cancelled");

        }
    }

}
