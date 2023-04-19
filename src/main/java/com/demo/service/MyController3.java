package com.demo.service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.DependencyTimeoutException;
import software.amazon.awssdk.services.kms.model.DisabledException;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;
import software.amazon.awssdk.services.kms.model.InvalidGrantTokenException;
import software.amazon.awssdk.services.kms.model.InvalidKeyUsageException;
import software.amazon.awssdk.services.kms.model.KeyUnavailableException;
import software.amazon.awssdk.services.kms.model.KmsException;
import software.amazon.awssdk.services.kms.model.KmsInternalException;
import software.amazon.awssdk.services.kms.model.KmsInvalidStateException;
import software.amazon.awssdk.services.kms.model.NotFoundException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretResponse;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@RestController
public class MyController3 {
	
	@Autowired
	AppConfig config;

	@GetMapping
	public void accessSecret() {
		String secretName = "dev/mysqldb";
	    Region region = Region.of("us-east-1");
	 
AwsCredentialsProvider provider=() -> ( AwsBasicCredentials.create(config.getAccessKey(),config.getSecretKey()));
	    // Create a Secrets Manager client

	    SecretsManagerClient client = SecretsManagerClient.builder()
	            .region(region)
	            .credentialsProvider(provider)
	            .build();
	    GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
	            .secretId(secretName)
	            .build();
	    
	    

	    GetSecretValueResponse getSecretValueResponse;

	    try {
	        getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
	    } catch (Exception e) {
	        // For a list of exceptions thrown, see
	        // https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
	        throw e;
	    }

	    String secret = getSecretValueResponse.secretString();
System.out.println("data: "+secret);
	   
		
	}
	
	
	@GetMapping("/a")
	public String createEncryptedSecret() throws NotFoundException, DisabledException, KeyUnavailableException, DependencyTimeoutException, InvalidKeyUsageException, InvalidGrantTokenException, KmsInternalException, KmsInvalidStateException, KmsException, AwsServiceException, SdkClientException {
		String secretString="{\"username\":\"root\",\"password\":\"password1\",\"engine\":\"mysql\","
				+ "\"host\":\"127.0.0.1\",\"port\":\"3306\",\"dbname\":\"mydb\"}";
		 
		
		    Region region = Region.of("us-east-1");
		 
	AwsCredentialsProvider provider=() -> ( AwsBasicCredentials.create(config.getAccessKey(),config.getSecretKey()));
		    // Create a Secrets Manager client

		    SecretsManagerClient secretClient = SecretsManagerClient.builder()
		            .region(region)
		            .credentialsProvider(provider)
		            .build();
		    
		    KmsClient  kmsClient= KmsClient.builder()
	        		.credentialsProvider(provider)
	           		.region(Region.US_EAST_1).build();
		    
		    Map<String , String> map=new HashMap<>();
		       map.put("env", "dev");
		            
		       EncryptRequest encryptRequest = EncryptRequest.builder()
		    		                          .keyId(config.getArn())
		    		                          .encryptionAlgorithm(EncryptionAlgorithmSpec.SYMMETRIC_DEFAULT)
		    		                          .encryptionContext(map)
		    		                          .plaintext(SdkBytes.fromByteArray(secretString.getBytes()))
		    		                          .build();
		      
		     EncryptResponse response=   kmsClient.encrypt(encryptRequest);
					
		    
		     CreateSecretRequest createSecretRequest=CreateSecretRequest.builder()
		    		 .name("dev/encrypte******")
		    		 .kmsKeyId(config.getArn())
		    		 .secretBinary(response.ciphertextBlob())
		    		 .build();
		              
		     
		        CreateSecretResponse createSecretResponse=        secretClient.createSecret(createSecretRequest);
		     
		    System.out.println("secret arn: "+createSecretResponse.arn());
		return createSecretResponse.arn();
	}
	
	
	@GetMapping("/b")
	public String getDecryptedSecrets() {
		
		String secretName = "dev/encryptedDBSecret1";
	    Region region = Region.of("us-east-1");
	 
AwsCredentialsProvider provider=() -> ( AwsBasicCredentials.create(config.getAccessKey(),config.getSecretKey()));
	    // Create a Secrets Manager client

	    SecretsManagerClient client = SecretsManagerClient.builder()
	            .region(region)
	            .credentialsProvider(provider)
	            .build();
	    GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
	            .secretId(secretName)
	            .build();
	    
	    

	    GetSecretValueResponse getSecretValueResponse;

	    try {
	        getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
	    } catch (Exception e) {
	        // For a list of exceptions thrown, see
	        // https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
	        throw e;
	    }
  
	   // String secret = getSecretValueResponse.secretString();
	   // SdkBytes cipherBlob=SdkBytes.fromByteArray(secret.getBytes());
	    SdkBytes cipherBlob=getSecretValueResponse.secretBinary();
	    KmsClient  kmsClient= KmsClient.builder()
        		.credentialsProvider(provider)
           		.region(Region.US_EAST_1).build();
	    
	    Map<String , String> map=new HashMap<>();
	       map.put("env", "dev");
	    DecryptRequest decryptRequest = DecryptRequest.builder()
	    		.ciphertextBlob(cipherBlob)
	    		.encryptionAlgorithm(EncryptionAlgorithmSpec.SYMMETRIC_DEFAULT)
	    		.encryptionContext(map)
	    		.keyId(config.getArn())
	    		.build();
       DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
       String decrypted=new String (decryptResponse.plaintext().asByteArray());
		System.out.println("decrypted: "+decrypted);
		return decrypted;
	}
}
