package nash.demo.quarkus.mock;

import io.quarkus.test.Mock;
import nash.demo.quarkus.service.NumberService;

import javax.enterprise.context.ApplicationScoped;

@Mock
@ApplicationScoped
public class MockNumberService extends NumberService {

    @Override
    public String generate() {
        return "a number";
    }
}
