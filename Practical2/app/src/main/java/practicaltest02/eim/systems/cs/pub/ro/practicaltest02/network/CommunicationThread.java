package practicaltest02.eim.systems.cs.pub.ro.practicaltest02.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import practicaltest02.eim.systems.cs.pub.ro.practicaltest02.constants.Constants;
import practicaltest02.eim.systems.cs.pub.ro.practicaltest02.constants.Utilities;


public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");
            String city = bufferedReader.readLine();
            if (city == null || city.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }


            HttpClient httpClient = new DefaultHttpClient();

            HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + "aq?" + Constants.QUERY_ATTRIBUTE + "=" + city);





            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String pageSourceCode = httpClient.execute(httpGet, responseHandler);


            if (pageSourceCode == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                return;
            }
            else {
                System.out.println(pageSourceCode);
                String result = "";
                JSONObject content = new JSONObject(pageSourceCode);
                JSONArray array = content.getJSONArray("RESULTS");
                for (int i = 0; i < array.length(); i++) {
                    result = result.concat(array.getJSONObject(i).getString("name") + "\n");
                }


                //StringTokenizer st = new StringTokenizer(pageSourceCode, "next");
//                String currentString = pageSourceCode;
//                System.out.println("---- Split by space ------");
//                while (currentString.indexOf("next") > 0 ) {
//                    //currentString.substring(currentString.indexOf("next:"), currentString.indexOf("}"));
//                    System.out.println( currentString.substring(currentString.indexOf("next"), currentString.indexOf("}")));
//                    currentString = currentString.substring(currentString.indexOf("next"));
//                }

                System.out.println(result);
                printWriter.print(result);

                printWriter.flush();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (JSONException jsonException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            if (Constants.DEBUG) {
                jsonException.printStackTrace();
            }
        }

            finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}
