package nash.demo.quarkus;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.orm.PersistenceUnit;
import nash.demo.quarkus.entity.Citizen;
import nash.demo.quarkus.service.NumberProxy;
import nash.demo.quarkus.service.cqrs.AbstractCqrs;
import nash.demo.quarkus.service.cqrs.CqrsDispatcher;
import nash.demo.quarkus.service.cqrs.demo1.MyCqrs1;
import nash.demo.quarkus.service.cqrs.demo1.MyCqrs1Executor;
import nash.demo.quarkus.service.cqrs.demo1.MyCqrs1RequestDTO;
import nash.demo.quarkus.service.cqrs.demo2.MyCqrs2;
import nash.demo.quarkus.service.cqrs.demo2.MyCqrs2Executor;
import nash.demo.quarkus.service.cqrs.demo2.MyCqrs2RequestDTO;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Path("/citizens")
public class CitizenApi {

    @Inject
    Logger logger;

    @Inject
    @DataSource("db1")
    AgroalDataSource db1DataSource;

    @Inject
    @DataSource("db2")
    AgroalDataSource db2DataSource;

    @Inject
    @PersistenceUnit("db3")
    EntityManager db3EntityManager;

    @Inject
    @PersistenceUnit("db4")
    EntityManager db4EntityManager;

    @RestClient
    NumberProxy numberProxy;

    @Inject
    @Channel("my_chanel_kafka")
    Publisher<Citizen> publisher;

    @Inject
    @Channel("my_chanel_activemq")
    Publisher<Citizen> publisherFromActiveMQ;

    @Inject
    MyCqrs1Executor myCqrs1Executor;

    @Inject
    MyCqrs2Executor myCqrs2Executor;

    @Inject
    CqrsDispatcher cqrsDispatcher;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @CacheResult(cacheName = "social_security_number")
    public List<Citizen> list() {
        try {
            CallableStatement stmt = db1DataSource.getConnection().prepareCall("select * from citizen");
            stmt.execute();
            ResultSet rs = stmt.getResultSet();

            List<Citizen> citizens = new ArrayList<>();
            int count1 = 0;
            while (rs.next()) {
                count1++;
                Citizen citizen = new Citizen();
                citizen.setId(rs.getLong("id"));
                citizen.setDateOfBirth(rs.getTimestamp("date_of_birth").toInstant());
                citizen.setSocialSecurityNumber(rs.getString("social_security_number"));
                citizen.setName(rs.getString("name"));
                citizen.setGender(rs.getInt("gender"));
                citizen.setActive(rs.getBoolean("active"));
                citizens.add(citizen);
            }

            logger.info("DB1 count: " + count1);

            int count2 = 0;
            CallableStatement stmt2 = db2DataSource.getConnection().prepareCall("select * from citizen");
            stmt2.execute();
            ResultSet rs2 = stmt2.getResultSet();
            while (rs2.next()) {
                count2++;
            }
            logger.info("DB2 count: " + count2);

            int count3 = db3EntityManager.createQuery("select c from citizen c", nash.demo.quarkus.entity.a.Citizen.class).getResultList().size();
            logger.info("DB3 count: " + count3);

            int count4 = db4EntityManager.createQuery("select c from citizen c", nash.demo.quarkus.entity.b.Citizen.class).getResultList().size();
            logger.info("DB4 count: " + count4);

            logger.info("DB5 count (default): " + nash.demo.quarkus.entity.c.Citizen.count());
            logger.info("DB5 list all then count (manual): " + nash.demo.quarkus.entity.c.Citizen.list("1=1").size());

            logger.info("MongoDb count: " + nash.demo.quarkus.entity.mongo.Citizen.count());

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return citizens;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/stream/kafka")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON)
    public Publisher<Citizen> streamCitizens() {
        return publisher;
    }

    @GET
    @Path("/stream/activemq")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON)
    public Publisher<Citizen> streamCitizensFromActiveMQ() {
        return publisherFromActiveMQ;
    }

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Fallback(fallbackMethod = "fallbackWhenCreate")
    @Retry(delay = 1500, maxRetries = 1)
    @Transactional
    public Citizen create(@FormParam("name") String name,
                          @FormParam("gender") int gender) {

        Citizen citizen = new Citizen();
        citizen.setDateOfBirth(Instant.now());
        citizen.setSocialSecurityNumber(numberProxy.getSocialSecurityNumber());
        citizen.setName(name);
        citizen.setGender(gender);

        saveToDb1(citizen);
        saveToDb2(citizen);
        saveToDb3(citizen);
        saveToDb4(citizen);
        saveToDb5(citizen);
        saveToMongoDb(citizen);

        logger.info("Citizen has been created, " + citizen);
        return citizen;
    }

    @POST
    @Path("/cqrs1")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Citizen doCqrs1(@FormParam("name") String name,
                          @FormParam("gender") int gender) {

        MyCqrs1RequestDTO requestDTO = new MyCqrs1RequestDTO();
        requestDTO.setGender(gender);
        requestDTO.setName(name);
        MyCqrs1 cqrs = new MyCqrs1(requestDTO);
        myCqrs1Executor.execute(cqrs);
        return null;
    }

    @POST
    @Path("/cqrs2")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Citizen doCqrs2(@FormParam("name") String name,
                           @FormParam("gender") int gender) {

        MyCqrs2RequestDTO requestDTO = new MyCqrs2RequestDTO();
        requestDTO.setGender(gender);
        requestDTO.setName(name);
        MyCqrs2 cqrs = new MyCqrs2(requestDTO);
        myCqrs2Executor.execute(cqrs);
        return cqrs.getReturnDTO().getCitizen();
    }

    @POST
    @Path("/multiple-cqrs")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Citizen doMultipleCqrs(@FormParam("name") String name,
                           @FormParam("gender") int gender) {

        MyCqrs1RequestDTO request1DTO = new MyCqrs1RequestDTO();
        request1DTO.setGender(gender);
        request1DTO.setName(name);
        MyCqrs1 cqrs1 = new MyCqrs1(request1DTO);

        MyCqrs2RequestDTO request2DTO = new MyCqrs2RequestDTO();
        request2DTO.setGender(gender);
        request2DTO.setName(name);
        MyCqrs2 cqrs2 = new MyCqrs2(request2DTO);

        cqrsDispatcher.dispatch(List.of(cqrs1, cqrs2, cqrs2));

        return null;
    }

    @Transactional
    public Citizen fallbackWhenCreate(@FormParam("name") String name,
                                      @FormParam("gender") int gender) {

        Citizen citizen = new Citizen();
        citizen.setDateOfBirth(Instant.now());
        citizen.setSocialSecurityNumber("undetermined");
        citizen.setName(name);
        citizen.setGender(gender);

        saveToDb1(citizen);
        saveToDb2(citizen);
        saveToDb3(citizen);
        saveToDb4(citizen);
        saveToDb5(citizen);
        saveToMongoDb(citizen);

        logger.warn("Citizen has been saved, " + citizen);

        return citizen;
    }

    private void saveToDb1(Citizen citizen) {
        try {
            int index = 0;
            CallableStatement stmt = db1DataSource.getConnection().prepareCall("insert into citizen (date_of_birth, social_security_number, name, gender, active) values (?, ?, ?, ?, ?)");
            stmt.setTimestamp(++index, Timestamp.from(citizen.getDateOfBirth()));
            stmt.setString(++index, citizen.getSocialSecurityNumber());
            stmt.setString(++index, citizen.getName());
            stmt.setInt(++index, citizen.getGender());
            stmt.setBoolean(++index, citizen.isActive());

            stmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveToDb2(Citizen citizen) {
        try {
            int index = 0;
            CallableStatement stmt = db2DataSource.getConnection().prepareCall("insert into citizen (date_of_birth, social_security_number, name, gender, active) values (?, ?, ?, ?, ?)");
            stmt.setTimestamp(++index, Timestamp.from(citizen.getDateOfBirth()));
            stmt.setString(++index, citizen.getSocialSecurityNumber());
            stmt.setString(++index, citizen.getName());
            stmt.setInt(++index, citizen.getGender());
            stmt.setBoolean(++index, citizen.isActive());

            stmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveToDb3(Citizen origin) {
        nash.demo.quarkus.entity.a.Citizen citizen = new nash.demo.quarkus.entity.a.Citizen();
        citizen.setActive(origin.isActive());
        citizen.setDateOfBirth(origin.getDateOfBirth());
        citizen.setGender(origin.getGender());
        citizen.setName(origin.getName());
        citizen.setSocialSecurityNumber(origin.getSocialSecurityNumber());

        db3EntityManager.persist(citizen);
    }

    private void saveToDb4(Citizen origin) {
        nash.demo.quarkus.entity.b.Citizen citizen = new nash.demo.quarkus.entity.b.Citizen();
        citizen.setActive(origin.isActive());
        citizen.setDateOfBirth(origin.getDateOfBirth());
        citizen.setGender(origin.getGender());
        citizen.setName(origin.getName());
        citizen.setSocialSecurityNumber(origin.getSocialSecurityNumber());

        db4EntityManager.persist(citizen);
    }

    private void saveToDb5(Citizen origin) {
        nash.demo.quarkus.entity.c.Citizen citizen = new nash.demo.quarkus.entity.c.Citizen();
        citizen.setActive(origin.isActive());
        citizen.setDateOfBirth(origin.getDateOfBirth());
        citizen.setGender(origin.getGender());
        citizen.setName(origin.getName());
        citizen.setSocialSecurityNumber(origin.getSocialSecurityNumber());

        citizen.persist();
    }

    private void saveToMongoDb(Citizen origin) {
        nash.demo.quarkus.entity.mongo.Citizen citizen = new nash.demo.quarkus.entity.mongo.Citizen();
        citizen.setActive(origin.isActive());
        citizen.setDateOfBirth(origin.getDateOfBirth());
        citizen.setGender(origin.getGender());
        citizen.setName(origin.getName());
        citizen.setSocialSecurityNumber(origin.getSocialSecurityNumber());

        citizen.persistOrUpdate();
    }


}