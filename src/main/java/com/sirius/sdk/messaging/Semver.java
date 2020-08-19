package com.sirius.sdk.messaging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper around the more complete VersionInfo class from semver package.
 * <p>
 * This wrapper enables abbreviated versions in message types
 * (i.e. 1.0 not 1.0.0).
 */

public class Semver {

    public static final Pattern SEMVER_RE = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:\\.(0|[1-9]\\d*))?$");
    public static Semver fromStr(String version) {
       // Matcher matcher =  Matcher.
        Semver semver = new Semver();
        return semver;
    }
}
/*class Semver(VersionInfo):
        """
    """
        SEMVER_RE = re.compile(
        r'^(0|[1-9]\d*)\.(0|[1-9]\d*)(?:\.(0|[1-9]\d*))?$'
        )

@classmethod
    def from_str(cls, version_str):
            """ Parse version information from a string. """
            matches = Semver.SEMVER_RE.match(version_str)
            if matches:
            args = list(matches.groups())
            if not matches.group(3):
            args.append('0')
            return Semver(*map(int, filter(partial(is_not, None), args)))

            parts = parse(version_str)

            return cls(
            parts['major'],
            parts['minor'],
            parts['patch'],
            parts['prerelease'],
            parts['build']
            )*/
