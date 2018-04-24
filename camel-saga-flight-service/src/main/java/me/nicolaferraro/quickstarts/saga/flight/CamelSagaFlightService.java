package me.nicolaferraro.quickstarts.saga.flight;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class CamelSagaFlightService {

    public static void main(String[] args) {
        SpringApplication.run(CamelSagaFlightService.class, args);
    }

    @Component
    static class Routes extends RouteBuilder {
        @Override
        public void configure() throws Exception {

            restConfiguration().port(8383);


            rest().post("/flight/buy")
                    .param().type(RestParamType.header).name("id").required(true).endParam()
                    .route()
                    .saga()
                        .propagation(SagaPropagation.MANDATORY)
                        .option("id", header("id"))
                        .compensation("direct:cancelPurchase")
                    .log("Buying flight #${header.id}")
                    .to("http4://camel-saga-payment-service:8080/api/pay?bridgeEndpoint=true&type=flight")
                    .log("Payment for flight #${header.id} done");

            from("direct:cancelPurchase")
                    .log("Flight purchase #${header.id} has been cancelled");

        }
    }

}
