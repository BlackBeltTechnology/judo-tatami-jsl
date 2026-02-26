# Development Version and Branch Handling

This document describes the Git branching strategy, version numbering, and CI/CD automation used by this project.

## Branches

The versioning policy follows [GitFlow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

| Branch Pattern | Purpose |
|----------------|---------|
| `develop` | Main development branch — contains the latest sources for the active version |
| `feature/JNG-NUMBER_short_summary` | Feature branches, based on `develop` |
| `(release/)X.Y.Z` | Release branches (`release/` prefix reserved for CI) |
| `bugfix/JNG-NUMBER_short_summary` | Bugfix branches, based on release branches; must be applied to release and develop branches of newer versions too |
| `support/JNG-NUMBER_short_summary` | Support branches, based on release branches; same merge-forward policy as bugfix |
| `master` | Contains the latest released sources |

```mermaid
gitGraph
    commit id: "init"
    branch develop
    commit id: "dev-1"
    branch feature/JNG-1
    commit id: "feat-1a"
    commit id: "feat-1b"
    checkout develop
    merge feature/JNG-1 id: "merge-feat-1"
    branch feature/JNG-3
    commit id: "feat-3"
    checkout develop
    merge feature/JNG-3 id: "merge-feat-3"
    branch release/1.0-beta1
    commit id: "rc-1"
    branch bugfix/JNG-4
    commit id: "fix-4"
    checkout release/1.0-beta1
    merge bugfix/JNG-4 id: "merge-fix-4"
    checkout develop
    merge release/1.0-beta1 id: "merge-release"
    checkout main
    merge release/1.0-beta1 id: "release-1.0"
```

## Version Numbers

Version numbers follow [semantic versioning](https://semver.org/) with these rules:

| Event | Version change |
|-------|---------------|
| Start a `feature/` branch | No version change |
| Start a `release/` branch | 2nd number (minor) incremented on `develop` |
| Start a `bugfix/` branch | No version change (applied to release branch) |
| Start a `support/` branch | 3rd number (patch) incremented |
| Start a `hotfix/` branch | 4th number incremented (applied to both release and master) |

## GitHub Actions Workflows

### build.yml — Main Build Pipeline

Triggered on: push to `develop`, or pull request targeting `develop`, `master`, `increment/*`, or `release/*`.

```mermaid
flowchart TD
    trigger["Push / PR event"]
    branch_check{"Base branch?"}
    version_snap["Set version from pom.xml<br/>(without -SNAPSHOT)"]
    version_dev["Set version<br/>major.minor.qualifier.date_commitId_branch"]
    build["Build & deploy to Nexus"]
    tag["Create git tag v&lt;version&gt;"]
    is_inc{"increment/* or release/*?"}
    merge_tag["Create tag merge-pr/&lt;version&gt;"]
    trigger_merge["Trigger merge-pr-tagged.yml"]
    is_dev{"develop?"}
    changelog["Build changelog"]
    release["Create GitHub pre-release"]

    trigger --> branch_check
    branch_check -->|master, release/*| version_snap
    branch_check -->|develop, increment/*| version_dev
    version_snap --> build
    version_dev --> build
    build --> tag
    tag --> is_inc
    is_inc -->|Yes| merge_tag --> trigger_merge
    is_inc -->|No| is_dev
    tag --> is_dev
    is_dev -->|Yes| changelog --> release
    is_dev -->|No| done["End"]
```

### merge-pr-tagged.yml — PR Merge Automation

Triggered when a `merge-pr/*` tag is pushed.

```mermaid
flowchart TD
    trigger["merge-pr/* tag pushed"]
    get_ver["Get version from tag"]
    check{"Version format?"}
    merge_master["Merge PR to master"]
    trigger_release["Trigger create-release-on-master.yml"]
    squash_dev["Squash PR to develop"]
    trigger_build["Trigger build.yml"]
    cleanup["Delete merge-pr/ tag"]

    trigger --> get_ver --> check
    check -->|major.minor.qualifier| merge_master --> trigger_release
    check -->|other| squash_dev --> trigger_build
    merge_master --> cleanup
    squash_dev --> cleanup
```

### create-release-on-master.yml — Master Release

Triggered on push to `master`. Gets the version from the tag, builds a changelog, and creates a GitHub release (marked as latest).

### release.yml — Manual Release Trigger

Manually triggered with a version parameter (`auto` or a specific `major.minor.qualifier`).

```mermaid
flowchart TD
    trigger["Manual trigger with version"]
    check{"Version = 'auto'?"}
    auto["Read version from pom.xml<br/>(strip -SNAPSHOT)"]
    manual["Use given version"]
    next["Set next = qualifier + 1"]
    pr_master["Create PR to master<br/>with release version"]
    pr_develop["Create PR to develop<br/>with next version"]
    build1["Trigger build.yml"]
    build2["Trigger build.yml"]

    trigger --> check
    check -->|Yes| auto --> next
    check -->|No| manual --> next
    next --> pr_master --> build1
    next --> pr_develop --> build2
```

## Development Rules

> **Important:** There is no commit without a ticket number. Every pull request and commit must include a JIRA ticket reference (e.g., `JNG-xxx`).

Issue tracking: [JIRA Dashboard](https://blackbelt.atlassian.net/jira/dashboards)
