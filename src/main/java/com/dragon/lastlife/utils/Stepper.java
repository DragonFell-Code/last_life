package com.dragon.lastlife.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

/**
 * Fires an event once per step crossed (step, 2*step, 3*step, …) as a running total increases.
 * <p>
 * Supports both BigDecimal and double inputs. It is monotonic in one direction:
 * only fires when the computed bucket index increases (no re-fire on decreases).
 *
 * @param step integer step size, e.g., 50
 */
public record Stepper(int step, Consumer<BigDecimal> consumer) {
    public Stepper {
        if (step <= 0) throw new IllegalArgumentException("step must be > 0");
    }


    /**
     * BigDecimal variant. Call this with the latest cumulative total.
     * Fires once per crossed step: step, 2*step, 3*step, …
     */
    public void accept() {
        BigDecimal total = Utils.configs().DONATION_CONFIG().total;
        if (total == null) throw new IllegalArgumentException("total cannot be null");
        if (consumer == null) throw new IllegalArgumentException("consumer cannot be null");
        BigDecimal stepBD = BigDecimal.valueOf(step);
        long lastBucket = Utils.configs().DONATION_CONFIG().last_bucket;
        long currentBucket = total
                .divide(stepBD, 0, RoundingMode.FLOOR)
                .longValue();

        if (currentBucket > lastBucket) {
            for (long b = lastBucket + 1; b <= currentBucket; b++) {
                BigDecimal threshold = stepBD.multiply(BigDecimal.valueOf(b));
                consumer.accept(threshold);
            }
            lastBucket = currentBucket;
        }
    }

}