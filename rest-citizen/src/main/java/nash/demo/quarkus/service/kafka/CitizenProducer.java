package nash.demo.quarkus.service.kafka;

import io.smallrye.mutiny.Multi;
import nash.demo.quarkus.entity.Citizen;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@ApplicationScoped
public class CitizenProducer {
    @Outgoing("citizen-out")
    public Multi<Citizen> autoSend() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(2))
                .map(it -> {
                            Citizen citizen = new Citizen();
                            citizen.setActive(new Random().nextInt(2) == 0);
                            citizen.setDateOfBirth(Instant.now());
                            citizen.setGender(new Random().nextInt(3));
                            citizen.setName("A new name (" + new Random().nextInt(10000) + ")");
                            citizen.setId(Math.abs(new Random().nextLong()));
                            citizen.setSocialSecurityNumber("undetermined");
                            return citizen;
                        }
                );
    }
}