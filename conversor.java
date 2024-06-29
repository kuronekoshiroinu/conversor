import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CurrencyConverter {

    private static final String API_KEY = "tu_api_key_de_ExchangeRate-API";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    public static void main(String[] args) {
        String fromCurrency = "USD"; // Moneda de origen
        String toCurrency = "EUR";   // Moneda de destino
        double amount = 100;         // Cantidad a convertir

        try {
            double conversionRate = getConversionRate(fromCurrency, toCurrency);
            double convertedAmount = amount * conversionRate;
            System.out.printf("%.2f %s = %.2f %s%n", amount, fromCurrency, convertedAmount, toCurrency);
        } catch (IOException e) {
            System.out.println("Error al obtener la tasa de cambio: " + e.getMessage());
        }
    }

    private static double getConversionRate(String fromCurrency, String toCurrency) throws IOException {
        String urlStr = BASE_URL + fromCurrency;
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Parse JSON response
        String jsonResponse = response.toString();
        double conversionRate = parseExchangeRate(jsonResponse, toCurrency);

        return conversionRate;
    }

    private static double parseExchangeRate(String jsonResponse, String toCurrency) {
        // Este método es específico para el formato de respuesta de ExchangeRate-API
        // Puedes ajustarlo según la estructura de la respuesta JSON del API que estés utilizando
        // Aquí se asume que la respuesta contiene un campo 'conversion_rates' con un JSON de tasas de cambio
        String ratesKey = "\"conversion_rates\":";
        int startIndex = jsonResponse.indexOf(ratesKey);
        if (startIndex == -1) {
            throw new IllegalArgumentException("Formato de respuesta no válido");
        }
        int endIndex = jsonResponse.indexOf("}", startIndex + ratesKey.length());
        if (endIndex == -1) {
            throw new IllegalArgumentException("Formato de respuesta no válido");
        }

        String ratesJson = jsonResponse.substring(startIndex + ratesKey.length(), endIndex + 1);
        String rateKey = "\"" + toCurrency + "\":";
        int rateIndex = ratesJson.indexOf(rateKey);
        if (rateIndex == -1) {
            throw new IllegalArgumentException("No se encontró la tasa de cambio para la moneda especificada");
        }

        int rateValueStartIndex = rateIndex + rateKey.length();
        int rateValueEndIndex = ratesJson.indexOf(",", rateValueStartIndex);
        if (rateValueEndIndex == -1) {
            rateValueEndIndex = ratesJson.indexOf("}", rateValueStartIndex);
        }

        String rateValueStr = ratesJson.substring(rateValueStartIndex, rateValueEndIndex);
        return Double.parseDouble(rateValueStr);
    }
}
