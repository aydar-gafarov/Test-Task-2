
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);
        Document document = new Document();
        document.owner_inn = "1234567890";
        document.products = new Product[1];
        document.products[0] = new Product();
        document.products[0].certificate_document = "1234567890";
        document.products[0].certificate_document_date = new Timestamp(System.currentTimeMillis());
        document.products[0].certificate_document_number = "1234567890";
        document.products[0].owner_inn = "1234567890";
        document.products[0].producer_inn = "1234567890";
        document.products[0].production_date = new Timestamp(System.currentTimeMillis());
        document.products[0].tnved_code = "1234567890";
        document.products[0].uit_code = "1234567890";
        document.products[0].uitu_code = "1234567890";
        document.participant_inn = "1234567890";
        document.production_date = new Timestamp(System.currentTimeMillis());
        document.production_type = "1234567890";
        document.reg_date = "1234567890";
        document.reg_number = "1234567890";
        try {
            crptApi.createDocument(document, "1234567890");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final Semaphore semaphore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.semaphore = new Semaphore(requestLimit, true);
    }

    public String createDocument(Document document, String token) throws InterruptedException, IOException {
        if (!semaphore.tryAcquire(requestLimit, timeUnit)) {
            semaphore.acquire();
        }
        try {
            String jsonDocument = objectMapper.writeValueAsString(document);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonDocument))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Failed to create document: " + response.body());
            }
            return response.body();
        } finally {
            semaphore.release();
        }
    }

    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type = "LP_INTRODUCE_GOODS";
        private boolean importRequest = true;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Timestamp production_date;
        private String production_type;
        private Product[] products;
        private String reg_date;
        private String reg_number;
    }

    public static class Description {
        private String participantInn;
    }

    public static class Product {
        private String certificate_document;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Timestamp certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Timestamp production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
    }
}

