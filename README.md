**Tested on Java LTS versions from <!--property:java-runtime.min-version-->11<!--/property--> to <!--property:java-runtime.max-version-->24<!--/property-->.**

**Tested on Gradle versions from <!--property:gradle-api.min-version-->7.0<!--/property--> to <!--property:gradle-api.max-version-->9.0.0-rc-2<!--/property-->.**

# `name.remal.lombok` plugin

[![configuration cache: supported from v2.2](https://img.shields.io/static/v1?label=configuration%20cache&message=supported+from+v2.2&color=success)](https://docs.gradle.org/current/userguide/configuration_cache.html)

Usage:

<!--plugin-usage:name.remal.lombok-->
```groovy
plugins {
    id 'name.remal.lombok' version '3.1.0'
}
```
<!--/plugin-usage-->

&nbsp;

This plugin adds [Lombok annotation processor](https://mvnrepository.com/artifact/org.projectlombok/lombok/1.18.38) to `compileOnly` and `annotationProcessor` configurations for every [`SourceSet`](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/SourceSet.html).

This is done via creating `lombok` configuration and making `compileOnly` and `annotationProcessor` configurations extend it.

## Plugin configuration

The plugin can be configured via `lombok` extension:

```groovy
lombok {
  lombokVersion = '1.18.38' // Lombok version
}
```

The used Lombok version can also be configured via constraints. This is useful for tools that automatically update dependencies (like [Renovate](https://renovatebot.com/)).

```groovy
dependencies {
  constraints {
    lombok 'org.projectlombok:lombok:1.18.38'
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

The MapStruct annotation processor is always put before the Lombok annotation processor. It is done to prevent [this MapStruct dependency ordering issue](https://github.com/mapstruct/mapstruct/issues/1581). The issue is marked as fixed, but it is still reproducible.

This logic is executed whether MapStruct is used in the project or not.

## Micronaut compatibility

The Lombok annotation processor should always be before the Micronaut annotation processor to prevent issues like [this](https://github.com/micronaut-projects/micronaut-core/issues/218). This plugin automatically applies a fix described [here](https://github.com/micronaut-projects/micronaut-core/issues/218#issuecomment-397584046).

## Compatibility with other annotation processors

By default, the Lombok annotation processor is always put before any other annotation processors.

Exceptions:

* MapStruct - Lombok is always put after

If there is a compatibility issue and the Lombok annotation processor has to be after some processor (like MapStruct), please report it.

# Migration guide

## Version 2.* to 3.*

The minimum Java version is 11 (from 8).
The minimum Gradle version is 7.0 (from 6.7).

## Version 1.* to 2.*

* Package name was changed from `name.remal.gradleplugins.lombok` to `name.remal.gradle_plugins.lombok`.
