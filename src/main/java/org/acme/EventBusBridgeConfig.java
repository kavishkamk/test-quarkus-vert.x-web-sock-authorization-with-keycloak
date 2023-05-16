package org.acme;

import io.quarkus.logging.Log;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.Router;
import io.vertx.mutiny.ext.web.handler.sockjs.SockJSHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class EventBusBridgeConfig {

    private final Vertx vertx;

    public EventBusBridgeConfig(Vertx vertx) {
        this.vertx = vertx;
    }

    public void init(@Observes Router router) {

        Log.info("here.....");
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);

        SockJSBridgeOptions sockJSBridgeOptions = new SockJSBridgeOptions()
                        .addInboundPermitted(new PermittedOptions().setAddress("telemetry-subscribe").setRequiredAuthority("place_order"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("telemetry-feed-.*"));


        router.route("/eventbus/*")
                .subRouter(sockJSHandler.bridge(sockJSBridgeOptions, bridgeEvent -> {

                    Log.info("in the event capturing:" + bridgeEvent.type());

                    if (bridgeEvent.type() == BridgeEventType.SOCKET_CREATED) {
                        Log.info("socket created");
                    }

                    if (bridgeEvent.type() == BridgeEventType.REGISTER) {
                        String address = bridgeEvent.getRawMessage().getString("address");
                        Log.info("socket registered, address: " + address);
                    }

                    if (bridgeEvent.type() == BridgeEventType.SOCKET_CLOSED) {
                        Log.info("socket closed");
                    }

                    bridgeEvent.complete(true);

                }));
    }
}
