package nash.demo.quarkus.service.cqrs.demo1;

import nash.demo.quarkus.service.cqrs.AbstractCqrsRequestDTO;

public class MyCqrs1RequestDTO extends AbstractCqrsRequestDTO {

    private int gender;
    private String name;

    @Override
    public void validate() throws RuntimeException {
        // Nothing to validate
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
