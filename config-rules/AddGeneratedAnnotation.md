# `AddGeneratedAnnotation`

Lombok can be configured to add `@lombok.Generated` annotations to all generated nodes where possible. It's very useful for tools like JaCoCo, or other checkers and code coverage tools.

To enable this functionality, set one of these properties to `true`:

* `lombok.addLombokGeneratedAnnotation`
* `lombok.addJavaxGeneratedAnnotation`
* `lombok.addGeneratedAnnotation` (deprecated)

If you want to disable this functionality intentionally, set any of the properties listed earlier to `false`.

See [https://projectlombok.org/features/configuration](https://projectlombok.org/features/configuration).
