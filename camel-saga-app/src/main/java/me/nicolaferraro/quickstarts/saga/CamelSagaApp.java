package me.nicolaferraro.quickstarts.saga;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class CamelSagaApp {

    public static void main(String[] args) {
        SpringApplication.run(CamelSagaApp.class, args);
    }

    @Component
    static class Routes extends RouteBuilder {
        @Override
        public void configure() throws Exception {
            restConfiguration().port(8181);

            from("timer:clock?period=5s")
                    .saga()
                        .setHeader("id", header(Exchange.TIMER_COUNTER))
                        .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                        .log("Executing saga #${header.id}")
                        .to("undertow:http://localhost:8282/train/buy/seat")
                        .to("undertow:http://localhost:8383/flight/buy")
                        .to("undertow:http://localhost:8484/pay");

        }
    }

}
