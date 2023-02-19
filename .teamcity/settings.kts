import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.nunit
import jetbrains.buildServer.configs.kotlin.failureConditions.BuildFailureOnText
import jetbrains.buildServer.configs.kotlin.failureConditions.failOnText
import jetbrains.buildServer.configs.kotlin.projectFeatures.buildReportTab
import jetbrains.buildServer.configs.kotlin.projectFeatures.githubIssues
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2022.10"

project {
    description = "Contains all other projects"

    features {
        buildReportTab {
            id = "PROJECT_EXT_1"
            title = "Code Coverage"
            startPage = "coverage.zip!index.html"
        }
        githubIssues {
            id = "PROJECT_EXT_7"
            displayName = "test1"
            repositoryURL = "https://github.com/fayeznasri/simple-astronomy-lib.git"
            authType = accessToken {
                accessToken = "credentialsJSON:97be40ef-9327-46a7-a124-7c5bfda0c4c6"
            }
        }
    }

    cleanup {
        baseRule {
            all(days = 365)
            history(days = 90)
            preventDependencyCleanup = false
        }
    }

    subProject(SimpleAstronomyLib)
}


object SimpleAstronomyLib : Project({
    name = "Simple Astronomy Lib"
    defaultTemplate = RelativeId("SimpleAstronomyLib_BuildTest")

    vcsRoot(SimpleAstronomyLib_HttpsGithubComFayeznasriSimpleAstronomyLibRefsHeadsMaster)

    buildType(SimpleAstronomyLib_Build)

    template(SimpleAstronomyLib_BuildTest)
    template(SimpleAstronomyLib_Template)
})

object SimpleAstronomyLib_Build : BuildType({
    name = "Build"

    buildNumberPattern = "1.%build.counter%"

    params {
        param("newParam", "env%")
        param("JDK_19_0", "full moon")
        checkbox("env.JDK_19_0", "",
                  checked = "true")
        param("env.MY_PARAMETER", "env%JDK_19_0%")
    }

    vcs {
        root(SimpleAstronomyLib_HttpsGithubComFayeznasriSimpleAstronomyLibRefsHeadsMaster)
    }

    steps {
        maven {
            name = "TestMaven"
            id = "RUNNER_16"
            goals = "test"
            workingDir = ".teamcity%env.JDK_19_0%"
            mavenVersion = auto()
            userSettingsSelection = "settings.xml"
            jdkHome = "%env.JDK_19_0%"
            coverageEngine = idea {
                includeClasses = "%build.vcs.number%*"
                excludeClasses = "%build.vcs.number%"
            }
        }
    }

    triggers {
        vcs {
            id = "TRIGGER_9"
        }
    }

    failureConditions {
        failOnText {
            id = "BUILD_EXT_2"
            conditionType = BuildFailureOnText.ConditionType.CONTAINS
            pattern = "${SimpleAstronomyLib_HttpsGithubComFayeznasriSimpleAstronomyLibRefsHeadsMaster.paramRefs.buildVcsNumber}"
            failureMessage = "failure"
            reverse = false
        }
    }

    features {
        perfmon {
            id = "perfmon"
        }
    }
})

object SimpleAstronomyLib_BuildTest : Template({
    name = "BuildTest"
    description = "Template_config"

    publishArtifacts = PublishMode.SUCCESSFUL

    params {
        checkbox("newParam", "env%build.counter%",
                  checked = "true", unchecked = "false")
    }

    steps {
        nunit {
            name = "BuildTest"
            id = "RUNNER_17"
            nunitPath = "%teamcity.tool.NUnit.Console.3.15.0%"
            includeTests = "test%newParam%"
        }
    }
})

object SimpleAstronomyLib_Template : Template({
    name = "Template"
    description = "Template1"

    allowExternalStatus = true
    publishArtifacts = PublishMode.SUCCESSFUL

    triggers {
        vcs {
            id = "TRIGGER_10"
        }
    }
})

object SimpleAstronomyLib_HttpsGithubComFayeznasriSimpleAstronomyLibRefsHeadsMaster : GitVcsRoot({
    name = "https://github.com/fayeznasri/simple-astronomy-lib#refs/heads/master"
    url = "https://github.com/fayeznasri/simple-astronomy-lib"
    branch = "refs/heads/master"
    branchSpec = "refs/heads/*"
    authMethod = password {
        userName = "fayeznasri"
        password = "credentialsJSON:60efae0b-6aed-4d13-8406-370fcb0b302a"
    }
    param("oauthProviderId", "tc-cloud-github-connection")
})
