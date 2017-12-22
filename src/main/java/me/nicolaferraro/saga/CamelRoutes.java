package me.nicolaferraro.saga;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class CamelRoutes extends RouteBuilder {

    public void configure() throws Exception {

        restConfiguration().port(8181);

        from("timer:clock?period=5s")
                .saga()
                    .option("id", header(Exchange.TIMER_COUNTER))
                    .compensation("direct:cancelAll")
                    .completion("direct:confirm")
                .setHeader("id", header(Exchange.TIMER_COUNTER))
                .to("direct:buyTrain")
                .to("direct:buyFlight")
                .to("direct:pay");

        from("direct:buyTrain")
                .log("Buying a train ticket #${header.id}");

        from("direct:buyFlight")
                .log("Buying flight #${header.id}");

        from("direct:pay")
                .choice()
                    .when(x -> Math.random() >= 0.6)
                        .throwException(new RuntimeException("random failure"))
                .end()
                .log("Paying for saga #${header.id}");

        // ----- Saga actions -----

        from("direct:confirm")
                .log("Saga #${header.id} completed!!!");

        from("direct:cancelAll")
                .log("Saga #${header.id} compensation")
                .log("Cancelling payment #${header.id}")
                .log("Cancelling train ticket #${header.id}")
                .log("Cancelling flight #${header.id}");

    }
}
