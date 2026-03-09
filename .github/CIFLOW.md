# Development version and branch handling

## Table of Contents
- [Branches](#branches)
- [Version numbers](#version-numbers)
- [GitHub action flows](#github-action-flows)
- [How to develop](#how-to-develop)

## Branches

Versioning policy of JUDO NG modules are based on GitFlow: https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow.

Branches:

* **develop**: development branch contains latest development sources of the last active version
* **feature/JNG-NUMBER_short_summary**: feature branches are based on **develop** and contains sources of new features that will be included in last active version
* **(release/)1_0_beta1**: release branches of 1.0-beta1 (release/ prefix is still reserved for CI)
* **bugfix/JNG-NUMBER_short_summary**, **support/JNG-NUMBER_short_summary**: bugfix and support branches are based on release branches and must be applied to release and development branches of newer versions too
* **master**: contains latest released sources of the last active version

```mermaid
%%{init: { 'gitGraph': {'showBranches': true, 'showCommitLabel': false}} }%%
gitGraph
    commit id: "initial"
    branch develop
    checkout develop
    commit id: "d1"
    branch feature/JNG-1
    checkout feature/JNG-1
    commit id: "f1-1"
    commit id: "f1-2"
    checkout develop
    branch feature/JNG-2
    checkout feature/JNG-2
    commit id: "f2-1"
    checkout develop
    merge feature/JNG-2
    checkout feature/JNG-1
    commit id: "f1-3"
    checkout develop
    merge feature/JNG-1
    branch feature/JNG-3
    commit id: "f3-1"
    checkout develop
    merge feature/JNG-3
    branch release/1.0-beta1
    checkout release/1.0-beta1
    commit id: "r1-1"
    branch bugfix/JNG-4
    commit id: "b4-1"
    checkout release/1.0-beta1
    merge bugfix/JNG-4
    checkout develop
    merge release/1.0-beta1
    checkout main
    merge release/1.0-beta1 tag: "v1.0-beta1"
```

## Version numbers

Version numbers are increased using semantic versioning:

* do not change version numbers on starting feature/ branches
* 2nd number in version of **develop** branch is increased when a release branch started
* do not change version numbers on bugfix/ branches - that are applied on release branches during testing before releasing it (merging to master)
* 3rd number in version of support/ branches is increased when started - it is used to support a previous release including new (minor) changes; support/ branches are merged back to release branch when update is released (without merging changes to master)
* 4th number in version of hotfix/ branches is increased when started (that are applied on both release and master branches)

## GitHub action flows

### build.yml

```mermaid
flowchart TD
    A[/"when: push on develop branch<br/>or pull request on develop,<br/>master, increment/*, release/* branch"/]
    B{Commit or Pull request's<br/>base branch?}
    C[set version from project pom.xml<br/>version without '-SNAPSHOT']
    D[set version major.minor.qualifier.date_commitId_branchName<br/>from project pom.xml version without '-SNAPSHOT']
    E[build and deploy to nexus]
    F[create git tag v&lt;version&gt;]
    G{Pull request or commit<br/>base branch?}
    H[create tag merge-pr/&lt;version&gt;]
    I[trigger merge-pr-tagged.yml]
    J{Pull request's or commit<br/>base branch?}
    K[build change log]
    L[create github release prerelease with change log]
    M((End))

    A --> B
    B -->|master, release/*| C
    B -->|develop, increment/*| D
    C --> E
    D --> E
    E --> F
    F --> G
    G -->|increment/*, release/*| H
    H --> I
    I --> M
    G -->|other| J
    J -->|develop| K
    K --> L
    L --> M
    J -->|other| M
```

### merge-pr-tagged.yml

```mermaid
flowchart TD
    A[/"when: push on merge-pr/* tag"/]
    B[get &lt;version&gt; from tag name]
    C{check &lt;version&gt; format}
    D[merge pull request to master]
    E[trigger create-release-on-master.yml]
    F[squash pull request to develop]
    G[trigger build.yml]
    H[delete tag merge-pr/&lt;version&gt;]
    M((End))

    A --> B
    B --> C
    C -->|major.minor.qualifier| D
    D --> E
    E --> H
    C -->|other| F
    F --> G
    G --> H
    H --> M
```

### create-release-on-master.yml

```mermaid
flowchart TD
    A[/"when: push on master branch"/]
    B[get &lt;version&gt; from tag name]
    C[build change log]
    D[create github release last with change log]
    M((End))

    A --> B
    B --> C
    C --> D
    D --> M
```

### release.yml

```mermaid
flowchart TD
    A[/"when: manually triggered with given version<br/>which is 'auto' or any other in major.minor.qualifier form"/]
    B{given version is}
    C[set release version from project pom.xml<br/>version without '-SNAPSHOT']
    D[set release version to given version]
    E[set next version to release version's qualifier + 1]
    F[create pull request on master with release version]
    G[trigger build.yml]
    H[create pull request on develop with next version]
    I[trigger build.yml]
    M((End))

    A --> B
    B -->|'auto'| C
    B -->|other| D
    C --> E
    D --> E
    E --> F
    F --> G
    G --> H
    H --> I
    I --> M
```

## How to develop

For issue tracking we are using [JIRA](https://blackbelt.atlassian.net/jira/dashboards). Golden rule:

> **IMPORTANT**: There is no commit without ticket number

So for pull request or commit `JNG-xxx` have to be presented in the commit.
