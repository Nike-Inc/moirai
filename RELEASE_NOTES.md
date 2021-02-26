# Moirai Release Notes

## Moirai 2.1.0

* Adds a feature-group to adjust how `ProportionOfUsersConfigDecider` decides to hash users. Now the hash will be based on the feature group if it exists, and use the feature identifier if no feature group is defined. In typesafe-config this can be used by adding a `featureGroup=myFeatureGroupName` property within the feature. 

## Moirai 2.0.0

* When using a `ProportionOfUsersConfigDecider`, the hash will be based on the feature identifier and userId instead of just the userId.
  * This makes sure that different features don't always target the same set of users.
  * This will change which users are selected for a feature, so you should probably not update Moirai while you have a feature with only a partial proportion enabled.
* Removed deprecated `WhitelistedUsersConfigDecider` and corresponding `WHITELISTED_USERS`.
* Migrated `moirai-s3` to [AWS Java SDK V2](https://github.com/aws/aws-sdk-java-v2). You will need to create a S3Client using the SDK, which in turn will require you to depend on a synchronous SDK implementation such as `url-connection-client` or `apache-client`

## Moirai 1.3.1

* Use `getResourceAsStream` to read classpath resources so they can be resolved from jar files properly

## Moirai 1.3.0

* Adds `FeatureEnabledConfigDecider`, which is a decider that takes boolean value from the configuration and does not need input.

## Moirai 1.2.0

* Adds the `CachingS3ResourceLoader`, which is intended as a drop-in replacement for `S3ResourceLoader` but is aware
of ETags. This is to allow more frequent polling of the S3 Object without reading the whole object each time.

## Moirai 1.1.0

* Deprecated `WhitelistedUsersConfigDecider` and `TypesafeConfigDecider.WHITELISTED_USERS` in favor of new names: `EnabledUsersConfigDecider` and `TypesafeConfigDecider.ENABLED_USERS`
* Added support for deciding if a feature is enabled based on custom dimensions

### Example custom dimension usage in Scala

Creating the `FeatureFlagChecker`:
```scala
val featureFlagChecker = ConfigFeatureFlagChecker.forReloadableResource(
    resourceReloader,
    TypesafeConfigDecider.WHITELISTED_USERS
      .or(TypesafeConfigDecider.PROPORTION_OF_USERS)
      .or(TypesafeConfigDecider.enabledCustomStringDimension("country", "enabledCountries"))
)
```

Using the `FeatureFlagChecker`:
```scala
val featureCheckInput = FeatureCheckInput.forUser(auth.userId).withAdditionalDimension("country", "Belgium")
if (featureChecker.isFeatureEnabled("random.magicnumber", featureCheckInput)) {
  "42" 
} else {
  calculateRealNumber()
}
```

Example config file:
```
moirai {
  random.magicnumber {
    whitelistedUserIds = [
      8675309
      1234
    ]
    enabledProportion = 0.01
    enabledCountries = ["Belgium", "Peru"]
  }
}
```

## Moirai 1.0.1

* POM only challenges for publishing to Maven Central

## Moirai 1.0.0

Initial release of Moirai

* supports classpath resources, file-system files, and S3 objects to load resources
* supports typesafe-config for the configuration format
* provides the ability to decide if a feature is enabled based on a whitelist or a proportion of users

### Example usage in Scala

Creating the `FeatureFlagChecker`:
```scala
val fileSupplier = FileResourceLoaders.forFile(new File("/path/to/conf/file/moirai.conf"))
val configSupplier = supplierAndThen(fileSupplier, TypesafeConfigReader.FROM_STRING)

val resourceReloader = ResourceReloader.withDefaultSettings(
  Suppliers.async(configSupplier),
  configSupplier.get()
)

val featureFlagChecker = ConfigFeatureFlagChecker.forReloadableResource(
    resourceReloader,
    TypesafeConfigDecider.WHITELISTED_USERS.or(TypesafeConfigDecider.PROPORTION_OF_USERS)
)
```

Using the `FeatureFlagChecker`:
```scala
if (featureChecker.isFeatureEnabled("random.magicnumber", FeatureCheckInput.forUser(auth.userId))) {
  "42" 
} else {
  calculateRealNumber()
}
```

Example config file:
```
moirai {
  random.magicnumber {
    whitelistedUserIds = [
      8675309
      1234
    ]
    enabledProportion = 0.01
  }
}
```
