# Release Procedure

Gentity is published to Maven Central through Sonatype's **Central Publisher
Portal** (<https://central.sonatype.com>).

Publishing is driven by
[`central-publishing-maven-plugin`](https://central.sonatype.org/publish/publish-portal-maven/),
declared in the root `pom.xml` with `<extensions>true</extensions>`. That makes
it take over Maven's `deploy` phase: it collects the whole reactor into a single
bundle and uploads that to the Portal. The root POM therefore has no
`<distributionManagement>` section.

## Prerequisites (one-time, manual)

1. **Namespace.** The `com.github.gentity` namespace must exist and be verified
   under *Namespaces* at <https://central.sonatype.com>.

2. **User token.** Generate a token under *View Account* → *Generate User
   Token* in the Portal.

3. **`~/.m2/settings.xml`.** Add the token under the server id `central` — this
   matches `<publishingServerId>` in the root POM, and the same credentials are
   used for snapshot deployment:

   ```xml
   <settings>
     <servers>
       <server>
         <id>central</id>
         <username><!-- token username --></username>
         <password><!-- token password --></password>
       </server>
     </servers>
   </settings>
   ```

4. **GPG signing key.** Central requires a detached signature for every
   artifact. Create a key and publish it to a public keyserver, otherwise
   validation rejects the deployment:

   ```bash
   gpg --gen-key
   gpg --list-keys --keyid-format LONG
   gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
   ```

   See <https://central.sonatype.org/publish/requirements/gpg/>.

5. **JDK and Maven.** Build with a current JDK (21 or newer is what the release
   plugins are verified against here) and Maven 3.6.3 or newer.

## Version scheme

`master` carries a **three-component** snapshot version, currently
`1.1.0-SNAPSHOT`. Releases are cut directly from `master`; there is no
long-lived release branch. A patch branch is created *lazily* — only when a fix
has to go out for an already released version that `master` has since moved
past. In that case, branch from the release tag:

```bash
git checkout -b gentity-1.1.x v1.1.0
```

To change the version on a branch outside of a release, use:

```bash
mvn versions:set -DnewVersion=1.2.0-SNAPSHOT -DgenerateBackupPoms=false
```

## Snapshots

`mvn deploy` on a `-SNAPSHOT` version uploads to the Portal's snapshot
repository, <https://central.sonatype.com/repository/maven-snapshots/>, using
the same `central` credentials. Snapshots are not validated and are cleaned up
after about 90 days. Consumers who want them must add that URL as a repository
with snapshots enabled — it is not part of Maven Central proper.

## Releasing

Run everything below from a clean working tree on the branch you are releasing.

### 1. Sanity checks

```bash
mvn clean install
```

Javadoc is only built in the `release` profile, and Javadoc errors are the most
common cause of a failed release. `gentity-core` generates part of its sources
with JAXB, so `mvn javadoc:jar` on its own will not compile — run it after the
sources exist:

```bash
mvn clean generate-sources javadoc:jar
```

### 2. Optional: inspect the bundle without publishing

To see exactly what would be uploaded, point the plugin at an unreachable
endpoint. The build fails at the upload step, but the bundle is written first:

```bash
mvn clean deploy -Prelease -DcentralBaseUrl=http://127.0.0.1:1
unzip -l target/central-publishing/central-bundle.zip
```

The bundle must contain, for `gentity` (POM only), `gentity-lib`,
`gentity-core` and `gentity-maven-plugin`: the `.pom`, the main `.jar`, the
`-sources.jar`, the `-javadoc.jar`, and a `.asc` signature plus checksums for
each. The three test modules (`gentity-test`, `gentity-test-eclipselink`,
`gentity-test-hibernate`) must be absent — they set `<skipPublishing>true</skipPublishing>`
on the publishing plugin in their own POMs.

Note this only works on a non-snapshot version; snapshots bypass bundling and
upload directly.

### 3. Prepare

`release:prepare` updates the POMs, commits, tags, and sets the next
development version. Dry-run it first — this touches neither the remote nor the
working copy:

```bash
mvn release:clean release:prepare -DdryRun=true
mvn release:clean
```

Then for real:

```bash
mvn release:clean release:prepare
```

The plugin prompts for the release version, the tag name (`v<version>`, per
`tagNameFormat`) and the next development version. Non-interactively:

```bash
mvn release:clean release:prepare -B \
    -DreleaseVersion=1.1.0 -Dtag=v1.1.0 -DdevelopmentVersion=1.2.0-SNAPSHOT
```

If pushing the tag needs credentials, pass them as
`-Dusername=<user> -Dpassword=<token>`.

### 4. Perform

`release:perform` checks out the tag into `target/checkout` and runs
`deploy` there with the `release` profile active (`<releaseProfiles>release</releaseProfiles>`),
which adds the sources jar, the javadoc jar and the GPG signatures, and then
uploads the bundle to the Portal.

```bash
mvn release:perform
```

`release:perform` forks a **separate** Maven process for that build, and
command-line properties are *not* inherited by it. Anything the forked build
needs has to be passed through `-Darguments`:

```bash
mvn release:perform -Darguments="-Dgpg.passphrase=<passphrase>"
```

(`-Dusername`/`-Dpassword` are consumed by the release plugin itself for SCM
access and do not belong in `-Darguments`.)

The plugin's `waitUntil` default is `validated`, so the command blocks until
Central has validated the upload. A missing signature, a missing javadoc jar or
incomplete POM metadata surfaces here as a build failure, before anything is
visible to the public.

### 5. Publish in the Portal

The root POM sets `<autoPublish>false</autoPublish>`, so a validated deployment
is **not** released automatically. Open
<https://central.sonatype.com/publishing/deployments>, review the deployment
(named `gentity <version>`), and press **Publish**. Dropping it there instead
discards it with no trace on Maven Central.

Once published, the artifacts appear on Maven Central within roughly 10–30
minutes, and in search a while after that. **A published version can never be
changed or removed** — verify in the Portal before pressing Publish.

## Rollback

### Before `release:perform`

```bash
mvn release:rollback
```

This restores the POM versions and reverts the release commits. It does *not*
remove the tag, so delete it locally and on the remote (the tag name is in the
`mvn` output and in `release.properties`):

```bash
git tag -d v<version>
git push origin :refs/tags/v<version>
```

Then clean up the release plugin's scratch files:

```bash
mvn release:clean
```

### After `release:perform`, before pressing Publish

Drop the deployment in the Portal at
<https://central.sonatype.com/publishing/deployments>. Nothing was published, so
the tag and the version can be reused — roll back as above.

### After pressing Publish

Nothing can be undone. Maven Central is immutable. Fix forward with the next
patch version.
