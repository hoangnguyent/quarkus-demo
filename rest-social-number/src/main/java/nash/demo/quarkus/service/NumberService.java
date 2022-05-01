package nash.demo.quarkus.service;

import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import java.util.Random;

@ApplicationScoped
public class NumberService {
    public String generate() {
        return String.format("%s - %s - %s", stripZeroNum(), stripZeroNum(), stripZeroNum());
    }

    private String stripZeroNum() {
        String value = String.valueOf(new Random().nextInt(10000));
        return StringUtils.stripStart(value, "0");
    }
}