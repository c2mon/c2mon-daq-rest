# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

All issues referenced in parentheses can be consulted under [CERN JIRA](https://its.cern.ch/jira/projects/CM).
For more details on a given release, please check also the [version planning](https://its.cern.ch/jira/projects/CM/versions).

## Unreleased
### Added
- Added Maven settings.xml that works also outside of CERN and updated README accordingly

### Changed

### Fixed
- Fixed a bug related to auto-configuration. If this feature was turned off (`c2mon.daq.rest.autoConfiguration=false`) the CLient API settings had still to be declared.


## 1.11.0 - 2021-09-07
### Fixed
- Setting change report to success after successful configuration in order to assure correct saving of change in DAQ Core (CM-311)
