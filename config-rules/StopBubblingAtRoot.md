# `StopBubblingAtRoot`

The root project or the repository root doesn't contain `lombok.config` file. Create such a file with `config.stopBubbling = true` line to make the build system independent.

Lombok traverses all directories up to the system root (`/` on Linux) until a `lombok.config` file with `config.stopBubbling = true` is found. This makes the build system dependant, as some developers can have their own `lombok.config` files in one of parent directories.

See [https://projectlombok.org/features/configuration](https://projectlombok.org/features/configuration).
