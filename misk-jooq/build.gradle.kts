plugins {
  kotlin("jvm")
  `java-library`
  
  // Needed to generate jooq test db classes
  id("org.flywaydb.flyway") version "9.1.2"
  id("nu.studer.jooq") version "7.1.1"
}

dependencies {
  implementation(Dependencies.guava)
  implementation(Dependencies.guice)
  implementation(Dependencies.javaxInject)
  implementation(Dependencies.jooq)
  implementation(Dependencies.kotlinxCoroutines)
  api(Dependencies.kotlinRetry)
  implementation(project(":misk"))
  implementation(project(":misk-core"))
  implementation(project(":misk-inject"))
  api(project(":misk-jdbc"))
  api(Dependencies.wispLogging)

  testImplementation(Dependencies.assertj)
  testImplementation(Dependencies.wispTimeTesting)
  testApi(project(":misk-testing"))
  testApi(project(":misk-jdbc-testing"))

  // Needed to generate jooq test db classes
  jooqGenerator(Dependencies.mysql)
}

// Needed to generate jooq test db classes
buildscript {
  dependencies {
    classpath("org.flywaydb:flyway-gradle-plugin:9.1.2")
    classpath(Dependencies.mysql)
  }
}

// Needed to generate jooq test db classes
flyway {
  url = "jdbc:mysql://localhost:3500/misk-jooq-test-codegen"
  user = "root"
  password = "root"
  schemas = arrayOf("jooq")
  locations = arrayOf("filesystem:${project.projectDir}/src/test/resources/db-migrations")
  sqlMigrationPrefix = "v"
}
// Needed to generate jooq test db classes
jooq {
  version.set("3.14.8")
  edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

  configurations {
    create("main") {
      generateSchemaSourceOnCompilation.set(false)
      jooqConfiguration.apply {
        jdbc.apply {
          driver = "com.mysql.cj.jdbc.Driver"
          url = "jdbc:mysql://localhost:3500/misk-jooq-test-codegen"
          user = "root"
          password = "root"
        }
        generator.apply {
          name = "org.jooq.codegen.KotlinGenerator"
          database.apply {
            name = "org.jooq.meta.mysql.MySQLDatabase"
            inputSchema = "jooq"
            outputSchema = "jooq"
            includes = ".*"
            excludes = "(.*?FLYWAY_SCHEMA_HISTORY)|(.*?schema_version)"
            recordVersionFields = "version"
          }
          generate.apply {
            isJavaTimeTypes = true
          }
          target.apply {
            packageName = "misk.jooq.testgen"
            directory   = "${project.projectDir}/src/test/generated/kotlin"
          }
        }
      }
    }
  }
}
// Needed to generate jooq test db classes
val generateJooq by project.tasks
generateJooq.dependsOn("flywayMigrate")

// Needed to generate jooq test db classes
// If you are using this as an example for your service, remember to add the generated code to your
// main source set instead of your tests as it is done below.
sourceSets.getByName("test").java.srcDirs
  .add(File("${project.projectDir}/src/test/generated/kotlin"))
