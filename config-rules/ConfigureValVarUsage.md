# `ConfigureValUsage`, `ConfigureVarUsage`

Lombok’s `val` and `var` can break static analysis tools
(see SonarQube's [SONARJAVA-4074](https://sonarsource.atlassian.net/browse/SONARJAVA-4074))
and occasionally leads to compilation exceptions that are very hard to debug.

Since Java 10, the built-in `var` keyword covers the only mainstream use case, making Lombok’s alternatives unnecessary.

We don't want to force any specific solution,
so this check just validates that `lombok.val.flagUsage` and `lombok.var.flagUsage` settings are
explicitly set to any value (`error`, `warning`, `allow`).

Our recommendation is to use `lombok.val.flagUsage = warning` + `lombok.var.flagUsage = warning`.

See [https://projectlombok.org/features/val](https://projectlombok.org/features/val)
and [https://projectlombok.org/features/var](https://projectlombok.org/features/var).
