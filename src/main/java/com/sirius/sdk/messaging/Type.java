package com.sirius.sdk.messaging;

import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Type {


    public static final Pattern MTURI_RE = Pattern.compile("(.*?)([a-z0-9._-]+)/(\\d[^/]*)/([a-z0-9._-]+)$");
    public static final String FORMAT_PATTERN = "%s%s/%s/%s";
    String docUri;
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


    /**
     * Parse type from string.
     *
     * @param type
     * @return
     */
    public static Type fromStr(String type) throws SiriusInvalidType {
        Matcher matcher = MTURI_RE.matcher(type);
        System.out.println();
        if (!matcher.matches()) {
            throw new SiriusInvalidType("Invalid message type");
        }
        if (matcher.groupCount() >= 4) {
            return new Type(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
        } else {
            throw new SiriusInvalidType("Invalid message type");
        }

    }
}


/*
MTURI_RE = re.compile(r'(.*?)([a-z0-9._-]+)/(\d[^/]*)/([a-z0-9._-]+)$')




class Type:
        """ Message and Module type container """
        FORMAT = '{}{}/{}/{}'

        __slots__ = (
        'doc_uri',
        'protocol',
        'version',
        'version_info',
        'name',
        '_normalized',
        '_str'
        )

        def __init__(
        self,
        doc_uri: str,
        protocol: str,
        version: Union[str, Semver],
        name: str):
        if isinstance(version, str):
        try:
        self.version_info = Semver.from_str(version)
        except ValueError as err:
        raise SiriusInvalidType(
        'Invalid type version {}'.format(version)
        ) from err
        self.version = version
        elif isinstance(version, Semver):
        self.version_info = version
        self.version = str(version)
        else:
        raise SiriusInvalidType(
        '`version` must be instance of str or Semver,'
        ' got {}'.format(type(version).__name__)
        )

        self.doc_uri = doc_uri
        self.protocol = protocol
        self.name = name
        self._str = Type.FORMAT.format(
        self.doc_uri,
        self.protocol,
        self.version,
        self.name
        )
        self._normalized = Type.FORMAT.format(
        self.doc_uri,
        self.protocol,
        self.version_info,
        self.name
        )

@classmethod
    def from_str(cls, type_str):
            """ Parse type from string. """
            matches = MTURI_RE.match(type_str)
            if not matches:
            raise SiriusInvalidType('Invalid message type')

            return cls(*matches.groups())

            def __str__(self):
            return self._str

@property
    def normalized(self):
            """ Return the normalized string representation """
            return self._normalized

            def __hash__(self):
            return hash(self._normalized)

            def __eq__(self, other):
            if isinstance(other, Type):
            return self._normalized == other.normalized
            if isinstance(other, str):
            return self._normalized == other
            raise TypeError('Cannot compare Type and {}'.format(type(other)))

            def __ne__(self, other):
            return not self.__eq__(other)
*/
