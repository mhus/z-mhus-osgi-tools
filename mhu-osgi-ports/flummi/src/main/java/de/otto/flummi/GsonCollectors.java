package de.otto.flummi;

import static java.util.function.Function.identity;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class GsonCollectors {
    public static Collector<JsonElement, JsonArray, JsonArray> toJsonArray() {
        Supplier<JsonArray> supplier = JsonArray::new;
        BiConsumer<JsonArray, JsonElement> accumulator = JsonArray::add;
        BinaryOperator<JsonArray> combiner = (array1, array2) -> {
            array1.addAll(array2);
            return array1;
        };
        return Collector.of(supplier, accumulator, combiner, identity());
    }
}
