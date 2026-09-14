# DEV-004 hotfix 2

## Cause
`DantaPlugin.java` called `DantaCore.version()`, but `DantaCore` currently exposes the version as the public constant `DantaCore.VERSION` and has no `version()` method.

## Fix
Both bootstrap references now use `DantaCore.VERSION`.

## Expected result
`:paper-plugin:jar` should no longer fail on `cannot find symbol: method version()`.
