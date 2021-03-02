package com.sirius.sdk.agent.microledgers;

import java.util.List;

public abstract class AbstractMicroledger {

    public abstract String name();

    public abstract int size();

    public abstract int uncommittedSize();

    public abstract String rootHash();

    public abstract String uncommittedRootHash();

    public abstract int seqNo();

    public abstract void reload();

    public abstract void rename(String newName);
    //    @abstractmethod
    //    async def init(self, genesis: List[Transaction]) -> List[Transaction]:
    //        pass
    //
    //    @abstractmethod
    //    async def append(
    //            self, transactions: Union[List[Transaction], List[dict]], txn_time: Union[str, int] = None
    //    ) -> (int, int, List[Transaction]):
    //        pass
    //
    //    @abstractmethod
    //    async def commit(self, count: int) -> (int, int, List[Transaction]):
    //        pass
    //
    //    @abstractmethod
    //    async def discard(self, count: int):
    //        pass
    //
    //    @abstractmethod
    //    async def merkle_info(self, seq_no: int) -> MerkleInfo:
    //        pass
    //
    //    @abstractmethod
    //    async def audit_proof(self, seq_no: int) -> AuditProof:
    //        pass
    //
    //    @abstractmethod
    //    async def reset_uncommitted(self):
    //        pass
    //
    //    @abstractmethod
    //    async def get_transaction(self, seq_no: int) -> Transaction:
    //        pass
    //
    //    @abstractmethod
    //    async def get_uncommitted_transaction(self, seq_no: int) -> Transaction:
    //        pass
    //
    //    @abstractmethod
    //    async def get_last_transaction(self) -> Transaction:
    //        pass
    //
    //    @abstractmethod
    //    async def get_last_committed_transaction(self) -> Transaction:
    //        pass
    //
    //    @abstractmethod
    //    async def get_all_transactions(self) -> List[Transaction]:
    //        pass
    //
    //    @abstractmethod
    //    async def get_uncommitted_transactions(self) -> List[Transaction]:
    //        pass
}
