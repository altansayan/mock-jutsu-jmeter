# Contributing to mock-jutsu JMeter Plugin

## Development Setup

```bash
git clone https://github.com/altansayan/mock-jutsu-jmeter.git
cd mock-jutsu-jmeter/jmeter-plugin
mvn test
```

Requirements: Java 17+, Maven 3.8+

## Adding a New Data Type

### 1. Add the generator logic

In the appropriate `generators/*.java` class, add a new `case` to the `generate()` switch:

```java
// e.g. in IdentityGen.java
case "my_new_type" -> myNewType(rng, locale);
```

Implement the method. **Zero external dependencies** — stdlib only.

### 2. Register the type

In `MockJutsuRegistry.java`, add the type string to the correct `Set<String>`:

```java
private static final Set<String> IDENTITY_TYPES = Set.of(
    ..., "my_new_type"   // add here
);
```

### 3. Update the category function description

In `functions/MockJutsu<Category>Function.java`, add the type to `typeDescription()`:

```java
return "... | my_new_type";
```

### 4. Write tests (TDD — tests first)

**Layer 1** — Algorithm unit test in `AlgorithmTest.java`:
```java
@RepeatedTest(20)
void myNewTypeIsValid() {
    String result = IdentityGen.generate("my_new_type", "TR");
    // assert format, length, checksum...
}
```

**Layer 2** — Smoke test in `MockJutsuRegistryTest.java`:  
Add the type string to the appropriate `@ValueSource` list.

### 5. Verify

```bash
mvn test   # all tests must pass
```

## Code Rules

- **Zero dependencies** — no external libraries, stdlib only
- **Java 17+** — switch expressions, `HexFormat`, `List.of()` are fine
- **Algorithm correctness** — every checksum must be verified by a unit test
- **No locale-unaware generation** — always accept and use the `locale` parameter
- **Thread-safe** — use `ThreadLocalRandom.current()`, never shared mutable state

## Commit Format

```
feat: add my_new_type generator
fix: correct IBAN check digit for FR locale
test: add 20 repeated tests for my_new_type
```

## Pull Request Checklist

- [ ] New type added to generator, registry, and category function
- [ ] Algorithm unit test (Layer 1) written and passing
- [ ] Smoke test (Layer 2) updated
- [ ] `mvn test` passes with 0 failures
- [ ] No external dependencies added to `pom.xml`
