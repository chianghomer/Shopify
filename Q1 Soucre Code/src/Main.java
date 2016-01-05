import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by HenryChiang on 2015-12-28.
 */
public class Main {

    private static final int MAX_WEIGHT = 100000;
    private static final String COMP = "computer";
    private static final String KEYBOARD = "keyboard";
    private static final String URL = "http://shopicruit.myshopify.com/products.json";

    public static void main(String args[]){

        String response = readTask(URL);
        ArrayList<JSONObject> itemVariantList = parseAvailableVariantTask(response, COMP);
        itemVariantList.addAll(parseAvailableVariantTask(response, KEYBOARD));

        if (itemVariantList.size() != 0) {

            // Sort the JSONObject ArrayList with respect to the grams. (low to high)
            Collections.sort(itemVariantList, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject obj1, JSONObject obj2) {
                    try {
                        return obj1.getInt("grams") > obj2.getInt("grams") ? 1 : (obj1.getInt("grams") < obj2.getInt("grams") ? -1 : 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            });

            double finalPrice = calculateTotalPrice(0.0, 0, MAX_WEIGHT, itemVariantList);

            if (finalPrice != -1)
                System.out.println("Final Price: " + finalPrice);
            else
                System.out.println("JSONException");

        }

    }

    /**
     * Calculate the total price with respect to the maximum weight constraint
     *
     * @param currentPrice The initial or current price
     * @param currentGrams The initial or current weight in grams
     * @param maxGrams The maximum grams allows
     * @param availableVariantList The ArrayList of the all available variants of the product type(s) that requested
     * @return The price of the purchase and -1 if exception throws.
     */
    public static double calculateTotalPrice(double currentPrice, int currentGrams, int maxGrams, ArrayList<JSONObject> availableVariantList){

        int currentIndex = 0 ;
        try {

            while (currentGrams <= maxGrams && currentIndex < availableVariantList.size()) {

                if (availableVariantList.get(currentIndex).getInt("grams") <= (maxGrams - currentGrams)) {
                    currentGrams += availableVariantList.get(currentIndex).getInt("grams");
                    currentPrice += availableVariantList.get(currentIndex).getDouble("price");
                    currentIndex++;
                } else {
                    break;
                }
            }

            if (currentGrams <= maxGrams && (maxGrams - currentGrams) >= availableVariantList.get(0).getInt("grams"))
                return calculateTotalPrice(currentPrice,currentGrams, maxGrams,availableVariantList);
            else
                return currentPrice;

            } catch (JSONException e){

                e.printStackTrace();
                return -1;
            }

    }

    /**
     * Parse the all the available variants of the given product type from the response string of the url requested.
     *
     * @param itemResult The result string from the url
     * @param productType The type of the product user wishes to be parsed
     * @return A list of the JSON object contains all the available variants with respect to the product type provided
     *         and a zero size of ArrayList is returned is exception throws
     */
    public static ArrayList<JSONObject> parseAvailableVariantTask (String itemResult ,String productType){

        ArrayList<JSONObject> itemVariantArrayList = new ArrayList<>();

        try {

            JSONObject itemJsonObj = new JSONObject(itemResult);

            for (int productPos = 0 ; productPos < itemJsonObj.getJSONArray("products").length() ; productPos++) {

                if (itemJsonObj.getJSONArray("products").getJSONObject(productPos).getString("product_type").equalsIgnoreCase(productType)) {

                    for (int variantPos = 0; variantPos < itemJsonObj.getJSONArray("products").getJSONObject(productPos).getJSONArray("variants").length(); variantPos++) {

                        if (itemJsonObj.getJSONArray("products").getJSONObject(productPos).getJSONArray("variants").getJSONObject(variantPos).getBoolean("available")){

                            itemVariantArrayList.add(itemJsonObj.getJSONArray("products").getJSONObject(productPos).getJSONArray("variants").getJSONObject(variantPos));

                        }
                    }
                }
            }

        } catch (JSONException e){
            e.printStackTrace();
        }

        return itemVariantArrayList;

    }

    /**
     * Request connection with the given url link to get the response string.
     *
     * @param url The url link to be connected to
     * @return The result string
     */
    public static String readTask(String url){

        StringBuilder responseStrBuilder = new StringBuilder();

        try {

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String inputStr;

            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            streamReader.close();

            return responseStrBuilder.toString();

        } catch (IOException e) {
            return e.toString();
        }


    }


}
