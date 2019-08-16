package org.freedesktop.secret.simple;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Example {

    @BeforeEach
    public void beforeEach() throws InterruptedException {
        Thread.sleep(50L);
    }

    @AfterEach
    public void afterEach() throws InterruptedException {
        Thread.sleep(50L);
    }

    /*
     * FIXME: [#4](https://github.com/swiesend/secret-service/issues/4)
     *     Running JUnit test for all in secret-service fails, whereas individually they pass.
     *     [main] INFO org.freedesktop.secret.handlers.SignalHandler - await signal org.freedesktop.secret.interfaces.Prompt$Completed(/org/freedesktop/secrets/prompt/u8) within 60 seconds.
     *     [main] ERROR org.freedesktop.secret.handlers.SignalHandler - java.util.concurrent.TimeoutException
     */
    @Test
    @Disabled
    @DisplayName("Create a password in the user's default collection.")
    public void createPasswordInTheDefaultCollection() {
        Optional<SimpleCollection> connection = new SimpleService().connect();

        try (SimpleCollection collection = connection.get()) {
            String item = collection.createItem("My Item", "secret").get();

            char[] actual = collection.getSecret(item);
            assertEquals("secret", new String(actual));
            assertEquals("My Item", collection.getLabel(item));

            collection.deleteItem(item);
        } catch (NoSuchElementException | IOException e) {
            // something went wrong
        } // clears automatically all session secrets in memory
    }

    @Test
    @DisplayName("Create a password in a non-default collection.")
    public void createPasswordInANonDefaultCollection() {
        Optional<SimpleCollection> connection = new SimpleService().connect("My Collection", "super secret");

        try (SimpleCollection collection = connection.get()) {
            String item = collection.createItem("My Item", "secret").get();

            char[] actual = collection.getSecret(item);
            assertEquals("secret", new String(actual));
            assertEquals("My Item", collection.getLabel(item));

            collection.deleteItem(item);
            collection.delete();
        } catch (NoSuchElementException | IOException e) {
            // something went wrong
        } // clears automatically all session secrets in memory
    }

    @Test
    @DisplayName("Create a password with additional attributes.")
    public void createPasswordWithAttributes() {
        Optional<SimpleCollection> connection = new SimpleService().connect("My Collection", "super secret");

        try (SimpleCollection collection = connection.get()) {
            // define unique attributes
            Map<String, String> attributes = new HashMap();
            attributes.put("uuid", "42");

            // create and forget
            collection.createItem("My Item", "secret", attributes);

            // find by attributes
            List<String> items = collection.getItems(attributes).get();
            assertEquals(1, items.size());
            String item = items.get(0);

            char[] actual = collection.getSecret(item);
            assertEquals("secret", new String(actual));
            assertEquals("My Item", collection.getLabel(item));
            assertEquals("42", collection.getAttributes(item).get().get("uuid"));

            collection.deleteItem(item);
            collection.delete();
        } catch (NoSuchElementException | IOException e) {
            // something went wrong
        } // clears automatically all session secrets in memory

    }
}
