//package com.demo.service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.core.SdkBytes;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.kms.KmsClient;
//import software.amazon.awssdk.services.kms.model.DecryptRequest;
//import software.amazon.awssdk.services.kms.model.DecryptResponse;
//import software.amazon.awssdk.services.kms.model.EncryptRequest;
//import software.amazon.awssdk.services.kms.model.EncryptResponse;
//import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;
//
//@RestController
//public class MyController2 {
//
//	@Autowired
//	AppConfig config;
//	
//	
//	@GetMapping("/")
//	public void encryptData() {
//		String input= "{"
//				+ "\"username\":\"root\","
//				+ "\"password\":\"pass1\""
//				+ "}";
//		
//		 AwsBasicCredentials awsCreds = AwsBasicCredentials.create(config.getAccessKey(),config.getSecretKey());
//	        KmsClient  kmsClient= KmsClient.builder()
//	        		.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
//	           		.region(Region.US_EAST_1).build();
//	        
//	       Map<String , String> map=new HashMap();
//	       map.put("env", "dev");
//	            
//	       EncryptRequest encryptRequest = EncryptRequest.builder()
//	    		                          .keyId(config.getArn())
//	    		                          .encryptionAlgorithm(EncryptionAlgorithmSpec.SYMMETRIC_DEFAULT)
//	    		                          .plaintext(SdkBytes.fromByteArray(input.getBytes()))
//	    		                          .build();
//	    
//	         EncryptResponse resp=      kmsClient.encrypt(encryptRequest);
//	         System.out.println("encrypted Data: "+resp.ciphertextBlob());
//	         System.out.println("algo: "+resp.encryptionAlgorithm());
//	         
//	         System.out.println("-------------Decryption----------------");
//	         
//	         DecryptRequest decryptRequest = DecryptRequest.builder().ciphertextBlob(resp.ciphertextBlob()).build();
//	         DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
//	         System.out.println("decrypted: "+decryptResponse.plaintext().asByteArray());
//	         System.out.println("decrypted: "+new String(decryptResponse.plaintext().asByteArray()));
//	         
//	}
//}
