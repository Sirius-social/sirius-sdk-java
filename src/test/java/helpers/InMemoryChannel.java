package helpers;

import com.sirius.sdk.base.ReadOnlyChannel;
import com.sirius.sdk.base.WriteOnlyChannel;

public class InMemoryChannel implements ReadOnlyChannel, WriteOnlyChannel {

    public InMemoryChannel() {
    }

    @Override
    public byte[] read(int timeout) {
        return new byte[0];
    }

    @Override
    public boolean write(byte[] data) {
        return false;
    }

    /*def __init__(self):
    self.queue = asyncio.Queue()

    async def read(self, timeout: int = None) -> bytes:

    ret = None

    async def internal_reading():
    nonlocal ret
    ret = await self.queue.get()

    done, pending = await asyncio.wait([internal_reading()], timeout=timeout)

            for coro in pending:
            coro.cancel()
            if isinstance(ret, bytes):
            return ret
        else:
    raise SiriusTimeoutIO()

    async def write(self, data: bytes) -> bool:
    await self.queue.put(data)
            return True


    async def run_coroutines(*args, timeout: int = 15):
    results = []
    items = [i for i in args]
    done, pending = await asyncio.wait(items, timeout=timeout, return_when=asyncio.FIRST_EXCEPTION)
    for f in done:
            if f.exception():
    raise f.exception()
        results.append(f.result())
                for f in pending:
            f.cancel()
            return results*/
}


