package com.sirius.sdk.wallet.abstract_wallet.model;

public class AnonCredSchema {
}


/*
    def __init__(self, **kwargs):
        self.__body = dict()
        for field in ['ver', 'id', 'name', 'version', 'attrNames']:
        if field not in kwargs:
        raise SiriusValidationError('Expect for "%s" field exists' % field)
        self.__body[field] = kwargs[field]
        self.__body = kwargs

        def __eq__(self, other):
        if isinstance(other, AnonCredSchema):
        return self.id == other.id and self.name == other.name and \
        self.version == other.version and self.attributes == other.attributes
        else:
        return False

@property
    def id(self) -> str:
            return self.__body['id']

@property
    def attributes(self) -> List[str]:
            return sorted(self.__body['attrNames'])

@property
    def name(self) -> str:
            return self.__body['name']

@property
    def version(self) -> str:
            return self.__body['version']

@property
    def body(self) -> dict:
            return self.__body
*/

