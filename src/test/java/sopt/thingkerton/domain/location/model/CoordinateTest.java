package sopt.thingkerton.domain.location.model;

import org.junit.jupiter.api.Test;
import sopt.thingkerton.domain.location.code.LocationErrorCode;
import sopt.thingkerton.global.exception.BusinessException;

import static org.assertj.core.api.Assertions.*;

class CoordinateTest {
    @Test
    void acceptsBoundariesAndRejectsInvalidCoordinates() {
        assertThat(new Coordinate(90, -180).latitude()).isEqualTo(90);
        for (double[] values : new double[][] {{91, 0}, {-91, 0}, {0, 181}, {0, -181}, {Double.NaN, 0}, {0, Double.POSITIVE_INFINITY}}) {
            assertThatThrownBy(() -> new Coordinate(values[0], values[1]))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(LocationErrorCode.INVALID_COORDINATE);
        }
    }
}
