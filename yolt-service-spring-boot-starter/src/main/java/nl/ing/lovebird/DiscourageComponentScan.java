package nl.ing.lovebird;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Discourage people from ComponentScanning nl.ing.lovebird, as we want to rely on spring-boot-starters only, for easier refactoring and
 * conditional bean loading logic.
 */
@Component
public class DiscourageComponentScan implements InitializingBean {
    @Override
    public void afterPropertiesSet() {
        throw new IllegalStateException("Please stop ComponentScanning nl.ing.lovebird");
    }


}
