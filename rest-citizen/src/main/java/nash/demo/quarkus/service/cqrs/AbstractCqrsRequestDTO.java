package nash.demo.quarkus.service.cqrs;

import java.io.Serializable;

public abstract class AbstractCqrsRequestDTO implements Serializable {
    public abstract void validate() throws RuntimeException;
}
