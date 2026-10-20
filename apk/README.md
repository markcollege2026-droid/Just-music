# Just Music APKs

| File | What it is |
|---|---|
| `app-foss-universal-debug.apk` | debug build, debug-signed, `io.github.aedev.justmusic.debug` |
| `app-foss-universal-release.apk` | R8 release build, signed with the CI build key, `io.github.aedev.justmusic` |
| `checksums.txt` | SHA-256 of both APKs |

Fetch with `git fetch origin refs/tags/just-music-apks:refs/tags/just-music-apks`, or open
`apk/` on the tag in GitHub. The tag is replaced on every publish; old APKs are not kept.
