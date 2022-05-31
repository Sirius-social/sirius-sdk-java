package examples.raft.helpers;

public class IndyAgent {

}
/*
    WALLET = 'test'
            PASS_PHRASE = 'pass'
            DEFAULT_LABEL = 'BackCompatibility'
            SETUP_TIMEOUT = 60

            def __init__(self):
            self.__address = pytest.old_agent_address
            self.__auth_username = pytest.old_agent_root['username']
            self.__auth_password = pytest.old_agent_root['password']
            self.__endpoint = None
            self.__wallet_exists = False
            self.__endpoint = None
            self.__default_invitation = None

@property
    def endpoint(self) -> str:
            return self.__endpoint

@property
    def default_invitation(self) -> dict:
            return self.__default_invitation

            async def invite(self, invitation_url: str, for_did: str=None, ttl: int=None):
            url = '/agent/admin/wallets/%s/endpoints/%s/invite/' % (self.WALLET, self.endpoint['uid'])
            params = {'url': invitation_url, 'pass_phrase': self.PASS_PHRASE}
            if for_did:
            params['my_did'] = for_did
            if ttl:
            params['ttl'] = ttl
            ok, resp = await self.__http_post(
            path=url,
            json_=params
            )
            assert ok

            async def load_invitations(self):
            url = '/agent/admin/wallets/%s/endpoints/%s/invitations/' % (self.WALLET, self.__endpoint['uid'])
            ok, collection = await self.__http_get(url)
            assert ok is True
            return collection

            async def create_invitation(self, label: str, seed: str=None):
            url = '/agent/admin/wallets/%s/endpoints/%s/invitations/' % (self.WALLET, self.__endpoint['uid'])
            params = {'label': label, 'pass_phrase': self.PASS_PHRASE}
            if seed:
            params['seed'] = seed
            ok, invitation = await self.__http_post(url, params)
            assert ok is True
            return invitation

            async def create_and_store_my_did(self, seed: str = None) -> (str, str):
            url = '/agent/admin/wallets/%s/did/create_and_store_my_did/' % self.WALLET
            params = {'pass_phrase': self.PASS_PHRASE}
            if seed:
            params['seed'] = seed
            ok, resp = await self.__http_post(url, params)
            assert ok is True
            return resp['did'], resp['verkey']

            async def create_pairwise_statically(self, pw: Pairwise):
            url = '/agent/admin/wallets/%s/pairwise/create_pairwise_statically/' % self.WALLET
            metadata = {
            'label': pw.their.label,
            'their_vk': pw.their.verkey,
            'my_vk': pw.me.verkey,
            'their_endpoint': pw.their.endpoint
            }
            params = {'pass_phrase': self.PASS_PHRASE}
            params.update({
            'my_did': pw.me.did,
            'their_did': pw.their.did,
            'their_verkey': pw.their.verkey,
            'metadata': metadata
            })
            ok, resp = await self.__http_post(url, params)
            assert ok is True

            async def ensure_is_alive(self):
            inc_timeout = 10
            for n in range(1, self.SETUP_TIMEOUT, inc_timeout):
            ok, wallets = await self.__http_get('/agent/admin/wallets/')
            if ok:
            break
            progress = float(n / self.SETUP_TIMEOUT) * 100
            print('Indy-Agent setup Progress: %.1f %%' % progress)
            await asyncio.sleep(inc_timeout)
            if not self.__wallet_exists:
            ok, wallets = await self.__http_post(
            '/agent/admin/wallets/ensure_exists/',
            {'uid': self.WALLET, 'pass_phrase': self.PASS_PHRASE}
            )
            assert ok is True
            self.__wallet_exists = True
            ok, resp = await self.__http_post(
            '/agent/admin/wallets/%s/open/' % self.WALLET,
            {'pass_phrase': self.PASS_PHRASE}
            )
            assert ok
            if not self.__endpoint:
            url = '/agent/admin/wallets/%s/endpoints/' % self.WALLET
            ok, resp = await self.__http_get(url)
            assert ok is True
            if resp['results']:
            self.__endpoint = resp['results'][0]
            else:
            ok, endpoint = ok, wallets = await self.__http_post(url, {'host': self.__address})
            assert ok is True
            self.__endpoint = endpoint
            if not self.__default_invitation:
            url = '/agent/admin/wallets/%s/endpoints/%s/invitations/' % (self.WALLET, self.__endpoint['uid'])
            ok, resp = await self.__http_get(url)
            assert ok is True
            collection = [item for item in resp if item['seed'] == 'default']
            if collection:
            self.__default_invitation = collection[0]
            else:
            ok, invitaion = ok, wallets = await self.__http_post(
            url,
            {'label': self.DEFAULT_LABEL, 'pass_phrase': self.PASS_PHRASE, 'seed': 'default'}
            )
            assert ok is True
            self.__default_invitation = invitaion

            async def __http_get(self, path: str):
            url = urljoin(self.__address, path)
            auth = aiohttp.BasicAuth(self.__auth_username, self.__auth_password, 'utf-8')
            netloc = urlparse(self.__address).netloc
            host = netloc.split(':')[0]
            async with aiohttp.ClientSession(auth=auth) as session:
            headers = {
            'content-type': 'application/json',
            'host': host
            }
            try:
            async with session.get(url, headers=headers) as resp:
            if resp.status in [200]:
            content = await resp.json()
            return True, content
            else:
            err_message = await resp.text()
            return False, err_message
            except aiohttp.ClientError:
            return False, None

            async def __http_post(self, path: str, json_: dict=None):
            url = urljoin(self.__address, path)
            auth = aiohttp.BasicAuth(self.__auth_username, self.__auth_password, 'utf-8')
            netloc = urlparse(self.__address).netloc
            host = netloc.split(':')[0]
            async with aiohttp.ClientSession(auth=auth) as session:
            headers = {
            'content-type': 'application/json',
            'host': host
            }
            try:
            body = json.dumps(json_).encode() if json_ else None
            async with session.post(url, headers=headers, data=body) as resp:
            if resp.status in [200, 201]:
            try:
            content = await resp.json()
            except Exception as e:
            content = None
            return True, content
            else:
            err_message = await resp.text()
            return False, err_message
            except aiohttp.ClientError:
            return False, None

*/
