package me.nicolaferraro.quickstarts.saga.payment;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class CamelSagaPaymentService {

    public static void main(String[] args) {
        SpringApplication.run(CamelSagaPaymentService.class, args);
    }

    @Component
    static class Routes extends RouteBuilder {
        @Override
        public void configure() throws Exception {

            restConfiguration().port(8484);


            rest().post("/pay")
                    .param().type(RestParamType.header).name("id").required(true).endParam()
                    .route()
                    .saga()
                        .propagation(SagaPropagation.MANDATORY)
                        .option("id", header("id"))
                        .compensation("direct:cancelPayment")
                    .log("Paying for order #${header.id}")
                    .choice()
                        .when(x -> Math.random() >= 0.7)
                            .throwException(new RuntimeException("Random failure during payment"))
                    .end();

            from("direct:cancelPayment")
                    .log("Payment #${header.id} has been cancelled");

        }
    }

}
