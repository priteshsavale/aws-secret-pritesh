package com.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Value("${accessKey}")
	private String accessKey;
	@Value("${SecretAccessKey}")
	private String secretKey;
	@Value("${secretText}")
	private String encrypText;
	@Value("${kms.key.arn}")
	private String arn;
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public String getEncrypText() {
		return encrypText;
	}
	public void setEncrypText(String encrypText) {
		this.encrypText = encrypText;
	}
	public String getArn() {
		return arn;
	}
	public void setArn(String arn) {
		this.arn = arn;
	}
}
