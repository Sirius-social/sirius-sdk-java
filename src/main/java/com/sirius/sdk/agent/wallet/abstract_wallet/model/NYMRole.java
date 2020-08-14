package com.sirius.sdk.agent.wallet.abstract_wallet.model;

public enum  NYMRole {
    TRUSTEE,//0
    STEWARD,//2
    TRUST_ANCHOR,//101
    NETWORK_MONITOR,//201
    RESET;//none

    Integer value;


}
/*
   COMMON_USER = (None, 'null')
           TRUSTEE = (0, 'TRUSTEE')
           STEWARD = (2, 'STEWARD')
           TRUST_ANCHOR = (101, 'TRUST_ANCHOR')
           NETWORK_MONITOR = (201, 'NETWORK_MONITOR')
           RESET = (None, '')

           def serialize(self):
           _, role_name = self.value
           return role_name

@staticmethod
    def deserialize(buffer: str):
            role_name = buffer
            if role_name == 'null':
            return NYMRole.COMMON_USER
            elif role_name == 'TRUSTEE':
            return NYMRole.TRUSTEE
            elif role_name == 'STEWARD':
            return NYMRole.STEWARD
            elif role_name == 'TRUST_ANCHOR':
            return NYMRole.TRUST_ANCHOR
            elif role_name == 'NETWORK_MONITOR':
            return NYMRole.NETWORK_MONITOR
            elif role_name == '':
            return NYMRole.RESET
            else:
            raise RuntimeError('Unexpected value "%s"' % buffer)

*/
