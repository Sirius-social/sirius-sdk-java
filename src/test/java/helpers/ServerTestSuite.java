package helpers;

import com.sirius.sdk.base.JsonMessage;
import com.sirius.sdk.encryption.P2PConnection;
import models.AgentParams;
import org.json.JSONObject;

public class ServerTestSuite {


    public static ServerTestSuite newInstance() {
        ServerTestSuite serverTestSuite = new ServerTestSuite();
        return serverTestSuite;
    }

    int SETUP_TIMEOUT = 60;
    String serverAddress;
    String url;
    String metadata;
    String testSuitePath;
    boolean testSuiteExistsLocally;

    public ServerTestSuite() {

    }

    public AgentParams getAgentParams(String name) {
        if (metadata == null || metadata.isEmpty()) {
            throw new RuntimeException("TestSuite is not running...");
        }
        JsonMessage agentObject = new JsonMessage(metadata);
        String agent = agentObject.getStringFromJSON(name);
        if (agent == null || agent.isEmpty()) {
            throw new RuntimeException(String.format("TestSuite does not have agent with name %s", name));
        }

        JSONObject p2pObject = agentObject.getJSONOBJECTFromJSON("p2p");
        String credentials = agentObject.getStringFromJSON("credentials");
        JSONObject entitiesObject = agentObject.getJSONOBJECTFromJSON("entities");
        JSONObject smartContractObject = p2pObject.getJSONObject("smart_contract");
        JSONObject agentP2pObject = p2pObject.getJSONObject("agent");

        String myVerKey = smartContractObject.getString("verkey");
        String mySecretKey = smartContractObject.getString("secret_key");
        String theirVerkey = agentP2pObject.getString("verkey");
        P2PConnection connection = new P2PConnection(myVerKey,mySecretKey,theirVerkey);

        return new AgentParams(serverAddress,credentials,connection,entitiesObject);
    }

}
/*


          def __init__(self):
          self.__server_address = pytest.test_suite_baseurl
          self.__url = urljoin(self.__server_address, '/test_suite')
          self.__metadata = None
          test_suite_path = os.getenv('TEST_SUITE', None)
          if test_suite_path is None:
          self.__test_suite_exists_locally = False
          else:
          self.__test_suite_exists_locally = os.path.isfile(test_suite_path) and 'localhost' in self.__server_address

@property
    def metadata(self):
            return self.__metadata

            def get_agent_params(self, name: str):
            if not self.__metadata:
            raise RuntimeError('TestSuite is not running...')
            agent = self.__metadata.get(name, None)
            if not agent:
            raise RuntimeError('TestSuite does not have agent with name "%s"' % name)
            p2p = agent['p2p']
            return {
            'server_address': self.__server_address,
            'credentials': agent['credentials'].encode('ascii'),
            'p2p': P2PConnection(
            my_keys=(
            p2p['smart_contract']['verkey'],
            p2p['smart_contract']['secret_key']
            ),
            their_verkey=p2p['agent']['verkey']
            ),
            'entities': agent['entities']
            }

            async def ensure_is_alive(self):
            ok, meta = await self.__http_get(self.__url)
            if ok:
            self.__metadata = meta
            else:
            if self.__test_suite_exists_locally:
            await self.__run_suite_locally()
            inc_timeout = 10
            print('\n\nStarting test suite locally...\n\n')

            for n in range(1, self.SETUP_TIMEOUT, inc_timeout):
            progress = float(n / self.SETUP_TIMEOUT)*100
            print('TestSuite setup progress: %.1f %%' % progress)
            await asyncio.sleep(inc_timeout)
            ok, meta = await self.__http_get(self.__url)
            if ok:
            self.__metadata = meta
            print('Server test suite was detected')
            return
            print('Timeout for waiting TestSuite is alive expired!')
            raise RuntimeError('Expect server with running TestSuite. See conftest.py: pytest_configure')

@staticmethod
    async def __run_suite_locally():
            os.popen('python /app/configure.py --asgi_port=$ASGI_PORT --wsgi_port=$WSGI_PORT --nginx_port=$PORT')
            await asyncio.sleep(1)
            os.popen('python /app/manage.py test_suite > /tmp/test_suite.log 2> /tmp/test_suite.err')
            os.popen('supervisord -c /etc/supervisord.conf & sudo nginx -g "daemon off;"')
            await asyncio.sleep(5)

@staticmethod
    async def __http_get(url: str):
            async with aiohttp.ClientSession() as session:
            headers = {
            'content-type': 'application/json'
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
*/
