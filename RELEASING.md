Releasing Lovebird-Commons
===========================

Release and merge procedures.

## Deploying a SNAPSHOT Version

To deploy a `SNAPSHOT` version to Nexus, [find the corresponding branch
pipeline](https://git.yolt.io/backend/lovebird-commons/pipelines?scope=branches)
and start the `snapshot` step in the `publish` phase.

The artifact version will be prefixed with the branch name. So the full
artifact would be identified as 
`nl.ing.lovebird:lovebird-commons:<branch-name>-7.2.1-SNAPSHOT`

## Merging a branch

Before merging a branch update `CHANGELOG.md` with a brief description of your
changes under the `Unreleased` header.

## Releasing

This project uses [Semantic Versioning](https://semver.org/) since 7.2.0. Do note however that `lovebird-commons` is
used only for internal projects. It is okay to make breaking changes if it is known that no project is using that
feature.

### Releasing a PATCH Version

To release a PATCH version:
1. Verify that the changelog only contains backwards compatible bug fixes.
   If not; see the next section.
2. Change the `Unreleased` header in the `CHANGELOG.md` to the latest version.
3. Add the `Unreleased` header on top of the changelog.   
4. Commit, push and all changes merge.    
5. Find [the most recent `master`-branch pipeline](https://git.yolt.io/backend/lovebird-commons/pipelines?scope=branches&page=1&ref=master)
and manually start the `release` step in the `publish` phase.

### Releasing a MINOR Version

To release a MINOR version:
1. Verify that the changelog only contains new features, deprecations or backwards compatible bug fixes.
   If not; see the next section.
2. Change the version number by executing `./mvnw versions:set -DnewVersion=X.Y.0-SNAPSHOT` in the root of the project.
3. Change the `Unreleased` header in the `CHANGELOG.md` to `X.Y.0`
4. Add the `Unreleased` header on top of the changelog.
5. Commit, push and merge all changes.
6. Find [the most recent `master`-branch pipeline](https://git.yolt.io/backend/lovebird-commons/pipelines?scope=branches&page=1&ref=master)
   and manually start the `release` step in the `publish` phase.
   
### Releasing a MAJOR Version

To release a MAJOR version:
1. Change the version number by executing `./mvnw versions:set -DnewVersion=X.0.0-SNAPSHOT` in the root of the project.
2. Change the `Unreleased` header in the `CHANGELOG.md` to `X.0.0`
3. Commit, push and merge all changes.
4. Find [the most recent `master`-branch pipeline](https://git.yolt.io/backend/lovebird-commons/pipelines?scope=branches&page=1&ref=master)
   and manually start the `release` step in the `publish` phase.   
