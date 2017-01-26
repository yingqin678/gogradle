# Gogradle - a full-featured build tool for golang

[中文文档](./README_CN.md)

# 英文的标题格式是啥？

[![Build Status](https://travis-ci.org/blindpirate/gogradle.svg?branch=master)](https://travis-ci.org/blindpirate/gogradle)
[![Coverage Status](https://coveralls.io/repos/github/blindpirate/gogradle/badge.svg?branch=master)](https://coveralls.io/github/blindpirate/gogradle?branch=master)
[![Java 8+](https://img.shields.io/badge/java-8+-4c7e9f.svg)](http://java.oracle.com)
[![Apache License 2](https://img.shields.io/badge/license-APL2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

Gogradle is a gradle plugin which provides support for build golang.

## Feature

- Needless to preinstall anything but `JDK 8+` (including golang itself)
- Support all versions of golang and allow their existence at the same time
- Perfect cross-platform support (as long as `Java` can be run)
- Project-scope build, needless to set `GOPATH`
- Full-featured package management
  - Needless to install dependency packages manually, all you need to do is specifying the version
  - Four VCS supported without being installed: Git/Svn/Mercurial/Bazaar (Currently only Git is implemented)
  - Transitive dependency managerment
  - Resolve package conflict automatically
  - Support dependency lock
  - Support importing dependencies managed by various external tools such as glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash (Based on [this report](https://github.com/blindpirate/report-of-go-package-management-tool))
  - Support [SemVersion](http://semver.org/)
  - Support [vendor](https://docs.google.com/document/d/1Bz5-UB7g2uPBdOx-rw5t9MxJwkfpx90cqG9AFL0JAYo)
  - Support flattening dependencies (Inspired by [glide](https://github.com/Masterminds/glide)
  - Support renaming local packages
  - Support private repository
  - `build`/`test` dependencies are managed separately
  - Support dependency tree visualization
- Support build/test/single test/wildcard test/cross compile
- Modern production-grade support for automatic build, simple to define a customized task
- Native syntax of gradle
- Additional feature for users in mainland China who are behind the [GFW](https://en.wikipedia.org/wiki/Great_Firewall)
- IDE support (in plan)

## Advantage/Highlight

- Project-scope build
- Perfect cross-platform support
- Almost all external package management supported
- Fully test coverage
- Long-term support
- Various gradle plugin to enpower your build

## Getting started

- Install [JDK 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Copy `gradle` directory、`gradlew`（*nix）/`gradlew.bat`（Windows）file in **this project** to **the golang project to be built**
- Create a file named `build.gradle` in the golang project to be built with the following content:

```groovy
plugins {
    id "com.github.blindpirate.gogradle" version "0.1.0"
}

golang {
    packagePath = 'your/package/path' // 代构建项目的path
}
```

If you were using one of glide/glock/godep/gom/gopm/govendor/gvt/gbvendor/trash, then Gogradle will read the dependency lock file generated by them automatically without any extra configuration. After Gogradle's initial build, it will generate `gogradle.lock` as its own dependency lock file. Once the file is generated, the original lock file will lose efficacy, and you can delete them. See [Dependency Lock](#Dependency Lock) for more details.

## Build a golang project

Enter root directory of the project and run:

```
./gradlew build # *nix

gradlew build # Windows
```

在下文中，we will use with uniform command form `gradlew <task>` both on `*nix` and `Windows`.

The command is equivalent to `go build` in project root directory with proper `GOPATH`, and Gogradle will do all stuffs such as dependency resolution and installation. Note that Gogradle doesn't touch global `GOPATH` at all, it will install all denpendencies in project root directory and set environment variables to proper values automatically - that means the build would be totally isolated and reproducible.

### Test a golang project

Enter root directory of the project and run:

```
gradlew test
```

If you want to specify some tests:

```
gradlew test --tests main_test.go // Specify a single test
gradlew test --tests *_test.go // Wildcard test
```

If you want to let build depend on test, just add the following line to `build.gradle`:

```groovy
build.dependsOn test
```

### Add a dependency

To add a dependency package, just add its name and version into `dependencies` block of `build.gradle`:

```groovy
dependencies {
    build 'github.com/a/b@v1.0.0' 
    test 'github.com/c/d#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80'
}
```

# 这里参考一下gradle的文档

The `build` and `test` are two independent dependency set. Only `build` dependencies will exist in build process; both `build` and `test` dependencies will exist in test process.

To know more details about dependency, see [Dependency management](#Dependency management).

### Display dependencies

```
gradlew dependencies
```

The output is as follows:

```
build:

github.com/gogits/gogs
├── github.com/Unknwon/cae:c6aac99 √
├── github.com/Unknwon/com:28b053d √
├── github.com/Unknwon/i18n:39d6f27 √
│   ├── github.com/Unknwon/com:28b053d √ (*)
│   └── gopkg.in/ini.v1:766e555 -> 6f66b0e
├── github.com/Unknwon/paginater:701c23f √
├── github.com/bradfitz/gomemcache:2fafb84 √
├── github.com/go-macaron/binding:4892016 √
│   ├── github.com/Unknwon/com:28b053d √ (*)
│   └── gopkg.in/macaron.v1:ddb19a9 √
│       ├── github.com/Unknwon/com:28b053d √ (*)
│       ├── github.com/go-macaron/inject:d8a0b86 -> c5ab7bf
│       └── gopkg.in/ini.v1:766e555 -> 6f66b0e (*)
... 

```

This is denpendency tree of [gogs](https://github.com/gogits/gogs) in v0.9.113. A `glide.lock` file exists in its root directory, thus Gogradle import it automatically. It's simple, isn't it?

Tick mark `√` represents that the dependency is the final resolved version; arrow `->` represents that the dependency conflicts with another dependency and is resolved to another version; star `*` represents this node's descendants are ignored because they have been displayed before.

### Dependency Lock

```
gradlew lock
```

This command will let Gogradle generate a `gogradle.lock` file in project root directory, which records all dependencies of this project. By default, this task will be run before build/test, you don't need to run it manually.

`gogradle.lock` is recommended by Gogradle. Locking dependencies play an important role in reproducible build. Similar to [other package management tools](https://github.com/golang/go/wiki/PackageManagementTools), Gogradle can lock the versions of all dependency packages. Moreover, Gogradle can lock dependency packages even when they are in `vendor`!

Gogradle supports transitive dependency/dependency exclusion/customized url, see [Dependency Management](#Dependency Management).

Currently, only Git dependencies are supported, support for other vcs is under development.

## Configuration

The following complete configuration is located in `golang` block of `build.gradle`.

```groovy
golang {
    
    // Import path of package to be built
    packagePath = 'github.com/user/project'
    
    // The buid mode. There are two alternatives: DEVELOP/REPRODUCIBLE, DEVELOP by default
    // In DEVELOP mode, the dependency priority is:
    // 1.dependencies declared in build.gradle
    // 2.locked dependencies (in goradle.lock or lock file of other tools)
    // 3.dependencies in vendor directory
    // In REPRODUCIBLE mode, the priority is:
    // 1.dependencies in vendor
    // 2.locked dependencies
    // 3.dependencies declared in build.gradle

    mode = 'REPRODUCIBLE'
    
    // The golang version to use. See https://golang.org/dl/
    // If not specified, the latest table version is used
    goVersion = '1.7.1'
    
    // Default value is "go". Modify this when go is not in $PATH
    goExecutable = '/path/to/go/executable'
    
    // aka. build constraint. See https://golang.org/pkg/go/build/#hdr-Build_Constraints
    buildTags = ['appengine','anothertag']
    
    // Additional feature for users in mainland China,
    // ignore it if you are not
    fuckGfw = true
    
    // Extra command line arguments in build or test
    // Empty list by default
    extraBuildArgs = ['arg1','arg2']
    extraTestArgs = []

    // Location of build output, the default value is ./.gogradle
    // It can be absolute or relative (to project root)
    outputLocation = ''
    // Pattern or output, note that it must be single quote here
    outputPattern = '${os}_${arch}_${packageName}'
    // Specify output platforms in cross compile
    // Go 1.5+ required
    targetPlatform = 'windows-amd64, linux-amd64, linux-386'
}
```

## Dependency Management

Dependency management is a nightmare. Fortunately, the package management machanism of Gogradle is excellent enough to handle complicated scenarios.

It's well known that golang doesn't manage packages at all. It assumes that all packages are located in one or more [Workspace](https://golang.org/doc/code.html#Workspaces) specified by `GOPATH`. In build, golang will find required package in `GOPATH`. This caused many issues:

- Lack of package version infomation make it difficult to do reproducible and stable build.
- There may be multiple builds at the same time, and they may depend on different versions of a package.
- A project may depend on mutiple versions of a package due to transitive dependencies

Therefore, golang introduced a mechanism called [vendor](https://docs.google.com/document/d/1Bz5-UB7g2uPBdOx-rw5t9MxJwkfpx90cqG9AFL0JAYo), allowing dependencies to be managed by vcs along with golang project itself. Some questions are solved, but some more arise:

- Existence of redundant code makes project fatter and fatter
- Existence of multiple version in a project will cause [problems](https://github.com/blindpirate/golang-broken-vendor)
- Various [external package management tools](https://github.com/golang/go/wiki/PackageManagementTools) aren't compatible with each other.

Gogradle makes efforts to improve the situation. It doesn't follow golang's workspace convention, and uses totally isolated and project-grade workspace instead. All resolved packages will be placed into temp directory of project root, thus `GOPATH` is not required. 

### Dependency declaration

You can declare dependent packages in `dependencies` block of `build.gradle`. Currently only packages managed by Git are supported. Supports for other vcs are under development.

Some examples are as follows:

```groovy
dependencies {
    build 'github.com/user/project'  // No specific version, the latest will be used
    build name:'github.com/user/project' // Equivalent to last line
    
    build 'github.com/user/project@1.0.0-RELEASE' // Specify a version（tag in Git）
    build name:'github.com/user/project', tag:'1.0.0-RELEASE' // Equivalent to last line
    build name:'github.com/user/project', version:'1.0.0-RELEASE' // Equivalent to last line
    
    test 'github.com/user/project#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // Speicify a commit
    test name:'github.com/user/project', commit:'d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // Equivalent to last line
}
```

[SemVersion](http://semver.org/) is supported in dependency declaration. In Git, a "version" is just a tag. Gogradle doesn't recommend to use SemVersion since it may break reproducibility of build.

```groovy
dependencies {
    build 'github.com/user/project@1.*'  // Equivalent to >=1.0.0 & <2.0.0
    build 'github.com/user/project@1.x'  // Equivalent to last line
    build 'github.com/user/project@1.X'  // Equivalent to last line

    build 'github.com/user/project@~1.5' // Equivalent to >=1.5.0 & <1.6.0
    build 'github.com/user/project@1.0-2.0' // Equivalent to >=1.0.0 & <=2.0.0
    build 'github.com/user/project@^0.2.3' // Equivalent to >=0.2.3 & <0.3.0
    build 'github.com/user/project@1' // Equivalent to 1.X or >=1.0.0 & <2.0.0
    build 'github.com/user/project@!(1.x)' // Equivalent to <1.0.0 & >=2.0.0
    build 'github.com/user/project@ ~1.3 | (1.4.* & !=1.4.5) | ~2' // Very complicated expression
}
```

You can specify a url in declaration, which is extremely useful in case of private repository. See [Repository management](#Repository management) for more details.

```groovy
dependencies {
    build name: 'github.com/user/project', url:'https://github.com/user/project.git', tag:'v1.0.0'
    build name: 'github.com/user/project', url:'git@github.com:user/project.git', tag:'v2.0.0'
}
```

Multiple dependencies can be declared at the same time:

```groovy
dependencies {
    build 'github.com/a/b@1.0.0', 'github.com/c/d@2.0.0', 'github.com/e/f#commitId'
    
    build([name: 'github.com/g/h', version: '2.5'],
          [name: 'github.com/i/j', commit: 'commitId'])
}
```

Gogradle provides support for transitive dependencies. For example, the following declaration excludes transitive dependencies of `github.com/user/project`.

```groovy
dependencies {
    build('github.com/user/project') {
        transitive = false
    }
}
```

What's more, you can excludes some specific transitive dependencies. For example, the following declaration excludes all `github.com/c/d` and `github.com/e/f` in a specific version:

```groovy
dependencies {
    build('github.com/a/b') {
        exclude name:'github.com/c/d'
        exclude name:'github.com/c/d', tag: 'v1.0.0'
    }
}
```

If you have some packages in local directory, you can declare them with:

```groovy
dependencies {
    build name: 'a/local/package', dir: 'path/to/local/package' // It must be absolute
}
```

### build dependency and test dependency

You may notice that there are always `build` or `test` in dependency declarations. It's a term named [Configuration](https://docs.gradle.org/current/userguide/dependency_management.html#sub:configurations) in Gradle. Gogradle predefined `build` and `test` configuration, which you can see as two independent dependencies set. In build, only `build` dependencies will take effect; in test, both of them will take effect and dependencies in `build` will have higher priority.

### Dependency package management

There are four kinds of dependency package in Gogradle:

- Package managed by vcs
- Package located in local file system
- Package in vendor directory
- Package imported in go source code

# 这段可以参考glide的文档

There isn't dependency package in golang's world, golang just treat a ordinary directory as a package. In Gogradle, a dependency package is usually root directory or a repo managed by vcs. For example, all go files in a repository managed by Git belong to one dependency package. Gogradle resolve the package path by [the default golang way](https://golang.org/cmd/go/#hdr-Relative_import_paths).


### Dependency resolution

Dependency resolution is the process in which a dependency is resolved to some concrete code. This process usually relies on vcs such as Git. The ultimate goal of Gogradle is providing support for all four vcs (Git/Mercurial/Svn/Bazaar) with pure Java implementation. Currently only Git is supported. 

### Transitive dependency

The dependency of a dependency (transitive dependency) can be from:

- dependencies in vendor directory
- dependencies in lock files
- dependencies in `import` of go source code

By default, Gogradle will read dependencies in vendor directory and lock files as transitive dependencies. If the result set is empty, `import` statement in `.go` source code will be scanned as transitive dependencies.

### Dependency conflict

In practice, the situation may be extremely complicated due to the existence of transitive dependencies.

When a project depends multiple versions of one package, we say they are conflicted. For example, A depends B in version 1 and C, and C depends B in version 2, then version 1 and version 2 of B is conflicted. Golang's vendor mechanism allow them to exist at the same time, which is opposed by Gogradle. It brings more [problems](https://github.com/blindpirate/golang-broken-vendor) sooner or later. Gogradle will resolve all conflict and flatten them, i.e., Gogradle assure that there is only one version for a package in the final build. The final dependencies will be placed into a temp directory in project root.

The conflict resolution strategy is:

- First level package always wins: dependencies declared in project to be built have higher priority

- Newer package wins: newer package have priority over old ones

In detail, Gogradle will detect "update time" of every dependency, and use that time to resolve conflicts.

- Update time of package managed by vcs is the commit time.
- Update time of package in local file system is the last modified time of directory
- Update time of package in vendor directory is determined by its "host" dependency.

### Dependency lock

Before each build and test, Gogradle will do dependency lock. A file named `gogradle.lock` recording all version information of dependency packages is generated in this task. It can make subsequent build stable and reproducible. Under no circumstances should this file be modified manually.Gogradle encourage to check this file in vcs.

You can use

```
gradlew lock
```

to generate this file.

### Install dependencies into vendor

Vendor machanism is introduced after golang 1.5. It is fully supported but not encouraged by Gogradle. To install dependencies into vendor directory, run:

```
gradlew vendor
```

This task will copy all resolved `build` dependencies into vendor directory. Note that `test` dependencies won't be copied.

## Task in Gogradle

A task unit executed independenly is usually called [Task](https://docs.gradle.org/current/userguide/more_about_tasks.html). Gogradle predefined the following tasks:

- prepare
- resolveBuildDependencies
- resolveTestDependencies
- dependencies
- installBuildDependencies
- installTestDependencies
- build
- test
- clean
- check
- lock
- vendor

### prepare

Do some preparation, for example, verifying `build.gradle` and installing golang executables.

### resolveBuildDependencies/resolveTestDependencies

# Resolve conflict or solve conflict??

Resolve `build` and `test` dependencies to dependency trees. Conflicts will also be solved in this task.

分别解析`build`和`test`的依赖，生成依赖树。在这个过程中会解决相关依赖之间的冲突。

### dependencies

Display the dependency tree of current project. It's very useful when you need to resolve package conflict manually.

### installBuildDependencies/installTestDependencies

Flatten resolved `build` and `test` dependencies and install them into `.gogradle` directory 分别地 so that the future build can use them.

### build

Do build. This task is equivalent to:

```
cd <project path>
export GOPATH=<build dependencies path>
go build -o <output path> 
```

### test

Do test. This task is equivalent to:

```
cd <project path>
export GOPATH=<build dependencies path>:<test dependencies path>
go test
```

### check

This task is usually executed by CI to do some checking, such as test coverate rate. It depends on test task by default.

### clean

Clean temp files in project.

### lock

Generate dependency lock file. See [Dependency lock](#Dependency Lock)

### vendor

Install resolved `build` dependencies into vendor directory. See [Install dependencies into vendor](#) 

## Build output and cross compile

By default, Gogradle will place the build output into `${projectRoot}/.gogradle` directory and name it `${os}_${arch}_${packageName}`. You can change this behaviour in configuration, See [Configuration](#Configuration).


Go1.5+ introduce convenient [cross compile](https://dave.cheney.net/2015/08/22/cross-compilation-with-go-1-5), which enable Gogradle to output results available on multiple platform in a single build.

```
golang {
    ...
    targetPlatform = 'windows-amd64, linux-amd64, linux-386'
    ...
}
```

The configuration above indicates that three results should be outputed?? by current build. 

## 仓库管理

Gogradle supports private repository. You can declare repositories in `repositories` block of `build.gradle`.

By default, Gogradle will read `~/.ssh` when operating on git repositories. If your private key is placed somewhere else, the following configuration can be used:

```
repositories {
    git {
        all()
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }
}
```

You may want to apply different credentials to some repositories, as follows:

```
repositories{
    git {
        url 'http://my-repo.com/my/project.git'
        credentials {
            username ''
            password ''
        }

    git {
        name 'import/path/of/anotherpackage'
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }    
}
```

In the DSL above, `name` and `url` can be any object other than string. Gogradle will use built-in Groovy method [`Object.isCase()`](http://mrhaki.blogspot.jp/2009/08/groovy-goodness-switch-statement.html) to test if it matches the declaration.

For example, you can use regular expressions:

```
    git {
        url ~/.*github\.com.*/
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }
```

If a repository matches a declaration, then the credential in the declaration will be used. Currently only Git repository is supported, you can use username/password or ssh private key as credentials.

Support for other vcs repositories are under development.

## IDE integration

There are many IDEs supporting golang since it is static, e.g., [VSCode](https://github.com/Microsoft/vscode-go)/[IDEA](https://github.com/go-lang-plugin-org/go-lang-idea-plugin)/[Gogland](https://www.jetbrains.com/go/).

Usually, these IDEs ask user to set `GOPATH` and prepare dependent package there. Gogradle try to make it easier. Ideally, a user should be able to start development after checking out the code immediately, without understanding or setting anything.

This is in plan.

## Contributing

If you like Gogradle, star it please.

Please feel free to submit an [issue](https://github.com/blindpirate/gogradle/issues/new).

Fork and [PR](https://github.com/blindpirate/gogradle/pulls) are always welcomed.