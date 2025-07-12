# `ConfigureUtilityClassUsage`

Due to current Javac limitations, `@lombok.experimental.UtilityClass` doesn't work correctly with static imports.
Unfortunately, such compilation issues are very hard to debug,
so it would be better to completely forbid using `@lombok.experimental.UtilityClass`.

We don't want to force any specific solution,
so this check just validates that `lombok.utilityClass.flagUsage` setting
is explicitly set to any value (`error`, `warning`, `allow`).

Our recommendation is to use `lombok.utilityClass.flagUsage = error`.

See [https://projectlombok.org/features/experimental/UtilityClass](https://projectlombok.org/features/experimental/UtilityClass) and [https://github.com/rzwitserloot/lombok/issues/2044](https://github.com/rzwitserloot/lombok/issues/2044) (which is closed as "Won't Fix").
