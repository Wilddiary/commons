<h1 align="center" style="font-weight: bold; margin-top: 20px; margin-bottom: 20px;">commons</h1>

<p align="center">

  <img alt="Github Build" src="https://img.shields.io/github/actions/workflow/status/Wilddiary/commons/maven-release.yml" />
  <img alt="Synk Vulnerabilities" src="https://img.shields.io/snyk/vulnerabilities/github/Wilddiary/commons" />
  <img alt="GitHub Language Count" src="https://img.shields.io/github/languages/count/Wilddiary/commons" />
  <img alt="GitHub Top Language" src="https://img.shields.io/github/languages/top/Wilddiary/commons" />
  <img alt="GitHub Repo Size" src="https://img.shields.io/github/repo-size/Wilddiary/commons" />
  <img alt="GitHub File Count" src="https://img.shields.io/github/directory-file-count/Wilddiary/commons" />
  <img alt="GitHub Issues" src="https://img.shields.io/github/issues/Wilddiary/commons" />
  <img alt="GitHub Closed Issues" src="https://img.shields.io/github/issues-closed/Wilddiary/commons" />
  <img alt="GitHub Pull Requests" src="https://img.shields.io/github/issues-pr/Wilddiary/commons" />
  <img alt="GitHub Closed Pull Requests" src="https://img.shields.io/github/issues-pr-closed/Wilddiary/commons" />
  <img alt="GitHub Release" src="https://img.shields.io/github/v/release/Wilddiary/commons?date_order_by=created_at&sort=date" />
  <img alt="GitHub Tag" src="https://img.shields.io/github/v/tag/Wilddiary/commons" />
  <img alt="GitHub Contributors" src="https://img.shields.io/github/contributors/Wilddiary/commons" />
  <img alt="GitHub Last Commit" src="https://img.shields.io/github/last-commit/Wilddiary/commons" />
  <img alt="GitHub Commit Activity (Week)" src="https://img.shields.io/github/commit-activity/w/Wilddiary/commons" />
  <img alt="GitHub Commit Activity (Month)" src="https://img.shields.io/github/commit-activity/m/Wilddiary/commons" />
  <img alt="GitHub Commit Activity (Year)" src="https://img.shields.io/github/commit-activity/y/Wilddiary/commons" />
  <img alt="Github License" src="https://img.shields.io/github/license/Wilddiary/commons" />
  <img alt="Forks" src="https://img.shields.io/github/forks/Wilddiary/commons" />
  <img alt="Followers" src="https://img.shields.io/github/followers/Wilddiary" />
  <img alt="Discussions" src="https://img.shields.io/github/discussions/Wilddiary/commons" />

</p>

Repository for common reusable stuff. Its a Spring library module. Packages the following components.
1. Audit Support
2. Thread pools metrics that includes submitted, completed, failed and rejected tasks counts.
3. `@NoHtml` field validator
4. `@Generated` custom marker interface for excluding generated classes from jacoco code coverage calculation.

# Audit Support
Allows annotating auditable public methods with [@Audit](/src/main/java/com/wilddiary/commons/audit/Audit.java) to automatically trigger audit logging before, after and/or on failure of the operation. 
<br><br>To use this feature, annotate the application class with [@EnableAudit](/src/main/java/com/wilddiary/commons/audit/EnableAudit.java). This enables audits with the default [ConsoleAuditor](/src/main/java/com/wilddiary/commons/audit/impl/ConsoleAuditor.java) implementation that logs the audits to console.
<br><br>A custom auditor can be implemented and plugged in as per the need. To plug-in a custom auditor, export an [Auditor](/src/main/java/com/wilddiary/commons/audit/Auditor.java) bean with custom implementation in your configuration and mark it as the primary bean. 
<br><br>Below is an example to demonstrate the usage. In the example, acess to the resource at path `/greeeting` is audited after the successful execution of the ReST call if the condition specified in the `postCondition` is satisfied.

```java
@RestController
@RequestMapping("/greeting")
public class GreetingController {

    @GetMapping
    @Audit( action="'read'",
            object="'Greeting resource'",
            path="'/greeting'",
            post = "'Resource <greeting> was accessed with arguments - ' + #arg0 ",
            postCondition = "#result != null")
    public String sayHello(@RequestParam String name) {
       return "Hello " + name + "!";
    }
}
```


# NoHtml validator
Hibernate's `@SafeHtml` validator verifies the annotated fields do not contain malicious HTML content. However, the `Safehtml` validator has been removed from Hibernate validator's latest releases. `NoHtml` validator is a replacement for Hibernate's `SafeHtml` validator.


# ThreadPool metrics
Provides a reusable extension to the Micrometer classes to collect and register metrics for thread pools. Micrometer lacks support for submitted, failed and rejected execution task metrics that this extension provides. The extension can be used as follows.

```java
    ThreadPoolExecutor myExecutorPool =
        new TrackingThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            0L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueCapacity),
            new CustomizableThreadFactory("MyMonitoredThreadPool"),
            RejectedExecutionHandlerPolicy.CALLER_RUNS);

    // register the metrics
    ExecutorMetrics metrics =
        new ExecutorMetrics(
            myExecutorPool,
            "MyExecutorService",
            DEFAULT_METRICS_PREFIX,
            Collections.emptyList());
    metrics.bindTo(metricsRegistry);
```

# Build
This is a maven project. To build the project and install the build artifacts to the local maven repository, run the following command from the project root directory.

> `./mvnw clean install`

The build enforces some basics quality standards through build plug-ins. The build runs the Java linter, static code analysis, software composition analysis, tests and coverage check by default. Build fails if any of enforced quality thresholds are breached. It is advised not to skip any of the checks. However, if you have to then below listed java *system properties* help you do it.

1. `-Dcheckstyle.skip=true` - Skips static code analysis
2. `-Ddependency-check.skip=true` - Skips software composition analysis
3. `-DskipTests` - Skips tests
4. `-Djacoco.skip=true` - Skips code coverage check
5. `-Dgpg.skip=true` - Skips gpg signing for local builds, if needed.

<br>

