package nash.demo.quarkus.service.cqrs;

import org.jboss.logging.Logger;

import javax.inject.Inject;

public abstract class AbstractCqrsExecutor {
    @Inject
    Logger logger;

    private <C extends AbstractCqrs> void validateRequest(C cqrs) {
        cqrs.getRequestDTO().validate();
    }

    protected abstract <C extends AbstractCqrs> void initReturnDTO(C cqrs);

    public abstract <C extends AbstractCqrs, D> D doBusiness(C cqrs);

    public abstract <I, O> O createDataToReturn(I input);

    public <B, C extends AbstractCqrs, D> void execute(C cqrs) {

        try {

            // Prevent execute multiple times
            if (cqrs.isExecuted()) {
                logger.info(this.getClass().getSimpleName() + " has been executed  already!");
                return;
            }

            cqrs.setExecuted(true);
            logger.info("START: " + this.getClass().getSimpleName());

            validateRequest(cqrs);

            B businessResult = doBusiness(cqrs);

            // Create return
            initReturnDTO(cqrs);
            D data = createDataToReturn(businessResult);
            if (null != data) {
                cqrs.getReturnDTO().setData(data);
            }

        } catch (Exception e) {
            throw new RuntimeException(e); //TODO: replace with CustomizeException

        } finally {
            logger.info("END: " + this.getClass().getSimpleName());
        }
    }
}
