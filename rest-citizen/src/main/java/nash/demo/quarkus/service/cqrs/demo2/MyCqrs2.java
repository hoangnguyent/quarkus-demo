package nash.demo.quarkus.service.cqrs.demo2;

import nash.demo.quarkus.service.cqrs.AbstractCqrs;

public class MyCqrs2 extends AbstractCqrs<MyCqrs2RequestDTO, MyCqrs2ReturnDTO> {
    public MyCqrs2(MyCqrs2RequestDTO requestDTO) {
        super(requestDTO);
    }

    @Override
    public MyCqrs2ReturnDTO getReturnDTO() {
        return super.getReturnDTO();
    }
}
