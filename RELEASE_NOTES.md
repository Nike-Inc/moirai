# Moirai Release Notes

## Moirai 1.1.0

* Deprecate `WhitelistedUsersConfigDecider` and `TypesafeConfigDecider.WHITELISTED_USERS` in favor of new names: `EnabledUsersConfigDecider` and `TypesafeConfigDecider.ENABLED_USERS`

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
if (featureChecker.isFeatureEnabled("random.magicnumber", FeatureCheckInput.forUser(identity.upmId))) {
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
