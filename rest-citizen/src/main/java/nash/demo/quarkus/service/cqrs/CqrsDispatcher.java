package nash.demo.quarkus.service.cqrs;

import io.quarkus.arc.Arc;
import nash.demo.quarkus.service.cqrs.demo1.MyCqrs1;
import nash.demo.quarkus.service.cqrs.demo1.MyCqrs1Executor;
import nash.demo.quarkus.service.cqrs.demo2.MyCqrs2;
import nash.demo.quarkus.service.cqrs.demo2.MyCqrs2Executor;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class CqrsDispatcher {
    public void dispatch(List<AbstractCqrs> cqrsList) {
        if (null == cqrsList || cqrsList.isEmpty()) {
            return;
        }

        cqrsList.forEach(cqrs -> {
            getCorrespondingExecutor(cqrs).execute(cqrs);
        });
    }

    private AbstractCqrsExecutor getCorrespondingExecutor(AbstractCqrs cqrs) {
        if (null == cqrs) {
            throw new RuntimeException("Invalid"); //TODO: replace with CustomizeException
        } else if (cqrs instanceof MyCqrs1) {
            return Arc.container().instance(MyCqrs1Executor.class).get();
        } else if (cqrs instanceof MyCqrs2) {
            return Arc.container().instance(MyCqrs2Executor.class).get();
        } else {
            throw new RuntimeException("Invalid");
        }
    }
}