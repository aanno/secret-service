# Secret Service

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/61897aae6b5842f8a35ec81ca02112e3)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=swiesend/secret-service&amp;utm_campaign=Badge_Grade)
[![Maven Central](https://img.shields.io/maven-central/v/de.swiesend/secret-service.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22de.swiesend%22%20AND%20a:%22secret-service%22)

A Java library for storing secrets in a keyring over the D-Bus.

The library is conform to the freedesktop.org
[Secret Service API 0.2](https://specifications.freedesktop.org/secret-service/) and thus compatible with gnome linux systems.

The Secret Service itself is usually implemented by the [`gnome-keyring`](https://wiki.gnome.org/action/show/Projects/GnomeKeyring).

This library can be seen as the functional equivalent to the [`libsecret`](https://wiki.gnome.org/Projects/Libsecret) C library.

see: [Secret Storage Specification](https://www.freedesktop.org/wiki/Specifications/secret-storage-spec/)

## Usage

The library provides a simplified high-level API, which sends only transport encrypted secrets over the D-Bus.

```java
package org.freedesktop.secret.simple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Example {

    @Test
    @DisplayName("Create a password in the user's default collection ('/org/freedesktop/secrets/aliases/default').")
    public void createPasswordInDefaultCollection() throws IOException {
        try (SimpleCollection collection = new SimpleCollection()) {
            String item = collection.createItem("My Item", "secret");
            
            char[] actual = collection.getSecret(item);
            assertEquals("secret", new String(actual));
            assertEquals("My Item", collection.getLabel(item));
            
            collection.deleteItem(item);
        } // clears automatically all session secrets in memory
    }

    @Test
    @DisplayName("Create a password in a non-default collection ('/org/freedesktop/secrets/collection/xxxx').")
    public void createPasswordInNonDefaultCollection() throws IOException {
        try (SimpleCollection collection = new SimpleCollection("My Collection", "super secret")) {
            String item = collection.createItem("My Item", "secret");
            
            char[] actual = collection.getSecret(item);
            assertEquals("secret", new String(actual));
            assertEquals("My Item", collection.getLabel(item));
            
            collection.deleteItem(item);
            collection.delete();
        } // clears automatically all session secrets in memory
    }

    @Test
    @DisplayName("Create a password with additional attributes.")
    public void createPasswordWithAttributes() throws IOException {
        try (SimpleCollection collection = new SimpleCollection("My Collection", "super secret")) {
            // define unique attributes
            Map<String, String> attributes = new HashMap();
            attributes.put("uuid", "42");

            // create and forget
            collection.createItem("My Item", "secret", attributes);

            // find by attributes
            List<String> items = collection.getItems(attributes);
            assertEquals(1, items.size());
            String item = items.get(0);
            
            char[] actual = collection.getSecret(item);
            assertEquals("secret", new String(actual));
            assertEquals("My Item", collection.getLabel(item));
            assertEquals("42", collection.getAttributes(item).get("uuid"));

            collection.deleteItem(item);
            collection.delete();
        } // clears automatically all session secrets in memory
    }
}
```

For the details of the transport encryption see: [Transfer of Secrets](https://specifications.freedesktop.org/secret-service/ch07.html),
[Transport Encryption Example](src/test/java/org/freedesktop/secret/integration/IntegrationTest.java)

The low-level API gives access to all defined Methods, Properties and Signals of the Secret Service interface:

* [Service](src/main/java/org/freedesktop/secret/Service.java)
* [Collection](src/main/java/org/freedesktop/secret/Collection.java)
* [Item](src/main/java/org/freedesktop/secret/Item.java)
* [Session](src/main/java/org/freedesktop/secret/Session.java)
* [Prompt](src/main/java/org/freedesktop/secret/Prompt.java)

For examples on API usage checkout the tests:

* [ServiceTest](src/test/java/org/freedesktop/secret/ServiceTest.java)
* [CollectionTest](src/test/java/org/freedesktop/secret/CollectionTest.java)
* [ItemTest](src/test/java/org/freedesktop/secret/ItemTest.java)
* [SessionTest](src/test/java/org/freedesktop/secret/SessionTest.java)
* [PromptTest](src/test/java/org/freedesktop/secret/PromptTest.java)

## Security Issues

### CVE-2018-19358 (Vulnerability)

There is a current investigation on the behaviour of the Secret Service API:

* [CVE-2018-19358](https://nvd.nist.gov/vuln/detail/CVE-2018-19358)
* [GNOME Keyring Secret Service API Login Credentials Retrieval Vulnerability](https://tools.cisco.com/security/center/viewAlert.x?alertId=59179)
