package nash.demo.quarkus;

import nash.demo.quarkus.service.NumberService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/numbers")
public class NumberApi {

    @Inject
    NumberService numberService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String generate() {
        return numberService.generate();
    }
}