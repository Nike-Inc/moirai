# Moirai Release Notes

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
