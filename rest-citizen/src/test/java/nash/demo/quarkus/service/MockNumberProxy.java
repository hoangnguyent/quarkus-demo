package nash.demo.quarkus.service;

import io.quarkus.test.Mock;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Mock
@RestClient
public class MockNumberProxy implements NumberProxy {
    @Override
    public String getSocialSecurityNumber() {
        return "a number";
    }
}