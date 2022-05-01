package nash.demo.quarkus.entity;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class CitizenDeserializer extends JsonbDeserializer<Citizen> {
    public CitizenDeserializer() {
        super(Citizen.class);
    }
}
