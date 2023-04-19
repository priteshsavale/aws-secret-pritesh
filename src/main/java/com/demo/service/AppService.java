package com.demo.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DataKeySpec;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyRequest;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyResponse;

@RestController
public class AppService {
	
	@Autowired
	AppConfig config;
	
	@GetMapping("/")
	public GenerateDataKeyResponse encrypt() throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
		 AwsBasicCredentials awsCreds = AwsBasicCredentials.create(config.getAccessKey(),config.getSecretKey());
	        KmsClient  kmsClient= KmsClient.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds)).region(Region.US_EAST_1).build();
	       Map<String , String> map=new HashMap<>();
	       map.put("env", "dev");
	    GenerateDataKeyRequest request=GenerateDataKeyRequest.builder()
	    		.keyId(config.getArn())
	    		.encryptionContext(map)
	    		.keySpec(DataKeySpec.AES_128)
	    		.build(); 
	        
	    GenerateDataKeyResponse resp=    kmsClient.generateDataKey(request);
	     SdkBytes bytes=   resp.ciphertextBlob();
	       
	    
	    System.out.println("plainTextKey:  "+resp.plaintext().toString());	    
	    System.out.println("Encrypted key : "+bytes);
	    System.out.println("arn: "+resp.keyId());
	    
	    //2 Encrypt the text
	    
	    SecretKeySpec key = new SecretKeySpec(resp.plaintext().asByteArray(),"AES");
        Cipher cipher;
        cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        //#3 getting the encoded content
        byte[] encodedSecret = cipher.doFinal(config.getEncrypText().getBytes());
        
        System.out.println("encoded secretText: "+encodedSecret);
        
        //#4 Decode Encoded SecretText
        
        System.out.println("write to file ------------------------------");
        String  path = Paths.get(".").toAbsolutePath().normalize().toString() + "/encrypted_data_key.txt";
        writeToFile(resp.ciphertextBlob(), path);
        
        
		return resp;
	}
	
	
	 public  void writeToFile(SdkBytes bytesToWrite,String path) throws IOException {
	        FileChannel fc;
	        FileOutputStream outputStream = new FileOutputStream(path);
	        fc = outputStream.getChannel();
	        fc.write(bytesToWrite.asByteBuffer());
	        outputStream.close();
	        fc.close();
	    }
	 
	 public static SdkBytes readFromFile(String path) throws IOException {
	        InputStream in2 = new FileInputStream(path);
	        return SdkBytes.fromInputStream(in2);
	    }
	
	@PostMapping("/")
	public void decrypt(@RequestBody String decryptStr) throws UncheckedIOException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
	   
		 AwsBasicCredentials awsCreds = AwsBasicCredentials.create(config.getAccessKey(),config.getSecretKey());
		    
		 KmsClient  kmsClient= KmsClient.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds)).region(Region.US_EAST_1).build();
		 String path = Paths.get(".").toAbsolutePath().normalize().toString() + "/encrypted_data_key.txt";
         SdkBytes sdkBytes = readFromFile(path);
	  
		
         Map<String , String> map=new HashMap<>();
	       map.put("env", "dev");
        DecryptRequest decryptRequest=DecryptRequest.builder()
        		.ciphertextBlob(sdkBytes)
        		.keyId(config.getArn())
	    		.encryptionContext(map)
	    		
	    		//.keySpec(DataKeySpec.AES_128)
        		.build();
        DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
        SecretKeySpec secretKeySpec = new SecretKeySpec(decryptResponse.plaintext().asByteArray(), "AES");
        
        Cipher cipher1 = Cipher.getInstance("AES");
        cipher1.init(Cipher.DECRYPT_MODE, secretKeySpec);
        System.out.println("decoded :"+ SdkBytes.fromByteArray(cipher1.doFinal(decryptStr.getBytes())).asUtf8String());

        
		
	}
	

}
