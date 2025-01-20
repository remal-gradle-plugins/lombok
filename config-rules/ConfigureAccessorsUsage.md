# `ConfigureAccessorsUsage`

Fluent/chain accessors can break different libraries that rely on Java Beans Specification. That is why using `@lombok.experimental.Accessors` is not recommended.

We don't want to force any specific solution, so this check just validates that `lombok.accessors.flagUsage` setting explicitly set to any value (`error`, `warning`, `allow`).

Our recommendation if to use `lombok.accessors.flagUsage = warning`.

See [https://projectlombok.org/features/experimental/Accessors](https://projectlombok.org/features/experimental/Accessors).
