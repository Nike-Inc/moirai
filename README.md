# Moirai

[ ![Download](https://api.bintray.com/packages/nike/maven/moirai-core/images/download.svg) ](https://bintray.com/nike/maven/moirai-core/_latestVersion)
[![][travis img]][travis]
[![Code Coverage](https://img.shields.io/codecov/c/github/Nike-Inc/moirai/master.svg)](https://codecov.io/github/Nike-Inc/moirai?branch=master)
 [![][license img]][license]

[Moirai](https://en.wikipedia.org/wiki/Moirai) (or: the Fates) controls people's destiny. Moirai is a feature-flag and resource-reloading library for the JVM (requires Java 8 or above).

This project provides libraries that can be used to determine if a feature should be exposed to a user. This feature-flagging can be used for alpha or beta testing, load control, gradual rollout, A/B testing, etc.

It consists of support for reloading a configuration resource periodically in the background, and pluggable interfaces for how resources are loaded, what their content is, and how the configuration is used to decide if a feature is enabled for a user. Modules are then provided for reusable implementations of these components.

The resource reloading can be used independently of the feature-flagging as a light-weight configuration reloading library.

## Usage

The provided example is in Java, but Moirai should be easy to use in any JVM language.

Creates a `FeatureFlagChecker` that reloads a Typesafe config file that controls whether `getNumber` returns a hard-coded number or calls `calculateRealNumber`:
```java
import com.nike.moirai.ConfigFeatureFlagChecker;
import com.nike.moirai.FeatureCheckInput;
import com.nike.moirai.FeatureFlagChecker;
import com.nike.moirai.Suppliers;
import com.nike.moirai.resource.FileResourceLoaders;
import com.nike.moirai.resource.reload.ResourceReloader;
import com.nike.moirai.typesafeconfig.TypesafeConfigDecider;
import com.nike.moirai.typesafeconfig.TypesafeConfigReader;
import com.typesafe.config.Config;

import java.io.File;
import java.util.function.Supplier;

import static com.nike.moirai.Suppliers.supplierAndThen;

public class Usage {
    Supplier<String> fileSupplier = FileResourceLoaders.forFile(new File("/path/to/conf/file/moirai.conf"));
    Supplier<Config> configSupplier = supplierAndThen(fileSupplier, TypesafeConfigReader.FROM_STRING);

    ResourceReloader<Config> resourceReloader = ResourceReloader.withDefaultSettings(
        Suppliers.async(configSupplier),
        configSupplier.get()
    );

    FeatureFlagChecker featureFlagChecker = ConfigFeatureFlagChecker.forReloadableResource(
        resourceReloader,
        TypesafeConfigDecider.ENABLED_USERS.or(TypesafeConfigDecider.PROPORTION_OF_USERS)
    );
    
    public int getNumber(String userIdentity) {
        if (featureFlagChecker.isFeatureEnabled("random.calculatenumber", FeatureCheckInput.forUser(userIdentity))) {
            return calculateRealNumber();
        } else {
            return 42;
        }
    }
    
    public int calculateRealNumber() {
        // calculate a value
        ...
    }
}
```

Example config file:
```
moirai {
  random.calculatenumber {
    enabledUserIds = [
      8675309
      1234
    ]
   enabledProportion = 0.01
  }
}
```

## Modules

* `moirai-core` provides base functionality and abstractions with no additional dependencies beyond the JDK
* `moirai-s3` provides a convenient `Supplier` for loading a text file from Amazon S3
* `moirai-typesafeconfig` provides both a utility to read a String as a Typesafe `Config` and `Predicate` implementations based on a convention for representing feature-flag settings as a `Config`.
* `moirai-riposte-example` provides an example of how one would go about using riposte to use the moirai's `ConfigFeatureFlagChecker`

## Components

### ResourceReloader

The `ResourceReloader` takes an arbitrary `Supplier` of some value, and periodically calls the `Supplier` and stores the resulting value. It's essentially a cache of one value that is periodically updated asynchronously. Any data type and data source can be used by providing the `Supplier`.

You can have as much or as little transformation of the raw resource data cached as you want by chaining behavior onto provided `Supplier`. 

### Suppliers

There are utilities in `Suppliers` for transforming both synchronous and asynchronous `Supplier` instances. Additional modules can provide a `Supplier` for loading data from some location (such an object in S3). `FileResourceLoaders` provides instances for reading from the file system or the classpath. Modules can also provide functions to be used with the `Suppliers` for reading raw data into a useful format (such as Typesafe `Config`). 

### FeatureFlagChecker

`FeatureFlagChecker` is a very simple interface that takes in a feature-identifier and a `FeatureCheckInput` and returns a boolean if the feature should be enabled given that input.

The `FeatureCheckInput` represents any data that might be used to make a feature-check decision. It has built-in support for a userId and a dateTime, and also allows custom dimensions to be added to fit whatever input decision criteria you need.

### Config

Two methods of support for making a feature flag decision based on the userId input are provided: `WhitelistedUsersConfigDecider` and `ProportionOfUsersConfigDecider`. These are generic abstractions that modules (such as `moirai-typesafeconfig`) can provide concrete implementations of for specific config formats. The abstraction for these "deciders" is a `Predicate`, which allows you to combine them using `and` or `or` to flexibly define your own rules for feature checking.

* `WhitelistedUsersConfigDecider` takes a concrete list of user identifiers from some config source and checks if it contains the input user
* `ProportionOfUsersConfigDecider` uses the `hashCode` of the input userId against a ratio provided by some config source. For example, if the config source provides a value of 0.9 for the feature identifier, then approximately 90% of the users will get the feature enabled from this decider. 

Another method for making a feature flag decision based on a boolean value in the configuration: `FeatureEnabledConfigDecider`. This decider does not take any input.

### ConfigFeatureFlagChecker

`ConfigFeatureFlagChecker` is where you put it all together. Given either a `ResourceReloader` or just a `Supplier` and a corresponding `Predicate`, it provides a `FeatureFlagChecker`.

While the example from the Usage section demonstrates the intended common pattern for combining these components together, Moirai is designed to be flexible and composable. So you can build your `ResourceReloader`/`Supplier` and your `Predicate` however you want, including with custom implementations, or you can even implement `FeatureFlagChecker` directly if desired.

<a name="license"></a>
## License

Moirai is released under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

[travis]:https://travis-ci.org/Nike-Inc/moirai
[travis img]:https://api.travis-ci.org/Nike-Inc/moirai.svg?branch=master

[license]:LICENSE.txt
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg
