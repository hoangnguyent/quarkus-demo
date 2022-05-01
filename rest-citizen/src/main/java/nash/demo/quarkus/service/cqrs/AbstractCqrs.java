package nash.demo.quarkus.service.cqrs;

public abstract class AbstractCqrs<I extends AbstractCqrsRequestDTO, O extends AbstractCqrsReturnDTO> {

    private boolean executed = false;
    private I requestDTO;
    private O returnDTO;

    private AbstractCqrs() {
        // disable default constructor
    }

    public AbstractCqrs(I requestDTO) {
        this.requestDTO = requestDTO;
    }

    protected void validateRequest() throws RuntimeException {
        requestDTO.validate();
    }

    public void setRequestDTO(I requestDTO) {
        this.requestDTO = requestDTO;
    }

    public I getRequestDTO() {
        return requestDTO;
    }

    public void setReturnDTO(O returnDTO) {
        this.returnDTO = returnDTO;
    }

    public O getReturnDTO() {
        return returnDTO;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }
}
