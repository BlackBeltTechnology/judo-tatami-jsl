# AGENTS.md — `judo-tatami-jsl-jsl2psm/src/test/resources/actor`

JSL test model definitions for actor and security realm transformations to PSM.

| File | Purpose |
| --- | --- |
| `ActorTestModel.jsl` | JSL test model declaring an authenticated `Actor` with realm (`"COMPANY"`), claim, identity binding, and principal guard expressions accessing `UserTransfer`. |
| `AnonymousActorTestModel.jsl` | JSL test model declaring an anonymous `Actor` without realm, claim, or guard specifications, providing access to `UserTransfer`. |
