analytics-android-integration-nielsen-dtvr
======================================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.segment.analytics.android.integrations/nielsen-dtvr/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.segment.analytics.android.integrations/nielsen-dtvr)
[![Javadocs](http://javadoc-badge.appspot.com/com.segment.analytics.android.integrations/nielsen-dtvr.svg?label=javadoc)](http://javadoc-badge.appspot.com/com.segment.analytics.android.integrations/nielsen-dtvr)

Nielsen DTVR integration for [analytics-android](https://github.com/segmentio/analytics-android).

## Installation

To install the Segment-Nielsen-DTVR integration, simply add this line to your gradle file:

```
compile 'com.segment.analytics.android.integrations:nielsen-dtvr:+'

```

## Repository
- [Snapshots](https://oss.sonatype.org/content/repositories/snapshots/com/segment/analytics/android/integrations/nielsen-dtvr/)
- [Releases](https://oss.sonatype.org/content/repositories/releases/com/segment/analytics/android/integrations/nielsen-dtvr/)

## Releasing
CircleCI is configured to release the artifacts when a new tag is created. Snapshot builds are created and uploaded
for each commit in master.

When you are working in a new release, change the version to `<new-version>-SNAPSHOT` in `gradle.properties`. After you
are done, push your changes to master (it will upload the SNAPSHOT version) and then create a tag with the version (it
will release and promote the new version).

### Errors promoting
Go to [Sonatype](https://oss.sonatype.org/#stagingRepositories) to check if the previous staging was not closed
properly, or if the signature was invalid. Segment's staging repositories are `comsegment-xxxxx`.

### Verify signature
You can get Segment's public key from:
- [GnuPG](http://keys.gnupg.net:11371/pks/lookup?search=tools%2Bandroid%40segment.com&fingerprint=on&op=index)
- [PGP MIT](http://pgp.mit.edu/pks/lookup?search=tools%2Bandroid%40segment.com&op=index)

## Usage

After adding the dependency, you must register the integration with our SDK.  To do this, import the Nielsen DTVR integration:


```
import com.segment.analytics.android.integrations.nielsendtvr.NielsenDTVRIntegration;

```

And add the following line:

```
analytics = new Analytics.Builder(this, "write_key")
                .use(NielsenDTVRIntegration.FACTORY)
                .build();
```

## Local testing
The following project properties are required to run the tests or build locally:
* `SEGMENT_WRITE_KEY`: For the sample app
* `nielsen_user`: For getting the Nielsen SDK
* `NIELSEN_AUTHCODE`: For getting the Nielsen SDK

To get more information, see [Nielsen official documentation](https://engineeringportal.nielsen.com/docs/Digital_Measurement_Android_Artifactory_Guide).

An easy way to have this configuration set up is using env vars:
```bash
export ORG_GRADLE_PROJECT_SEGMENT_WRITE_KEY=foo
export ORG_GRADLE_PROJECT_NIELSEN_USER=bar
export ORG_GRADLE_PROJECT_NIELSEN_AUTHCODE=foo_secret
```

## License

```
WWWWWW||WWWWWW
 W W W||W W W
      ||
    ( OO )__________
     /  |           \
    /o o|    MIT     \
    \___/||_||__||_|| *
         || ||  || ||
        _||_|| _||_||
       (__|__|(__|__|

The MIT License (MIT)

Copyright (c) 2019 Segment, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
