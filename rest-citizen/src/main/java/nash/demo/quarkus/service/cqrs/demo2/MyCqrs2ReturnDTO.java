package nash.demo.quarkus.service.cqrs.demo2;

import nash.demo.quarkus.entity.Citizen;
import nash.demo.quarkus.service.cqrs.AbstractCqrsReturnDTO;

public class MyCqrs2ReturnDTO<T> extends AbstractCqrsReturnDTO {
    public Citizen getCitizen() {
        return (Citizen) super.getData();
    }
}
