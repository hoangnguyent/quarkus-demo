package nash.demo.quarkus.service.cqrs;

import java.io.Serializable;

public abstract class AbstractCqrsReturnDTO<D> implements Serializable {
    private D data;

    public D getData() {
        return data;
    }

    public void setData(D data) {
        this.data = data;
    }
}
