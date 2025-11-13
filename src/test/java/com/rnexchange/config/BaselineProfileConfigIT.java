package com.rnexchange.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

class BaselineProfileConfigIT {

    @ParameterizedTest
    @MethodSource("profileContexts")
    void liquibaseContextsAlignWithBaseline(String resourcePath, List<String> expectedContexts) throws IOException {
        List<String> contexts = loadLiquibaseContexts(resourcePath);

        assertThat(contexts)
            .as("Liquibase contexts for %s", resourcePath)
            .contains("baseline")
            .doesNotContain("faker")
            .containsExactlyElementsOf(expectedContexts);
    }

    private static Stream<Arguments> profileContexts() {
        return Stream.of(
            Arguments.of("config/application-dev.yml", List.of("dev", "baseline")),
            Arguments.of("config/application-prod.yml", List.of("baseline")),
            Arguments.of("config/application.yml", List.of("test", "baseline"))
        );
    }

    private List<String> loadLiquibaseContexts(String resourcePath) throws IOException {
        Resource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            throw new IllegalStateException("Expected resource not found: " + resourcePath);
        }

        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> propertySources = loader.load(resource.getDescription(), resource);

        for (PropertySource<?> propertySource : propertySources) {
            Object value = propertySource.getProperty("spring.liquibase.contexts");
            if (value != null) {
                return toStringList(value);
            }
        }
        throw new IllegalStateException("Property spring.liquibase.contexts not found in " + resourcePath);
    }

    private List<String> toStringList(Object value) {
        List<String> contexts = new ArrayList<>();
        if (value instanceof String stringValue) {
            for (String entry : stringValue.split(",")) {
                String trimmed = entry.trim();
                if (!trimmed.isEmpty()) {
                    contexts.add(trimmed);
                }
            }
            return contexts;
        }
        if (value instanceof Iterable<?> iterableValue) {
            Iterator<?> iterator = iterableValue.iterator();
            while (iterator.hasNext()) {
                Object entry = iterator.next();
                if (entry != null) {
                    String text = entry.toString().trim();
                    if (!text.isEmpty()) {
                        contexts.add(text);
                    }
                }
            }
            return contexts;
        }
        throw new IllegalStateException("Unsupported property value type: " + value.getClass());
    }
}
