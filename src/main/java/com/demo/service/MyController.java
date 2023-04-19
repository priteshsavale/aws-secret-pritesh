package com.demo.service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CommitmentPolicy;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider.RegionalClientSupplier;
import com.amazonaws.encryptionsdk.kmssdkv2.KmsMasterKey;
import com.amazonaws.encryptionsdk.kmssdkv2.KmsMasterKeyProvider;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsBaseClientBuilder;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.KmsClientBuilder;

@RestController
public class MyController {

	@Autowired
	AppConfig config;
	
	@GetMapping("/")
	public void encryptData() {
		
		byte[] s="Hello".getBytes(StandardCharsets.UTF_8);
		
		 AwsBasicCredentials awsCreds = AwsBasicCredentials.create(config.getAccessKey(),config.getSecretKey());
        KmsClient  kmsClient= KmsClient.builder().credentialsProvider(StaticCredentialsProvider.create(awsCreds)).region(Region.US_EAST_1).build();
	
		
		AwsCrypto crypto=AwsCrypto.builder().withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt).build();
		
	 
		KmsMasterKeyProvider keyProvider=KmsMasterKeyProvider.builder()
				.defaultRegion(Region.US_EAST_1)
				.buildStrict(config.getArn());		
		
		
		Map<String, String> context=Collections.singletonMap("env", "dev");
		 CryptoResult<byte[], KmsMasterKey>  result= crypto.encryptData(keyProvider, s,context);
	      System.out.println("encrypted byte array: "+result.getResult());	
	      System.out.println("Encrypted Str: "+new String(result.getResult()));
	      System.out.println("==========================");
	      
	      CryptoResult<byte[], KmsMasterKey> decryptResult = crypto.decryptData(keyProvider, result.getResult());
	      System.out.println(decryptResult.getResult());
	      System.out.println(new String(decryptResult.getResult()));
	      System.out.println(decryptResult.getCryptoAlgorithm().getDataKeyAlgo());
	 
	
	}
	
}
