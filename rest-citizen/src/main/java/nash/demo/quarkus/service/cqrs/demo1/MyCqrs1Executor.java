package nash.demo.quarkus.service.cqrs.demo1;

import nash.demo.quarkus.entity.Citizen;
import nash.demo.quarkus.service.NumberProxy;
import nash.demo.quarkus.service.cqrs.AbstractCqrs;
import nash.demo.quarkus.service.cqrs.AbstractCqrsExecutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
public class MyCqrs1Executor extends AbstractCqrsExecutor {

    @Inject
    Logger logger;

    @RestClient
    NumberProxy numberProxy;

    @Override
    protected void initReturnDTO(AbstractCqrs cqrs) {
        cqrs.setReturnDTO(new MyCqrs1ReturnDTO());
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public String doBusiness(AbstractCqrs cqrs) {

        MyCqrs1RequestDTO requestDTO = (MyCqrs1RequestDTO) cqrs.getRequestDTO();

        Citizen citizen = new Citizen();
        citizen.setDateOfBirth(Instant.now());
        citizen.setSocialSecurityNumber(numberProxy.getSocialSecurityNumber());
        citizen.setName(requestDTO.getName());
        citizen.setGender(requestDTO.getGender());

        return saveToMongoDb(citizen);
    }

    @Override
    public <I, O> O createDataToReturn(I businessData) {
        return null;
    }

    private String saveToMongoDb(Citizen origin) {
        nash.demo.quarkus.entity.mongo.Citizen citizen = new nash.demo.quarkus.entity.mongo.Citizen();
        citizen.setActive(origin.isActive());
        citizen.setDateOfBirth(origin.getDateOfBirth());
        citizen.setGender(origin.getGender());
        citizen.setName(origin.getName());
        citizen.setSocialSecurityNumber(origin.getSocialSecurityNumber());

        citizen.persistOrUpdate();
        return citizen.getSocialSecurityNumber();
    }

    private Citizen searchMongoBySocialSecurityNumber(String socialSecurityNumber) {
        nash.demo.quarkus.entity.mongo.Citizen mongoCitizen = nash.demo.quarkus.entity.mongo.Citizen.find("socialSecurityNumber", socialSecurityNumber).firstResult();

        Citizen citizen = new Citizen();
        citizen.setActive(mongoCitizen.isActive());
        citizen.setDateOfBirth(mongoCitizen.getDateOfBirth());
        citizen.setGender(mongoCitizen.getGender());
        citizen.setName(mongoCitizen.getName());
        citizen.setSocialSecurityNumber(mongoCitizen.getSocialSecurityNumber());

        return citizen;
    }
}
