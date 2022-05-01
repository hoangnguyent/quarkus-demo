package nash.demo.quarkus.service.cqrs.demo2;

import nash.demo.quarkus.entity.Citizen;
import nash.demo.quarkus.service.NumberProxy;
import nash.demo.quarkus.service.cqrs.AbstractCqrs;
import nash.demo.quarkus.service.cqrs.AbstractCqrsExecutor;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;

@ApplicationScoped
public class MyCqrs2Executor extends AbstractCqrsExecutor {

    @Inject
    Logger logger;

    @RestClient
    NumberProxy numberProxy;

    @Override
    protected void initReturnDTO(AbstractCqrs cqrs) {
        cqrs.setReturnDTO(new MyCqrs2ReturnDTO());
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW) // using REQUIRES_NEW to make sure record has been flushed
    public ObjectId doBusiness(AbstractCqrs cqrs) {

        MyCqrs2RequestDTO requestDTO = (MyCqrs2RequestDTO) cqrs.getRequestDTO();

        Citizen citizen = new Citizen();
        citizen.setDateOfBirth(Instant.now());
        citizen.setSocialSecurityNumber(numberProxy.getSocialSecurityNumber());
        citizen.setName(requestDTO.getName());
        citizen.setGender(requestDTO.getGender());

        return saveToMongoDb(citizen);
    }

    @Override
    public <I, O> O createDataToReturn(I businessData) {
        ObjectId id = (ObjectId) businessData;
        return (O) searchMongoById(id);

//        String socialSecurityNumber = (String) businessData;
//        return (O) searchMongoBySocialSecurityNumber(socialSecurityNumber);
    }

    private ObjectId saveToMongoDb(Citizen origin) {
        nash.demo.quarkus.entity.mongo.Citizen citizen = new nash.demo.quarkus.entity.mongo.Citizen();
        citizen.setActive(origin.isActive());
        citizen.setDateOfBirth(origin.getDateOfBirth());
        citizen.setGender(origin.getGender());
        citizen.setName(origin.getName());
        citizen.setSocialSecurityNumber(origin.getSocialSecurityNumber());

        citizen.persistOrUpdate();

//        return citizen.getSocialSecurityNumber();
        return citizen.id;
    }

    private Citizen searchMongoById(ObjectId id) {
        nash.demo.quarkus.entity.mongo.Citizen mongoCitizen = nash.demo.quarkus.entity.mongo.Citizen.findById(id);

        Citizen citizen = new Citizen();
        citizen.setActive(mongoCitizen.isActive());
        citizen.setDateOfBirth(mongoCitizen.getDateOfBirth());
        citizen.setGender(mongoCitizen.getGender());
        citizen.setName(mongoCitizen.getName());
        citizen.setSocialSecurityNumber(mongoCitizen.getSocialSecurityNumber());

        return citizen;
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
