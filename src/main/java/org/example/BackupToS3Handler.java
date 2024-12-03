package org.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
//import com.amazonaws.services.3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BackupToS3Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final String API_URL = "http://18.118.15.246:8080/api/data/export";  // Substitua com o URL da sua API
    private final String BUCKET_NAME = "mybucket-try";
    private final String FILE_NAME = "backup-dados.json";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            // Passo 1: Fazer a requisição para a API e obter os dados
            String dados = getDadosDaApi();

            // Passo 2: Salvar os dados no S3
            salvarNoS3(dados);

            // Passo 3: Retornar uma resposta de sucesso
            response.setStatusCode(200);
            response.setBody("Backup concluído com sucesso!");
        } catch (Exception e) {
            context.getLogger().log("Erro ao realizar backup: " + e.getMessage());
            response.setStatusCode(500);
            response.setBody("Erro ao realizar backup: " + e.getMessage());
        }

        return response;
    }

    // Método para fazer uma requisição GET à API e obter os dados como String
    private String getDadosDaApi() throws IOException {
        // Configurando timeout para a requisição
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000)  // Tempo limite para conectar
                .setSocketTimeout(5000)   // Tempo limite para resposta
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build()) {
            HttpGet request = new HttpGet(API_URL);
            HttpResponse response = httpClient.execute(request);
            return EntityUtils.toString(response.getEntity());
        }
    }

    // Método para salvar os dados no S3
    private void salvarNoS3(String dados) {
        // Criar o cliente S3
        S3Client s3 = S3Client.builder()
                .region(software.amazon.awssdk.regions.Region.US_EAST_2)  // Altere para sua região
                .build();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(FILE_NAME)
                .build();

        // Upload dos dados no S3
        s3.putObject(putObjectRequest, RequestBody.fromString(dados, StandardCharsets.UTF_8));
    }
}

    // Método para salvar os dados no S3
    //private void salvarNoS3(String dados) {
    //    Amazon s3Client = AmazonS3ClientBuilder.defaultClient();
    //    s3Client.putObject(BUCKET_NAME, FILE_NAME, dados);
    //}
