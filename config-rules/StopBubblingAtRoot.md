# `StopBubblingAtRoot`

The root project or the repository root doesn't contain `lombok.config` file with `config.stopBubbling = true` line.

Take a look at this file system structure:

```
/
  home/
    user/
      lombok.config
      project/
        .git/
        build.gradle
        src/
```

As you can see, a project in `/home/user/project/` directory doesn't have `lombok.config` file. So, when a user builds it, `/home/user/lombok.config` file influences the build because Lombok traverses all directories up to the system root (`/` on Linux) until a `lombok.config` file with `config.stopBubbling = true` is found.

Such a situation makes the build system dependent, leading to inconsistencies and much harder collaboration among team members.

To prevent such situations, create a `lombok.config` file with `config.stopBubbling = true` line in the repository root.

See [https://projectlombok.org/features/configuration](https://projectlombok.org/features/configuration).
