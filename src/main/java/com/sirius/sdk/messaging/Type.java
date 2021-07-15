package com.sirius.sdk.messaging;

import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Type {


    public static final Pattern MTURI_RE = Pattern.compile("(.*?)([a-z0-9._-]+)/(\\d[^/]*)/([a-z0-9._-]+)$");
    public static final Pattern MTURI_PROBLEM_REPORT_RE = Pattern.compile("(.*?)([a-z0-9._-]+)/(\\d[^/]*)/([a-z0-9._-]+)/([a-z0-9._-]+)$");
    public static final String FORMAT_PATTERN = "%s%s/%s/%s";
    public static final String FORMAT_PATTERN_PROBLEM_REPORT = "%s%s/%s/%s/%s";
    String docUri;

    public String getDocUri() {
        return docUri;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getVersion() {
        return version;
    }

    public Semver getVersionInfo() {
        return versionInfo;
    }

    public String getName() {
        return name;
    }

    public String getTypeString() {
        return typeString;
    }

    public String getNormalizedString() {
        return normalizedString;
    }

    String protocol;
    String version;
    Semver versionInfo;
    String name;
    String typeString;
    String normalizedString;


    public Type(String docUri, String protocol, Semver versionInfo, String name) {
        this.docUri = docUri;
        this.protocol = protocol;
        this.versionInfo = versionInfo;
        this.name = name;
        // version = versionInfo.
    }


    public Type(String docUri, String protocol, String version, String name) {
        this.docUri = docUri;
        this.protocol = protocol;
        this.version = version;
        this.name = name;
        this.versionInfo = Semver.fromStr(version);
        typeString = String.format(FORMAT_PATTERN, docUri, protocol, version, name);
        normalizedString = String.format(FORMAT_PATTERN, docUri, protocol, versionInfo, name);
    }

    @Override
    public String toString() {
        return String.format("%s%s/%s/%s", docUri, protocol, version, name);
    }


    /**
     * Parse type from string.
     *
     * @param type
     * @return
     */
    public static Type fromStr(String type) throws SiriusInvalidType {
        Matcher matcher = MTURI_RE.matcher(type);
        Matcher matcherProblemReport = MTURI_PROBLEM_REPORT_RE.matcher(type);
        if (!matcher.matches() && !matcherProblemReport.matches()) {
            throw new SiriusInvalidType("Invalid message type");
        }
        if (matcherProblemReport.matches()) {
            if (matcherProblemReport.groupCount() >= 5) {
                return new Type(matcherProblemReport.group(1), matcherProblemReport.group(2), matcherProblemReport.group(3), matcherProblemReport.group(5));
            } else {
                throw new SiriusInvalidType("Invalid message type");
            }
        }
        if (matcher.matches()) {
            if (matcher.groupCount() >= 4) {
                return new Type(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
            } else {
                throw new SiriusInvalidType("Invalid message type");
            }
        }
        throw new SiriusInvalidType("Invalid message type");
    }
}
