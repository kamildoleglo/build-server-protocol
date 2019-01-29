package ch.epfl.scala.bsp.gen

import java.{lang, util}

import ch.epfl.scala.bsp4j._
import org.scalacheck._
import Arbitrary.arbitrary

import scala.collection.JavaConverters._

object Bsp4jGenerators {

  /** Generate an uri string. */
  lazy val genUri: Gen[String] = for {
    schema <- Gen.listOfN(5, Gen.alphaChar).map(_.mkString)
    host <- Gen.identifier
    port <- Gen.choose(80, 1024)
    segmentCount <- Gen.choose(0, 10)
    segments <- Gen.listOfN(segmentCount, Gen.identifier)
  } yield s"$schema://$host:$port/${segments.mkString("/")}"

  lazy val genBspConnectionDetails: Gen[BspConnectionDetails] = for {
    name <- arbitrary[String]
    argv <- arbitrary[String].list
    version <- arbitrary[String]
    bspVersion <- arbitrary[String]
    languages <- genLanguageId.list
  } yield new BspConnectionDetails(name, argv, version, bspVersion, languages)

  lazy val genBuildClientCapabilities: Gen[BuildClientCapabilities] = for {
    languageIds <- genLanguageId.list
  } yield new BuildClientCapabilities(languageIds)

  val genBuildServerCapabilities: Gen[BuildServerCapabilities] = for {
    compileProvider <- genCompileProvider.nullable
    testProvider <- genTestProvider.nullable
    inverseSourcesProvider <- BoxedGen.boolean.nullable
    dependencySourcesProvider <- BoxedGen.boolean.nullable
    resourcesProvider <- BoxedGen.boolean.nullable
    buildTargetChangedProvider <- BoxedGen.boolean.nullable
  } yield {
    val capabilities = new BuildServerCapabilities()
    capabilities.setCompileProvider(compileProvider)
    capabilities.setTestProvider(testProvider)
    capabilities.setInverseSourcesProvider(inverseSourcesProvider)
    capabilities.setDependencySourcesProvider(dependencySourcesProvider)
    capabilities.setResourcesProvider(resourcesProvider)
    capabilities.setBuildTargetChangedProvider(buildTargetChangedProvider)
    capabilities
  }

  lazy val genBuildTarget: Gen[BuildTarget] = for {
    id <- genBuildTargetIdentifier
    tags <- genBuildTargetTag.list
    languageIds <- genLanguageId.list
    dependencies <- genBuildTargetIdentifier.list
    capabilities <- genBuildTargetCapabilities
    displayName <- arbitrary[String].nullable
    baseDirectory <- genUri.nullable
  } yield {
    val buildTarget = new BuildTarget(id, tags, languageIds, dependencies, capabilities)
    buildTarget.setDisplayName(displayName)
    buildTarget.setBaseDirectory(baseDirectory)
    buildTarget
  }

  lazy val genBuildTargetCapabilities: Gen[BuildTargetCapabilities] = for {
    canCompile <- arbitrary[Boolean]
    canTest <- arbitrary[Boolean]
    canRun <- arbitrary[Boolean]
  } yield new BuildTargetCapabilities(canCompile, canTest, canRun)

  lazy val genBuildTargetEvent: Gen[BuildTargetEvent] = for {
    uri <- genUri
    kind <- genBuildTargetEventKind.nullable
  } yield {
    val event = new BuildTargetEvent(uri)
    event.setKind(kind)
    event.setData(null) // TODO build target event data?
    event
  }

  lazy val genBuildTargetEventKind: Gen[BuildTargetEventKind] = Gen.oneOf(BuildTargetEventKind.values)

  lazy val genBuildTargetIdentifier: Gen[BuildTargetIdentifier] = for {
    uri <- genUri
  } yield new BuildTargetIdentifier(uri)

  lazy val genBuildTargetTag: Gen[String] = Gen.oneOf(
    BuildTargetTag.APPLICATION,
    BuildTargetTag.BENCHMARK,
    BuildTargetTag.INTEGRATION_TEST,
    BuildTargetTag.LIBRARY,
    BuildTargetTag.NO_IDE,
    BuildTargetTag.TEST
  )

  lazy val genCleanCacheParams: Gen[CleanCacheParams] = for {
    targets <- genBuildTargetIdentifier.list
  } yield new CleanCacheParams(targets)

  lazy val genCleanCacheResult: Gen[CleanCacheResult] = for {
    message <- arbitrary[String].nullable
    cleaned <- arbitrary[Boolean]
  } yield new CleanCacheResult(message, cleaned)

  lazy val genCompileParams: Gen[CompileParams] = for {
    targets <- genBuildTargetIdentifier.list
  } yield new CompileParams(targets)

  lazy val genCompileProvider: Gen[CompileProvider] = for {
    languageIds <- genLanguageId.list
  } yield new CompileProvider(languageIds)

  lazy val genCompileReport: Gen[CompileReport] = for {
    target <- genBuildTargetIdentifier
    errors <- arbitrary[Int]
    warnings <- arbitrary[Int]
    time <- BoxedGen.long.nullable
  } yield {
    val report = new CompileReport(target, errors, warnings)
    report.setTime(time)
    report
  }

  lazy val genCompileResult: Gen[CompileResult] = for {
    statusCode <- genStatusCode
  } yield new CompileResult(statusCode)

  lazy val genCompileTask: Gen[CompileTask] = for {
    target <- genBuildTargetIdentifier
  } yield new CompileTask(target)

  lazy val genDependencySourcesItem: Gen[DependencySourcesItem] = for {
    target <- genBuildTargetIdentifier
    sources <- genUri.list
  } yield new DependencySourcesItem(target, sources)

  lazy val genDependencySourcesParams: Gen[DependencySourcesParams] = for {
    targets <- genBuildTargetIdentifier.list
  } yield new DependencySourcesParams(targets)

  lazy val genDependencySourcesResult: Gen[DependencySourcesResult] = for {
    items <- genDependencySourcesItem.list
  } yield new DependencySourcesResult(items)

  lazy val genDiagnostic: Gen[Diagnostic] = for {
    range <- genRange
    message <- arbitrary[String]
    severity <- genDiagnosticSeverity.nullable
    code <- arbitrary[String].nullable
    source <- arbitrary[String].nullable
    relatedInformation <- genDiagnosticRelatedInformation.nullable
  } yield {
    val diagnostic = new Diagnostic(range, message)
    diagnostic.setSeverity(severity)
    diagnostic.setCode(code)
    diagnostic.setSource(source)
    diagnostic.setRelatedInformation(relatedInformation)
    diagnostic
  }

  lazy val genDiagnosticRelatedInformation: Gen[DiagnosticRelatedInformation] = for {
    location <- genLocation
    message <- arbitrary[String]
  } yield new DiagnosticRelatedInformation(location, message)

  lazy val genDiagnosticSeverity: Gen[DiagnosticSeverity] = Gen.oneOf(DiagnosticSeverity.values())

  lazy val genDidChangeBuildTarget: Gen[DidChangeBuildTarget] = for {
    events <- genBuildTargetEvent.list
  } yield new DidChangeBuildTarget(events)

  lazy val genFQN: Gen[String] = for {
    packages <- Gen.nonEmptyListOf(Gen.identifier)
    className <- genClassName
  } yield s"${packages.mkString(".")}.$className"

  lazy val genClassName: Gen[String] = for {
    initial <- Gen.alphaChar
    rest <- Gen.identifier
  } yield s"$initial$rest"

  lazy val genInitializeBuildParams: Gen[InitializeBuildParams] = for {
    displayName <- arbitrary[String]
    version <- arbitrary[String]
    bspVersion <- arbitrary[String]
    rootUri <- genUri
    capabilities <- genBuildClientCapabilities
  } yield new InitializeBuildParams(displayName, version, bspVersion, rootUri, capabilities)

  lazy val genInitializeBuildResult: Gen[InitializeBuildResult] = for {
    displayName <- arbitrary[String]
    version <- arbitrary[String]
    bspVersion <- arbitrary[String]
    capabilities <- genBuildServerCapabilities
  } yield new InitializeBuildResult(displayName, version, bspVersion, capabilities)

  lazy val genInverseSourcesParams: Gen[InverseSourcesParams] = for {
    textDocument <- genTextDocumentIdentifier
  } yield new InverseSourcesParams(textDocument)

  lazy val genInverseSourcesResult: Gen[InverseSourcesResult] = for {
    targets <- genBuildTargetIdentifier.list
  } yield new InverseSourcesResult(targets)

  lazy val genLanguageId: Gen[String] = Gen.oneOf("scala", "java")

  lazy val genLocation: Gen[Location] = for {
    uri <- genUri
    range <- genRange
  } yield new Location(uri, range)

  lazy val genLogMessageParams: Gen[LogMessageParams] = for {
    messageType <- genMessageType
    message <- arbitrary[String]
    task <- genTaskId.nullable
  } yield {
    val params = new LogMessageParams(messageType, message)
    params.setTask(task)
    params
  }

  lazy val genMessageType: Gen[MessageType] = Gen.oneOf(MessageType.values)

  lazy val genPosition: Gen[Position] = for {
    line <- arbitrary[Int]
    character <- arbitrary[Int]
  } yield new Position(line, character)

  lazy val genPublishDiagnosticsParams: Gen[PublishDiagnosticsParams] = for {
    textDocument <- genTextDocumentIdentifier
    buildTarget <- genBuildTargetIdentifier
    diagnostics <- genDiagnostic.list
    reset <- arbitrary[Boolean]
  } yield new PublishDiagnosticsParams(textDocument, buildTarget, diagnostics, reset)

  lazy val genRange: Gen[Range] = for {
    start <- genPosition
    end <- genPosition
  } yield new Range(start, end)

  lazy val genResourcesItem: Gen[ResourcesItem] = for {
    target <- genBuildTargetIdentifier
    resources <- genUri.list
  } yield new ResourcesItem(target, resources)

  lazy val genResourcesParams: Gen[ResourcesParams] = for {
    targets <- genBuildTargetIdentifier.list
  } yield new ResourcesParams(targets)

  lazy val genResourcesResult: Gen[ResourcesResult] = for {
    items <- genResourcesItem.list
  } yield new ResourcesResult(items)

  lazy val genRunParams: Gen[RunParams] = for {
    target <- genBuildTargetIdentifier
    arguments <- arbitrary[String].list.nullable
  } yield {
    val runParams = new RunParams(target)
    runParams.setArguments(arguments)
    runParams
  }

  lazy val genRunProvider: Gen[RunProvider] = for {
    languageIds <- genLanguageId.list
  } yield new RunProvider(languageIds)

  lazy val genRunResult: Gen[RunResult] = for {
    statusCode <- genStatusCode
  } yield new RunResult(statusCode)

  lazy val genSbtBuildTarget: Gen[SbtBuildTarget] = for {
    sbtVersion <- arbitrary[String]
    autoImports <- arbitrary[String].list
    classpath <- arbitrary[String].list
    scalaBuildTarget <- genScalaBuildTarget
    children <- genBuildTargetIdentifier.list
  } yield new SbtBuildTarget(sbtVersion, autoImports, classpath, scalaBuildTarget, children)

  lazy val genScalaBuildTarget: Gen[ScalaBuildTarget] = for {
    scalaOrganization <- arbitrary[String]
    scalaVersion <- arbitrary[String]
    scalaBinaryVersion <- arbitrary[String]
    platform <- genScalaPlatform
    jars <- arbitrary[String].list
  } yield new ScalaBuildTarget(scalaOrganization, scalaVersion, scalaBinaryVersion, platform, jars)

  lazy val genScalacOptionsItem: Gen[ScalacOptionsItem] = for {
    target <- genBuildTargetIdentifier
    options <- arbitrary[String].list
    classpath <- arbitrary[String].list
    classDirectory <- genUri
  } yield new ScalacOptionsItem(target, options, classpath, classDirectory)

  lazy val genScalacOptionsParams: Gen[ScalacOptionsParams] = for {
    targets <- genBuildTargetIdentifier.list
  } yield new ScalacOptionsParams(targets)

  lazy val genScalacOptionsResult: Gen[ScalacOptionsResult] = for {
    items <- genScalacOptionsItem.list
  } yield new ScalacOptionsResult(items)

  lazy val genScalaMainClass: Gen[ScalaMainClass] = for {
    className <- genClassName
    arguments <- arbitrary[String].list
    jvmOptions <- arbitrary[String].list
  } yield new ScalaMainClass(className, arguments, jvmOptions)

  lazy val genScalaMainClassesItem: Gen[ScalaMainClassesItem] = for {
    target <- genBuildTargetIdentifier
    classes <- genScalaMainClass.list
  } yield new ScalaMainClassesItem(target, classes)

  lazy val genScalaMainClassesParams: Gen[ScalaMainClassesParams] = for {
    targets <- genBuildTargetIdentifier.list
  } yield new ScalaMainClassesParams(targets)

  lazy val genScalaMainClassesResult: Gen[ScalaMainClassesResult] = for {
    items <- genScalaMainClassesItem.list
  } yield new ScalaMainClassesResult(items)

  lazy val genScalaPlatform: Gen[ScalaPlatform] = Gen.oneOf(ScalaPlatform.values())

  lazy val genScalaTestClassesItem: Gen[ScalaTestClassesItem] = for {
    target <- genBuildTargetIdentifier
    classes <- genFQN.list
  } yield new ScalaTestClassesItem(target, classes)

  lazy val genScalaTestClassesParams: Gen[ScalaTestClassesParams] = for {
    targets <- genBuildTargetIdentifier.list
  } yield new ScalaTestClassesParams(targets)

  lazy val genScalaTestClassesResult: Gen[ScalaTestClassesResult] = for {
    items <- genScalaTestClassesItem.list
  } yield new ScalaTestClassesResult(items)

  lazy val genScalaTestParams: Gen[ScalaTestParams] = for {
    items <- genScalaTestClassesItem.list.nullable
  } yield {
    val params = new ScalaTestParams()
    params.setTestClasses(items)
    params
  }

  lazy val genShowMessageParams: Gen[ShowMessageParams] = for {
    messageType <- genMessageType
    message <- arbitrary[String]
    taskId <- genTaskId.nullable
  } yield {
    val params = new ShowMessageParams(messageType, message)
    params.setTask(taskId)
    params
  }

  lazy val genSourceItem: Gen[SourceItem] = for {
    uri <- genUri
    isDirectory <- arbitrary[Boolean]
    generated <- arbitrary[Boolean]
  } yield new SourceItem(uri, isDirectory, generated)

  lazy val genSourcesItem: Gen[SourcesItem] = for {
    target <- genBuildTargetIdentifier
    sources <- genSourceItem.list
  } yield new SourcesItem(target, sources)

  lazy val genSourcesParams: Gen[SourcesParams] = for {
    targets <- genBuildTargetIdentifier.list
  } yield new SourcesParams(targets)

  lazy val genSourcesResult: Gen[SourcesResult] = for {
    items <- genSourcesItem.list
  } yield new SourcesResult(items)


  lazy val genStatusCode: Gen[StatusCode] = Gen.oneOf(StatusCode.values)

  lazy val genTaskDataKind: Gen[String] = Gen.oneOf(
    TaskDataKind.COMPILE_REPORT,
    TaskDataKind.COMPILE_TASK,
    TaskDataKind.TEST_FINISH,
    TaskDataKind.TEST_REPORT,
    TaskDataKind.TEST_START,
    TaskDataKind.TEST_TASK
  )

  lazy val genTaskFinishParams: Gen[TaskFinishParams] = for {
    taskId <- genTaskId
    status <- genStatusCode
    eventTime <- BoxedGen.long.nullable
    message <- arbitrary[String].nullable
    dataKind <- genTaskDataKind.nullable
  } yield {
    val params = new TaskFinishParams(taskId, status)
    params.setEventTime(eventTime)
    params.setMessage(message)
    params.setDataKind(dataKind)
    params.setData(null) // TODO data according to dataKind
    params
  }

  lazy val genTaskId: Gen[TaskId] = for {
    id <- arbitrary[String]
    parents <- arbitrary[String].list.nullable
  } yield {
    val taskId = new TaskId(id)
    taskId.setParents(parents)
    taskId
  }

  lazy val genTaskProgressParams: Gen[TaskProgressParams] = for {
    taskId <- genTaskId
    eventTime <- BoxedGen.long.nullable
    message <- arbitrary[String].nullable
    progress <- BoxedGen.long.nullable
    total <- BoxedGen.long.nullable
    unit <- arbitrary[String]
    dataKind <- genTaskDataKind.nullable
  } yield {
    val params = new TaskProgressParams(taskId)
    params.setEventTime(eventTime)
    params.setMessage(message)
    params.setProgress(progress)
    params.setTotal(total)
    params.setUnit(unit)
    params.setDataKind(dataKind)
    params.setData(null) // TODO data according to dataKind
    params
  }

  lazy val genTaskStartParams: Gen[TaskStartParams] = for {
    taskId <- genTaskId
    eventTime <- BoxedGen.long.nullable
    message <- arbitrary[String].nullable
    dataKind <- genTaskDataKind.nullable
  } yield {
    val params = new TaskStartParams(taskId)
    params.setEventTime(eventTime)
    params.setMessage(message)
    params.setDataKind(dataKind)
    params.setData(null) // TODO data according to dataKind
    params
  }

  lazy val genTestFinished: Gen[TestFinished] = for {
    displayName <- arbitrary[String]
    status <- genTestStatus
    location <- genLocation.nullable
    message <- arbitrary[String].nullable
  } yield {
    val testFinished = new TestFinished(displayName, status)
    testFinished.setDisplayName(displayName)
    testFinished.setLocation(location)
    testFinished.setMessage(message)
    testFinished.setData(null) // TODO data according to dataKind
    testFinished
  }

  lazy val genTestParams: Gen[TestParams] = for {
    targets <- genBuildTargetIdentifier.list
    arguments <- arbitrary[String].list.nullable
  } yield {
    val params = new TestParams(targets)
    params.setArguments(arguments)
    params.setData(null) // TODO data according to dataKind
    params
  }

  lazy val genTestProvider: Gen[TestProvider] = for {
    languageIds <- genLanguageId.list
  } yield new TestProvider(languageIds)

  lazy val genTestReport: Gen[TestReport] = for {
    target <- genBuildTargetIdentifier
    passed <- arbitrary[Int]
    failed <- arbitrary[Int]
    ignored <- arbitrary[Int]
    cancelled <- arbitrary[Int]
    skipped <- arbitrary[Int]
    time <- BoxedGen.long.nullable
  } yield {
    val report = new TestReport(target, passed, failed, ignored, cancelled, skipped)
    report.setTime(time)
    report
  }

  lazy val genTestResult: Gen[TestResult] = for {
    statusCode <- genStatusCode
    originId <- arbitrary[String]
  } yield {
    val result = new TestResult(statusCode)
    result.setOriginId(originId)
    result.setData(null) // TODO data according to dataKind
    result
  }

  lazy val genTestStarted: Gen[TestStarted] = for {
    displayName <- arbitrary[String]
    location <- genLocation.nullable
  } yield {
    val testStarted = new TestStarted(displayName)
    testStarted.setLocation(location)
    testStarted
  }

  lazy val genTestStatus: Gen[TestStatus] = Gen.oneOf(TestStatus.values)

  lazy val genTestTask: Gen[TestTask] = for {
    target <- genBuildTargetIdentifier
  } yield new TestTask(target)

  lazy val genTextDocumentIdentifier: Gen[TextDocumentIdentifier] = for {
    uri <- genUri
  } yield new TextDocumentIdentifier(uri)

  lazy val genWorkspaceBuildTargetsResult: Gen[WorkspaceBuildTargetsResult] = for {
    targets <- genBuildTarget.list
  } yield new WorkspaceBuildTargetsResult(targets)


  implicit class GenExt[T](gen: Gen[T]) {
    def optional: Gen[Option[T]] = Gen.option(gen)
    def nullable(implicit ev: Null <:< T): Gen[T] = Gen.option(gen).map(g => g.orNull)
    def list: Gen[util.List[T]] = Gen.listOf(gen).map(_.asJava)
  }

  private object BoxedGen {
    def boolean: Gen[lang.Boolean] = arbitrary[Boolean].map(Boolean.box)
    def int: Gen[lang.Integer] = arbitrary[Int].map(Int.box)
    def long: Gen[lang.Long] = arbitrary[Long].map(Long.box)
  }

}