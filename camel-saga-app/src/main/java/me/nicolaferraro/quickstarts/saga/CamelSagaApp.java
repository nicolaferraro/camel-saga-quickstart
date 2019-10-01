package me.nicolaferraro.quickstarts.saga;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestBindingMode;
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

            // from("timer:clock?period=5s")
            restConfiguration()
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "My Sagas REST API")
                .apiProperty("api.version", "1.0")
                .apiProperty("cors", "true")
                .apiProperty("base.path", "/api")
                .apiProperty("api.path", "/")
                .apiProperty("host", "")
                .apiContextRouteId("doc-api")
                    .component("servlet")
                    .dataFormatProperty("json.in.disableFeatures", "FAIL_ON_EMPTY_BEANS,FAIL_ON_UNKNOWN_PROPERTIES")
                    .bindingMode(RestBindingMode.auto);
            
            rest()
                .post("/saga/start").description("Initiate a new SAGA request...")
                .route().routeId("reservation")
                .log("Make a new reservation.")
                .saga()
                    .setHeader("id", header(Exchange.TIMER_COUNTER))
                    .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                    .log("Executing saga #${header.id}")
                    .to("http4://camel-saga-train-service:8080/api/train/buy/seat?bridgeEndpoint=true")
                    .to("http4://camel-saga-flight-service:8080/api/flight/buy?bridgeEndpoint=true")
            .endRest();

        }
    }

}
