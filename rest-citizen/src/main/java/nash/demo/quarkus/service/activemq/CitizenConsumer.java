package nash.demo.quarkus.service.activemq;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import nash.demo.quarkus.entity.Citizen;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import javax.inject.Inject;

public class CitizenConsumer {
    @Inject
    Logger logger;

    @Incoming("citizen-activemq-in")
    @Outgoing("my_chanel_activemq")
    @Broadcast
    public Citizen consume(Citizen citizen) {
        logger.infof("Got an Citizen: ", citizen);
        return citizen;
    }
}
