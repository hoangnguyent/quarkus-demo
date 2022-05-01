package nash.demo.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
public class CitizenApiTest {

    @Test
    public void testCreateEndpoint() {
        given()
                .formParam("name", "A name")
                .formParam("gender", 0)
                .when().post("/citizens/create")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("active", is(true))
                .body("date_of_birth", startsWith("2022"))
                .body("gender", is(0))
                .body("name", is("A name"))
                .body("social_security_number", is("a number"))
        ;
    }
}