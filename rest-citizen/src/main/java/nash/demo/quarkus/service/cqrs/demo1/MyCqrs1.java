package nash.demo.quarkus.service.cqrs.demo1;

import nash.demo.quarkus.service.cqrs.AbstractCqrs;

public class MyCqrs1 extends AbstractCqrs<MyCqrs1RequestDTO, MyCqrs1ReturnDTO> {

    public MyCqrs1(MyCqrs1RequestDTO requestDTO) {
        super(requestDTO);
    }

    @Override
    public MyCqrs1ReturnDTO getReturnDTO() {
        return super.getReturnDTO();
    }
}
