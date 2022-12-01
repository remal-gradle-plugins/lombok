**Min supported Gradle version: <!--property:gradle-api.min-version-->6.7<!--/property-->**

# `name.remal.lombok` plugin

[![configuration cache: not supported](https://img.shields.io/static/v1?label=configuration%20cache&message=not%20supported&color=critical)](https://docs.gradle.org/current/userguide/configuration_cache.html)

This plugin adds [Lombok annotation processor](https://mvnrepository.com/artifact/org.projectlombok/lombok/1.18.24) to `compileOnly` and `annotationProcessor` configurations for every [`SourceSet`](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/SourceSet.html).

This is done via creating `lombok` configuration and making `compileOnly` and `annotationProcessor` configurations extend it.

## Plugin configuration

The plugin can be configured via `lombok` extension:

```groovy
lombok {
  lombokVersion = '1.18.24' // Lombok version
}
```

The used Lombok version can also be configured via constraints. This is useful for tools that automatically update dependencies (like [Renovate](https://renovatebot.com/)).

```groovy
dependencies {
  constraints {
    lombok 'org.projectlombok:lombok:1.18.24'
  }
}
```

## Lombok config support

This plugin supports [the Lombok configuration system](https://projectlombok.org/features/configuration).

### Up-to-date checks for `JavaCompile` tasks

`lombok.config` files are added to input files for all [`JavaCompile`](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/compile/JavaCompile.html) tasks. So, if you change `lombok.config` file(s), your code will be recompiled.

### Lombok config validation

`lombok.config` files are validated via `validateLombokConfig` task. `check` task is configured to execute `validateLombokConfig`.

Supported rules can be found [here](config-rules).

The validation can be additionally configured:

```groovy
lombok {
  config {
    validate {
      disabledRules.add('AddGeneratedAnnotation') // Disable `AddGeneratedAnnotation` rule
    }
  }
}
```

### Lombok config generation

`lombok.config` file can be generated:

```groovy
lombok {
  config {
    generate {
      enabled = true // To enable generation
      set('lombok.addLombokGeneratedAnnotation', true) // Add `lombok.addLombokGeneratedAnnotation = true` line
      plus('lombok.copyableAnnotations', 'com.fasterxml.jackson.annotation.JsonProperty') // Add `lombok.copyableAnnotations += com.fasterxml.jackson.annotation.JsonProperty` line
      minus('lombok.copyableAnnotations', 'com.fasterxml.jackson.annotation.JsonProperty') // Add `lombok.copyableAnnotations -= com.fasterxml.jackson.annotation.JsonProperty` line
      clear('lombok.copyableAnnotations') // Add `clear lombok.copyableAnnotations` line
    }
  }
}
```

## Delombok

A `delombok` task is created for every [`SourceSet`](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/SourceSet.html).

The `javadoc` task is configured to process delomboked sources.

Delombok format con be configured this way:

```groovy
lombok {
  delombok {
    format {
      pretty = true // To use `--format=pretty`
      indent = '2' // To use `--format=indent:2`
      emptyLines = 'INDENT' // To use `--format=emptyLines:indent`
      finalParams = 'SKIP' // To use `--format=finalParams:skip`
      constructorProperties = 'GENERATE' // To use `--format=finalParams:generate`
      suppressWarnings = 'SKIP' // To use `--format=suppressWarnings:skip`
      generated = 'GENERATE' // To use `--format=generated:generate`
      danceAroundIdeChecks = 'SKIP' // To use `--format=danceAroundIdeChecks:skip`
      generateDelombokComment = 'GENERATE' // To use `--format=generateDelombokComment:generate`
      javaLangAsFQN = 'SKIP' // To use `--format=javaLangAsFQN:skip`
    }
  }
}
```

## MapStruct support

[`lombok-mapstruct-binding`](https://mvnrepository.com/artifact/org.projectlombok/lombok-mapstruct-binding/0.2.0) dependency is added to `annotationProcessor` configuration for every [`SourceSet`](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/SourceSet.html).

The Lombok annotation processor is always put at the end of annotation processors. It is done to prevent [this MapStruct dependency ordering issue](https://github.com/mapstruct/mapstruct/issues/1581). The issue is marked as fixed, but it is still reproducible.

This logic is executed whether MapStruct is used in the project or not.
