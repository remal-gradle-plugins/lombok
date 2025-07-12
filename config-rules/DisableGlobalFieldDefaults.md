# `DisableGlobalFieldDefaults`

Since Lombok 1.16.8,
simply having a `lombok.config` entry of `lombok.fieldDefaults.defaultPrivate = true` (or, analogously, `defaultFinal`)
is enough to modify every source file that is affected by that configuration,
even if said source file has absolutely no trace whatsoever of lombok anything inside it.

We agree with Lombok authors that it is too much magic.
From our experience, it leads to unexpected issues like package-private field is not available in tests.

Our recommendation is to disable `lombok.fieldDefaults.defaultPrivate` and `lombok.fieldDefaults.defaultFinal`
by not setting these settings or settings explicitly to `false`.

See [https://projectlombok.org/features/experimental/FieldDefaults](https://projectlombok.org/features/experimental/FieldDefaults).
