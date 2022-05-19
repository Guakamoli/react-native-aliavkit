fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## iOS

### ios test_beta

```sh
[bundle exec] fastlane ios test_beta
```

以 development 方式打包并上传到蒲公英

### ios beta

```sh
[bundle exec] fastlane ios beta
```

以 ad-hoc 方式打包并上传到蒲公英

### ios release

```sh
[bundle exec] fastlane ios release
```

以 app-store 方式打包并上传到 iTunes Connect

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
