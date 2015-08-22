package com.coderobot.receiptscanner.util;

import android.util.Log;

import com.coderobot.receiptscanner.ParseException;
import com.coderobot.receiptscanner.model.Product;
import com.coderobot.receiptscanner.model.Receipt;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;


/**
 * Created by the great Tony on 2015/8/15.
 */
public class ReceiptUtils {

    private static final String TAG = "ReceiptUtils";

    private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static String convertDateToAD(String date) throws NumberFormatException {
        int year = Integer.parseInt(date.substring(0, 3)) + 1911;

        return year + date.substring(3);
    }

    public static Receipt parseRaw(String raw) throws ParseException {
        Receipt receipt = new Receipt();

        try {

            receipt.invNum = raw.substring(0, 10);
            receipt.invDate = convertDateToAD(raw.substring(10, 17));
            receipt.invRandomNum = raw.substring(17, 21);
            receipt.invTotalCost = Integer.parseInt(raw.substring(29, 37), 16) + "";
            receipt.sellerID = raw.substring(45, 53);
            receipt.invEncrypt = raw.substring(53, 77);

            String[] tmp = raw.split(":");

            for (int i = 5; i < tmp.length; i += 3) {
                receipt.details.add(new Product(tmp[i], tmp[i + 1], tmp[i + 2]));
            }
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new ParseException();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return receipt;
    }

    public static Receipt queryRecieptDetail(Receipt receipt) throws KeyManagementException, NoSuchAlgorithmException, IOException {
        log("receipt = " + receipt.toString());
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("version", "0.2"));
        urlParameters.add(new BasicNameValuePair("type", "QRCode"));
        urlParameters.add(new BasicNameValuePair("invNum", receipt.invNum));
        urlParameters.add(new BasicNameValuePair("action", "qryInvDetail"));
        urlParameters.add(new BasicNameValuePair("generation", "V2"));
        urlParameters.add(new BasicNameValuePair("invDate", new StringBuilder(receipt.invDate).insert(4, "/").insert(7, "/").toString()));

        urlParameters.add(new BasicNameValuePair("encrypt", receipt.invEncrypt));
        urlParameters.add(new BasicNameValuePair("sellerID", receipt.sellerID));
        urlParameters.add(new BasicNameValuePair("UUID", "008055EC-4F4E-4F57-99A4-5E75113DDB9D"));
        urlParameters.add(new BasicNameValuePair("randomNumber", receipt.invRandomNum));
        urlParameters.add(new BasicNameValuePair("appID", "EINV1201501281498"));

        String data = getQuery(urlParameters);

        SSLContext sc;
        sc = SSLContext.getInstance("TLS");
        sc.init(null, null, new java.security.SecureRandom());

        HttpsURLConnection httpURLConnection = (HttpsURLConnection) new URL("https://www.einvoice.nat.gov.tw/PB2CAPIVAN/invapp/InvApp").openConnection();
        httpURLConnection.setConnectTimeout(3000);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setSSLSocketFactory(sc.getSocketFactory());
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));

        OutputStream outputStream = httpURLConnection.getOutputStream();
        outputStream.write(data.getBytes());

        int response = httpURLConnection.getResponseCode();
        if (response == HttpURLConnection.HTTP_OK) {
            InputStream inptStream = httpURLConnection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(inptStream));
            String line;
            StringBuilder stringBuilder = new StringBuilder("");
            while ((line = rd.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\r');
            }
            rd.close();
            Receipt receipt1 = new Gson().fromJson(stringBuilder.toString(), Receipt.class);
            log("result = " + receipt1.toString());
            return receipt1;
        }

        return null;
    }

    public static Receipt mergeReceipts(Receipt r1, Receipt r2) {
        Receipt receipt = new Receipt();

        receipt.invNum = (!r2.invNum.isEmpty()) ? r2.invNum : r1.invNum;
        receipt.invDate = (!r2.invDate.isEmpty()) ? r2.invDate : r1.invDate;
        receipt.sellerID = (!r2.sellerID.isEmpty()) ? r2.sellerID : r1.sellerID;
        receipt.sellerName = (!r2.sellerName.isEmpty()) ? r2.sellerName : r1.sellerName;
        receipt.invStatus = (!r2.invStatus.isEmpty()) ? r2.invStatus : r1.invStatus;
        receipt.invPeriod = (!r2.invPeriod.isEmpty()) ? r2.invPeriod : r1.invPeriod;
        receipt.invRandomNum = (!r2.invRandomNum.isEmpty()) ? r2.invRandomNum : r1.invRandomNum;
        receipt.invTotalCost = (!r2.invTotalCost.isEmpty()) ? r2.invTotalCost : r1.invTotalCost;
        receipt.invEncrypt = (!r2.invEncrypt.isEmpty()) ? r2.invEncrypt : r1.invEncrypt;
        receipt.v = (!r2.v.isEmpty()) ? r2.v : r1.v;
        receipt.code = (!r2.code.isEmpty()) ? r2.code : r1.code;
        receipt.msg = (!r2.msg.isEmpty()) ? r2.msg : r1.msg;
        receipt.details.addAll(r2.details);


        log("mergeReceipts = " + receipt.toString());
        return receipt;
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
