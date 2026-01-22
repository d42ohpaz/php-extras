<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# PHP Extras Changelog

## [Unreleased]

## [2.3.0] - 2026-01-22

### Fixed

* Replaced deprecated APIs with valid APIs

### Changed

* Update build system to use IntelliJ Platform Gradle Plugin (2.x)
* Update minimum version dependency to 2024.2 (242.20224.300)
* Internal code cleanup for future me

### Added

* README.md
* CHANGELOG.md

## [2.2.0] - 2022-07-16

* Support paths in the "psr-0" and "psr-4" sections of "autoload-dev"
* Filter out paths that exist within the project's base path

## [2.1.0] - 2022-07-15

* Fix NullPointerException when composer.json is missing name property
* Fix NullPointerException when removing a Directory (Preferences > Directories) with no content roots
* Simplify notification internals to use modern API calls
* Adding/Removing include paths now denote whether the path was added or removed, respectively
* Remove duplicate include paths when managing content roots

## [2.0.0] - 2022-07-12

* Refactor internals for better maintainability
* Add inspection to add autoload-dev entries as content roots
* Add back the path to the global include paths (Preferences > PHP > Include Path) when removing content root

## [1.2.0] - 2021-12-06

* Use invokeLater for modifying the root paths
* Register NotificationGroup in plugin.xml
* Upgrade SDK to Java 11 and IU 2020.3+

[Unrelease]: https://github.com/d42ohpaz/php-extras/compare/v2.2.0...HEAD
[2.3.0]: https://github.com/d42ohpaz/php-extras/compare/v2.2.0...v2.3.0
[2.2.0]: https://github.com/d42ohpaz/php-extras/compare/v2.1.0...v2.2.0
[2.1.0]: https://github.com/d42ohpaz/php-extras/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/d42ohpaz/php-extras/compare/v1.2.0...v2.0.0
[1.2.0]: https://github.com/d42ohpaz/php-extras/releases/tag/v1.2.0
