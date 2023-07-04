package nl.ing.lovebird.cassandra;

import lombok.experimental.UtilityClass;

@UtilityClass
class StringUtils {
    boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }
}
