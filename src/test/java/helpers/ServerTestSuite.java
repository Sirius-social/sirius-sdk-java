package helpers;

import com.sirius.sdk.base.JsonMessage;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.utils.Pair;

import models.AgentParams;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import sun.misc.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
        serverAddress = ConfTest.getSingletonInstance().test_suite_baseurl;
        url = serverAddress + "/test_suite";
        metadata = null;
        testSuitePath = System.getenv("TEST_SUITE");
        if (testSuitePath == null) {
            testSuiteExistsLocally = false;
        } else {
            //testSuiteExistsLocally = System.path.isfile(test_suite_path) and 'localhost' in self.__server_address
        }

    }

    public AgentParams getAgentParams(String name) {
        if (metadata == null || metadata.isEmpty()) {
            throw new RuntimeException("TestSuite is not running...");
        }
        JsonMessage agentObject = new JsonMessage(metadata);
        JSONObject agent = agentObject.getJSONOBJECTFromJSON(name);
        if (agent == null || agent.isEmpty()) {
            throw new RuntimeException(String.format("TestSuite does not have agent with name %s", name));
        }
        JSONObject p2pObject = agent.getJSONObject("p2p");
        String credentials = agent.getString("credentials");
        JSONObject entitiesObject = agent.getJSONObject("entities");
        JSONObject smartContractObject = p2pObject.getJSONObject("smart_contract");
        JSONObject agentP2pObject = p2pObject.getJSONObject("agent");
        String myVerKey = smartContractObject.getString("verkey");
        String mySecretKey = smartContractObject.getString("secret_key");
        String theirVerkey = agentP2pObject.getString("verkey");
        P2PConnection connection = new P2PConnection(myVerKey, mySecretKey, theirVerkey);
        return new AgentParams(serverAddress, credentials, connection, entitiesObject);
    }

    public String getMetadata() {
        return metadata;
    }

    public void ensureIsAlive() {
        Pair<Boolean,String> okMeta = httpGet(url);
        if(okMeta.first){
            metadata = okMeta.second;
        }else{
            if(testSuiteExistsLocally){
                runSuiteLocally();
            }
        }
    }

    public void runSuiteLocally(){

    }

    public Pair<Boolean, String> httpGet(String url) {
        try {

            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response1 = httpclient.execute(httpGet);
            try {
                HttpEntity entity1 = response1.getEntity();

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        entity1.getContent()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                EntityUtils.consume(entity1);
                // print result
                return new Pair(true, response.toString());
            } finally {
                 response1.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new Pair(false, e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return new Pair(false, e.getMessage());
        }
    }

}
/*




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


*/
